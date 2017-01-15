package org.honton.chas.datadog.apm.interception;

import javax.inject.Inject;
import org.honton.chas.datadog.apm.TraceOperation;
import org.honton.chas.datadog.apm.Tracer;

/**
 * A class to test interception
 */
@TraceOperation
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
}
