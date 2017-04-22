package org.honton.chas.datadog.apm.cdi;

import java.util.Map;
import org.honton.chas.datadog.apm.SpanBuilder;
import org.honton.chas.datadog.apm.SpanBuilder.Augmenter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TracerTest {

  private TracerTestImpl tracer;

  @Before
  public void setupTracer() {
    Augmenter augmenter = new Augmenter() {
      @Override
      public void augment(SpanBuilder spanBuilder) {
        if (spanBuilder.parentId() == null) {
          spanBuilder.meta("key", "value");
        }
      }
    };
    tracer = new TracerTestImpl(augmenter);
  }

  @Test
  public void testParentChild() {
    SpanBuilder rootBuilder = tracer.createSpan();
    Assert.assertNull(rootBuilder.parent());
    SpanBuilder childBuilder = tracer.createSpan();
    Assert.assertSame(rootBuilder, childBuilder.parent());
  }

  @Test
  public void testAugmenter() {
    SpanBuilder rootBuilder = tracer.createSpan();
    Map<String, String> meta = rootBuilder.meta();
    Assert.assertEquals("value", meta.get("key"));

    SpanBuilder childBuilder = tracer.createSpan();
    Assert.assertNull("value", childBuilder.meta());
  }
}
