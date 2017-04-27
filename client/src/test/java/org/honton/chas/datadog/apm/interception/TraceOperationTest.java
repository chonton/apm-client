package org.honton.chas.datadog.apm.interception;

import org.honton.chas.datadog.apm.SpanBuilder;
import org.honton.chas.datadog.apm.Tracer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 * Test that interception occurs on proper methods
 */
@RunWith(CdiRunner.class)
public class TraceOperationTest {
  @Inject
  Example example;
  @Inject
  Tracer tracer;

  @Before
  public void createRoot() {
    tracer.createSpan();
  }

  @After
  public void closeRoot() {
    tracer.closeCurrentSpan();
  }

  @Test
  public void methodInheritsClassAnnotation() {
    final SpanBuilder span = tracer.getCurrentSpan();
    Assert.assertNotEquals((Long) span.spanId(), example.on());
  }

  @Test
  public void methodDisablesClassAnnotation() {
    final SpanBuilder span = tracer.getCurrentSpan();
    Assert.assertEquals((Long) span.spanId(), example.off());
  }

  @Test
  public void methodInheritsType() {
    Assert.assertEquals("example", example.inherits().type());
  }

  @Test
  public void methodAltType() {
    Assert.assertEquals("alt", example.alt().type());
  }
}
