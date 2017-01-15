package org.honton.chas.datadog.apm;

import java.util.concurrent.Callable;

/**
 * Tracer acts as span manager.  Spans can be created and closed through this interface.  Cross-process
 * traces can be imported and exported.
 */
public interface Tracer {

  String SPAN_ID = "x-ddtrace-parent_span_id";
  String TRACE_ID = "x-ddtrace-parent_trace_id";

  /**
   * Execute a Callable with reporting
   *
   * @param resource The resource being called
   * @param operation The operation being called
   * @param callable The Callable to invoke
   * @return The value returned from the Callable
   */
  <T> T executeCallable(String resource, String operation, Callable<T> callable);

  /**
   * Execute a Runnable with reporting
   *
   * @param resource The resource being called
   * @param operation The operation being called
   * @param runnable The runnable to invoke
   */
  void executeRunnable(String resource, String operation, Runnable runnable);

  /**
   * Get the currently active span
   *
   * @return The current span, or null
   */
  SpanBuilder getCurrentSpan();

  /**
   * Import a span across process boundaries using a set of headers.
   *
   * @param headerAccessor The function access to headers. Function supplied with header name and
   *        should return the header value.
   * @return The SpanBuilder which is a child of the imported span.
   */
  SpanBuilder importSpan(HeaderAccessor headerAccessor);

  /**
   * Export a span to another process using headers. Creates a span in this process.
   *
   * @param resource The remote resource being invoked
   * @param operation The remote operation being invoked
   * @param headerAccessor The function access to headers. Function supplied with header name and
   *        value.
   */
  void exportSpan(String resource, String operation, HeaderMutator headerAccessor);

  /**
   * Create a span which is a child of the current span. The newly created span is now considered
   * the current span.
   *
   * @return The child span, to be filled with resource and operation.
   */
  SpanBuilder createSpan();

  /**
   * Finish the current span and restore the current span's parent as the current span
   */
  void closeCurrentSpan();

  /**
   * Finish the supplied span and restore the supplied span's parent as the current span.
   * This method is called from finally blocks and must not throw any exceptions.
   *
   * @param current The currently active span
   */
  void closeSpan(SpanBuilder current);

  /**
   * A functional interface to access a header value.
   */
  interface HeaderAccessor {
    /**
     * Get a header value.
     * @param name The name of the header.
     * @return The value of the header, or null.
     */
    String getValue(String name);
  }

  /**
   * A functional interface to set a header.
   */
  interface HeaderMutator {
    /**
     * Set a header value.
     * @param name The name of the header.
     * @param value The value of the header.
     */
    void setValue(String name, String value);
  }
}
