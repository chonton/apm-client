package org.honton.chas.datadog.apm.cdi;

import lombok.Getter;
import org.honton.chas.datadog.apm.TraceConfigurationFactory;
import org.honton.chas.datadog.apm.api.Span;

/**
 * An implementation that captures the queued span and allows access to it.
 */
public class TracerTestImpl extends TracerImpl {

  @Getter
  private Span capturedSpan;

  public TracerTestImpl() {
    setTraceConfiguration(TraceConfigurationFactory.DEFAULTS);
  }

  @Override
  void queueSpan(Span qs) {
    capturedSpan = qs;
  }
}
