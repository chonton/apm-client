package org.honton.chas.datadog.apm;

import org.honton.chas.datadog.apm.cdi.TracerImpl;
import org.junit.Assert;
import org.junit.Test;

public class TracerTest {

  @Test
  public void testParentChild() throws Exception {
    TracerImpl tracer = new TracerImpl();
    tracer.setTraceConfiguration(TraceConfigurationFactory.DEFAULTS);

    SpanBuilder rootBuilder = tracer.createSpan();
    Assert.assertNull(rootBuilder.parent());
    SpanBuilder childBuilder = tracer.createSpan();
    Assert.assertSame(rootBuilder, childBuilder.parent());
  }
}
