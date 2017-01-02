package org.honton.chas.datadog.apm;

import org.junit.Assert;
import org.junit.Test;

public class TracerTest {

  @Test
  public void testParentChild() throws Exception {
    Tracer tracer = new Tracer("service");
    SpanBuilder rootBuilder = tracer.createSpan();
    Assert.assertNull(rootBuilder.parent());
    SpanBuilder childBuilder = tracer.createSpan();
    Assert.assertSame(rootBuilder, childBuilder.parent());
  }
}
