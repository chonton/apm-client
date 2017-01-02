package org.honton.chas.datadog.apm;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;

import org.honton.chas.datadog.apm.api.Span;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Tracer which acts as span factory.
 */
@ApplicationScoped
@Slf4j
public class Tracer {

  static final String SPAN_ID = "X-SpanId";
  static final String TRACE_ID = "X-TraceId";

  private ThreadLocal<SpanBuilder> currentSpan = new ThreadLocal<>();

  private final String service;

  public Tracer(String service) {
    this.service = service;
  }

  /**
   * Execute a Callable with reporting
   * 
   * @param resource The resource being called
   * @param operation The operation being called
   * @param callable The Callable to invoke
   * @return The value returned from the Callable
   */
  @SneakyThrows
  public <T> T executeCallable(String resource, String operation, Callable<T> callable) {
    SpanBuilder builder = createSpan(resource, operation);
    try {
      return callable.call();
    } catch (Exception e) {
      builder.exception(e);
      throw e;
    } finally {
      finishSpan(builder);
    }
  }

  /**
   * Execute a Runnable with reporting
   * 
   * @param resource The resource being called
   * @param operation The operation being called
   * @param callable The runnable to invoke
   */
  @SneakyThrows
  public void executeRunnable(String resource, String operation, Runnable runnable) {
    SpanBuilder builder = createSpan(resource, operation);
    try {
      runnable.run();
    } catch (Exception e) {
      builder.exception(e);
      throw e;
    } finally {
      finishSpan(builder);
    }
  }

  /**
   * Get the currently active span
   * 
   * @return The current span, or null
   */
  public SpanBuilder getCurrentSpan() {
    return currentSpan.get();
  }

  /**
   * Import a span across process boundaries using a set of headers.
   * 
   * @param headerAccess The function access to headers. Function supplied with header name and
   *        should return the header value.
   * @param resource The name of the resource for the span.
   * @param operation The name of the operation for the span.
   */
  void importSpan(Function<String, String> headerAccess, String resource, String operation) {
    SpanBuilder current;

    String traceIdHeader = headerAccess.apply(TRACE_ID);
    if (traceIdHeader == null) {
      current = SpanBuilder.createRoot();
    } else {
      long traceId = Long.parseUnsignedLong(traceIdHeader, 16);
      long spanId = Long.parseUnsignedLong(headerAccess.apply(SPAN_ID), 16);
      current = SpanBuilder.createChild(traceId, spanId);
    }
    currentSpan.set(current.resource(resource).operation(operation).type("http"));
  }

  /**
   * Export a span to another process using headers. Creates a span in this process.
   * 
   * @param resource The remote resource being invoked
   * @param operation The remote operation being invoked
   * @param headerAccess The function access to headers. Function supplied with header name and
   *        value.
   */
  void exportSpan(String resource, String operation, BiConsumer<String, String> headerAccess) {
    SpanBuilder span = createSpan(resource, operation).type("http");
    headerAccess.accept(TRACE_ID, Long.toHexString(span.traceId()));
    headerAccess.accept(SPAN_ID, Long.toHexString(span.spanId()));
  }

  /**
   * Create a span which is a child of the current span. The newly created span is now considered
   * the current span.
   * 
   * @return The child span, to be filled with resource and operation.
   */
  SpanBuilder createSpan() {
    SpanBuilder parent = currentSpan.get();
    SpanBuilder span = parent == null ? SpanBuilder.createRoot() : parent.createChild();
    currentSpan.set(span);
    return span;
  }

  /**
   * Finish the current span and restore the current span's parent as the current span
   */
  final void finishSpan() {
    finishSpan(currentSpan.get());
  }

  /**
   * Finish the supplied span and restore the supplied span's parent as the current span.
   * This method is called from finally blocks and must not throw any exceptions.
   * 
   * @param current The currently active span
   */
  final void finishSpan(SpanBuilder current) {
    try {
      currentSpan.set(current.parent());
      Span span = current.finishSpan(service);
      queueSpan(span);
    } catch (RuntimeException re) {
      log.error("unexpected exception in tracing", re);
    }
  }

  void queueSpan(Span span) {
    // TODO Auto-generated method stub
  }

  private SpanBuilder createSpan(String resource, String operation) {
    return createSpan().resource(resource).operation(operation);
  }
}
