package org.honton.chas.datadog.apm.cdi;

import lombok.Getter;
import org.honton.chas.datadog.apm.SpanBuilder;
import org.honton.chas.datadog.apm.TraceConfiguration;
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

  public TracerTestImpl(SpanBuilder.Augmenter augmenter) {
    setTraceConfiguration(new TraceConfiguration(
    TraceConfigurationFactory.DEFAULTS.getService(),
      TraceConfigurationFactory.DEFAULTS.getCollectorUrl(),
      TraceConfigurationFactory.DEFAULTS.getBackoffDuration(),
      augmenter
    ));
  }

  @Override
  void queueSpan(Span qs) {
    capturedSpan = qs;
  }
}
