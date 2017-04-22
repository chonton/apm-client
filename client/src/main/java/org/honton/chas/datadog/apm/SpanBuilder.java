package org.honton.chas.datadog.apm;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.honton.chas.datadog.apm.api.Span;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * An active Span builder.  Provides methods to get and set the Span attributes.
 * Some attributes, such as the start time, traceId, parentId, and traceId
 * are final and cannot be modified.
 */
@Accessors(fluent = true)
@Getter
@Setter
@RequiredArgsConstructor
public class SpanBuilder {

  /**
   * A callback used to augment newly constructed Spans
   */
  public interface Augmenter {
    /**
     * Add information to the span before submitting to APM
     * @param spanBuilder The span builder to augment
     */
    void augment(SpanBuilder spanBuilder);
  }

  private static final Random ID_GENERATOR = new Random();

  private final SpanBuilder parent;

  /**
   * The resource name.
   */
  private String resource;

  /**
   * The operation name.
   */
  private String operation;

  /**
   * The id of the trace's root span.
   */
  private final long traceId;

  /**
   * The id of the span's direct parent span.
   */
  private final Long parentId;

  /**
   * The id of the span.
   */
  private final long spanId;

  /** 
   * The type of the span. e.g. http, sql
   */
  private String type;

  /**
   * The tags in the span.
   */
  private Map<String, String> meta;

  /**
   * The metrics in the span.
   */
  private Map<String, Number> metrics;

  /**
   * A error code that occurred for span
   */
  private int error;

  /**
   * The span start in nanoseconds (not epoch time)
   */
  private final long start = System.nanoTime();

  private static final long WALL_OFFSET = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()) - System.nanoTime();

  /**
   * Add a metric.
   * 
   * @param key The name of the metric
   * @param value The value of the metric
   * 
   * @return The builder, for fluent style programming
   */
  public SpanBuilder metric(String key, Number value) {
    if(metrics == null) {
      metrics = new HashMap<>();
    }
    metrics.put(key, value);
    return this;
  }

  /**
   * Add meta information.
   * 
   * @param key The name of the meta information
   * @param value The value of the meta information
   * 
   * @return The builder, for fluent style programming
   */
  public SpanBuilder meta(String key, String value) {
    if(meta == null) {
      meta = new HashMap<>();
    }
    meta.put(key, value);
    return this;
  }

  /**
   * Add exception information to the metadata.  Any previous exception information will be overwritten.
   * 
   * @param e The exception to add
   * @return The builder, for fluent style programming
   */
  public SpanBuilder exception(Throwable e) {
    meta("error.msg", e.getMessage());
    String exception = e.getClass().getCanonicalName();
    error = exception.hashCode();
    meta.put("error.type", exception);
    meta.put("error.stack", exceptionToString(e));
    return this;
  }

  /**
   * Create a child of this span
   * @return The child span
   */
  public SpanBuilder createChild() {
    return new SpanBuilder(this, traceId, spanId, createId());
  }

  /**
   * Create a builder for a root span.
   * @return A builder for a root span
   */
  public static SpanBuilder createRoot() {
    long traceId = createId();
    return new SpanBuilder(null, traceId, null, traceId);
  }

  /**
   * Create a builder for a span which is a child of another span.
   * @param traceId The id of the trace
   * @param parentSpanId The id of the parent span
   * @return The span which is a child the the imported span.
   */
  public static SpanBuilder createChild(long traceId, long parentSpanId) {
    return new SpanBuilder(null, traceId, parentSpanId, createId());
  }

  /**
   * Finish building the span.  Sets the duration of the span.
   * @param service The service value
   * @return The immutable Span that was completed
   */
  public Span finishSpan(String service) {
    return new Span(service, resource, operation,
        traceId, parentId, spanId, type,
        meta != null ? copyOf(meta) : null,
        metrics != null ? copyOf(metrics) : null,
        error,
        WALL_OFFSET + start, System.nanoTime() - start);
  }

  private static <K,V> Map<K,V> copyOf(Map<K,V> map) {
    return Collections.unmodifiableMap(new HashMap<>(map));
  }

  /**
   * Create a 63 bit random id.  Top bit is always clear to prevent serializing negative id.
   * @return A positive long value.
   */
  private static long createId() {
    long high = ID_GENERATOR.nextInt() & 0x7fffffffL;
    long low = ID_GENERATOR.nextInt() & 0xffffffffL;
    return (high << 32) | low;
  }

  /**
   * Create a string representation of an exception stack trace.
   * @param ex The exceptions
   * @return The stack trace
   */
  private static String exceptionToString(Throwable ex) {
    StringWriter errors = new StringWriter();
    ex.printStackTrace(new PrintWriter(errors));
    return errors.toString();
  }
}
