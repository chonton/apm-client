package org.honton.chas.datadog.apm.interception;

import javax.inject.Inject;
import org.honton.chas.datadog.apm.Tracer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
    Long unexpected = tracer.getCurrentSpan().spanId();
    Assert.assertNotEquals(unexpected, example.on());
  }

  @Test
  public void methodDisablesClassAnnotation() {
    Long expected = tracer.getCurrentSpan().spanId();
    Assert.assertEquals(expected, example.off());
  }
}
