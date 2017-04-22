package org.honton.chas.datadog.apm.cdi;

import org.honton.chas.datadog.apm.SpanBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TracerTest {

  private TracerTestImpl tracer;

  @Before
  public void setupTracer() {
    tracer = new TracerTestImpl();
  }

  @Test
  public void testParentChild() throws Exception {
    SpanBuilder rootBuilder = tracer.createSpan();
    Assert.assertNull(rootBuilder.parent());
    SpanBuilder childBuilder = tracer.createSpan();
    Assert.assertSame(rootBuilder, childBuilder.parent());
  }
}
