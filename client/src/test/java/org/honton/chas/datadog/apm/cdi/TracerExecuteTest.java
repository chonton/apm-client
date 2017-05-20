package org.honton.chas.datadog.apm.cdi;

import org.honton.chas.datadog.apm.api.Span;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

import static org.junit.Assert.fail;

public class TracerExecuteTest {

  private TracerTestImpl tracer;
  private boolean wasRun;

  @Before
  public void setupTracer() {
    tracer = new TracerTestImpl();
  }

  @After
  public void validate() {
    Assert.assertTrue(wasRun);

    Span span = tracer.getCapturedSpan();
    Assert.assertEquals("service", span.getService());
    Assert.assertEquals("resource", span.getResource());
    Assert.assertEquals("operation", span.getOperation());
  }

  private void testCallable(final boolean throwException) throws URISyntaxException, IOException {
    Callable<Long> runnable = () -> {
      wasRun = true;
      Assert.assertNotNull(tracer.getCurrentSpan());
      if (throwException) {
        throw new RuntimeException();
      }
      return 1066L;
    };

    tracer.executeCallable("resource", "operation", runnable);
  }

  @Test
  public void testCallableWithReturn() throws Exception {
    testCallable(false);
    Assert.assertEquals(0, tracer.getCapturedSpan().getError());
  }

  @Test
  public void testCallableWithException() throws Exception {
    try {
      testCallable(true);
      fail("Expected exception did not get thrown");
    } catch (RuntimeException expected) {
      Assert.assertNotEquals(0, tracer.getCapturedSpan().getError());
    }
  }

  private void testRunnable(final boolean throwException) throws URISyntaxException, IOException {
    Runnable runnable = () -> {
      wasRun = true;
      Assert.assertNotNull(tracer.getCurrentSpan());
      if (throwException) {
        throw new RuntimeException();
      }
    };
    tracer.executeRunnable("resource", "operation", runnable);
  }

  @Test
  public void testRunnableWithReturn() throws Exception {
    testRunnable(false);
    Assert.assertEquals(0, tracer.getCapturedSpan().getError());
  }

  @Test
  public void testRunnableWithException() throws Exception {
    try {
      testRunnable(true);
      fail("Expected exception did not get thrown");
    } catch (RuntimeException expected) {
      Assert.assertNotEquals(0, tracer.getCapturedSpan().getError());
    }
  }
}
