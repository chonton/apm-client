package org.honton.chas.datadog.apm.cdi;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.honton.chas.datadog.apm.SpanBuilder;
import org.honton.chas.datadog.apm.TraceConfiguration;
import org.honton.chas.datadog.apm.TraceOperation;
import org.honton.chas.datadog.apm.Tracer;
import org.honton.chas.datadog.apm.api.Span;
import org.honton.chas.datadog.apm.sender.Writer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.Callable;

/**
 * TracerImpl which acts as span factory.
 */
@ApplicationScoped
@Slf4j
public class TracerImpl implements Tracer {

  private final ThreadLocal<SpanBuilder> CURRENT_SPAN = new ThreadLocal<>();

  @Inject
  private Writer writer;

  private String service;

  public TracerImpl() {
  }

  @Inject
  void setTraceConfiguration(TraceConfiguration configuration) {
    service = configuration.getService();
  }

  /**
   * Execute a Callable with reporting
   * 
   * @param resource The resource being called
   * @param operation The operation being called
   * @param callable The Callable to invoke
   * @return The value returned from the Callable
   */
  @Override
  @SneakyThrows
  public <T> T executeCallable(String resource, String operation, Callable<T> callable) {
    SpanBuilder builder = createSpan(resource, operation);
    try {
      return callable.call();
    } catch (Exception e) {
      builder.exception(e);
      throw e;
    } finally {
      closeSpan(builder);
    }
  }

  /**
   * Execute a Runnable with reporting
   * 
   * @param resource The resource being called
   * @param operation The operation being called
   * @param runnable The runnable to invoke
   */
  @Override
  @SneakyThrows
  public void executeRunnable(String resource, String operation, Runnable runnable) {
    SpanBuilder builder = createSpan(resource, operation);
    try {
      runnable.run();
    } catch (Exception e) {
      builder.exception(e);
      throw e;
    } finally {
      closeSpan(builder);
    }
  }

  /**
   * Get the currently active span
   * 
   * @return The current span, or null
   */
  @Override
  public SpanBuilder getCurrentSpan() {
    return CURRENT_SPAN.get();
  }

  @Override
  public SpanBuilder.SpanContext exportCurrentSpan() {
    return CURRENT_SPAN.get().exportSpan();
  }

  @Override
  public SpanBuilder importCurrentSpan(SpanBuilder.SpanContext spanContext) {
    SpanBuilder span = spanContext.importSpan();
    CURRENT_SPAN.set(span);
    return span;
  }

  /**
   * Import a span across process boundaries using a set of headers.
   * If trace headers are not provided, creates a new root span.
   * 
   * @param headerAccessor The function access to headers. Function supplied with header name and
   *        should return the header value.
   */
  @Override
  public SpanBuilder importSpan(HeaderAccessor headerAccessor) {
    SpanBuilder current;

    String traceIdHeader = headerAccessor.getValue(TRACE_ID);
    if (traceIdHeader == null) {
      current = SpanBuilder.createRoot();
    } else {
      long traceId = Long.parseLong(traceIdHeader, 16);
      long spanId = Long.parseLong(headerAccessor.getValue(SPAN_ID), 16);
      current = SpanBuilder.createChild(traceId, spanId);
    }
    CURRENT_SPAN.set(current);
    return current;
  }

  /**
   * Export a span to another process using headers. Creates a span in this process.
   * 
   * @param resource The remote resource being invoked
   * @param operation The remote operation being invoked
   * @param headerAccessor The function access to headers. Function supplied with header name and
   *        value.
   */
  @Override
  public void exportSpan(String resource, String operation, HeaderMutator headerAccessor) {
    SpanBuilder span = createSpan(resource, operation).type(TraceOperation.WEB);
    headerAccessor.setValue(TRACE_ID, Long.toHexString(span.traceId()));
    headerAccessor.setValue(SPAN_ID, Long.toHexString(span.spanId()));
  }

  /**
   * Create a span which is a child of the current span. The newly created span is now considered
   * the current span.
   * 
   * @return The child span, to be filled with resource and operation.
   */
  @Override
  public SpanBuilder createSpan() {
    SpanBuilder parent = CURRENT_SPAN.get();
    SpanBuilder span = parent == null ? SpanBuilder.createRoot() : parent.createChild();
    CURRENT_SPAN.set(span);
    return span;
  }

  /**
   * Finish the current span and restore the current span's parent as the current span
   */
  @Override
  public void closeCurrentSpan() {
    closeSpan(CURRENT_SPAN.get());
  }

  /**
   * Finish the supplied span and restore the supplied span's parent as the current span.
   * This method is called from finally blocks and must not throw any exceptions.
   * 
   * @param current The currently active span
   */
  @Override
  public void closeSpan(SpanBuilder current) {
    try {
      CURRENT_SPAN.set(current.parent());
      Span span = current.finishSpan(service);
      queueSpan(span);
    } catch (RuntimeException re) {
      log.error("Exception in tracing", re);
    }
  }

  void queueSpan(Span span) {
    writer.queue(span);
  }

  private SpanBuilder createSpan(String resource, String operation) {
    return createSpan().resource(resource).operation(operation);
  }
}
