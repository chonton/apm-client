package org.honton.chas.datadog.apm.cdi;

import lombok.Getter;
import org.honton.chas.datadog.apm.TraceConfigurationFactory;
import org.honton.chas.datadog.apm.api.Span;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.interceptor.Interceptor;

/**
 * An implementation that captures the queued span and allows access to it.
 */
@Alternative
@Priority(Interceptor.Priority.APPLICATION+10)
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
