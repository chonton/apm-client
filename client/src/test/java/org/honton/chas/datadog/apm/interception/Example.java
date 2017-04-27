package org.honton.chas.datadog.apm.interception;

import org.honton.chas.datadog.apm.SpanBuilder;
import org.honton.chas.datadog.apm.TraceOperation;
import org.honton.chas.datadog.apm.Tracer;

import javax.inject.Inject;
import org.honton.chas.datadog.apm.api.Span;

/**
 * A class to test interception
 */
@TraceOperation(type = "example")
public class Example {

  @Inject
  private Tracer tracer;

  @TraceOperation(false)
  public Long off() {
    return tracer.getCurrentSpan().spanId();
  }

  public Long on() {
    return tracer.getCurrentSpan().spanId();
  }

  @TraceOperation(type = "alt")
  public SpanBuilder alt() {
    return tracer.getCurrentSpan();
  }

  @TraceOperation
  public SpanBuilder inherits() {
    return tracer.getCurrentSpan();
  }
}
