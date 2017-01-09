package org.honton.chas.datadog.apm;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

import org.honton.chas.datadog.apm.api.Span;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TracerExecuteTest {

  private Span span;
  private Tracer tracer;
  private boolean wasRun;

  @Before
  public void setupTracer() {
    tracer = new Tracer() {
      @Override
      void queueSpan(Span qs) {
        span = qs;
      }
    };
    tracer.setTraceConfiguration(DefaultTraceConfigurationFactory.DEFAULTS);
  }

  @After
  public void validate() {
    Assert.assertTrue(wasRun);

    Assert.assertEquals("service", span.getService());
    Assert.assertEquals("resource", span.getResource());
    Assert.assertEquals("operation", span.getOperation());
  }

  private void testCallable(boolean throwException) throws URISyntaxException, IOException {
    Callable<Long> runnable = new Callable<Long>() {
      @Override
      public Long call() throws Exception {
        wasRun = true;
        Assert.assertNotNull(tracer.getCurrentSpan());
        if (throwException) {
          throw new RuntimeException();
        }
        return 1066L;
      }
    };

    tracer.executeCallable("resource", "operation", runnable);
  }

  @Test
  public void testCallableWithReturn() throws Exception {
    testCallable(false);
    Assert.assertEquals(0, span.getError());
  }

  @Test
  public void testCallableWithException() throws Exception {
    try {
      testCallable(true);
      fail("Expected exception did not get thrown");
    } catch (RuntimeException expected) {
      Assert.assertNotEquals(0, span.getError());
    }
  }

  private void testRunnable(boolean throwException) throws URISyntaxException, IOException {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        wasRun = true;
        Assert.assertNotNull(tracer.getCurrentSpan());
        if (throwException) {
          throw new RuntimeException();
        }
      }
    };
    tracer.executeRunnable("resource", "operation", runnable);
  }

  @Test
  public void testRunnableWithReturn() throws Exception {
    testRunnable(false);
    Assert.assertEquals(0, span.getError());
  }

  @Test
  public void testRunnableWithException() throws Exception {
    try {
      testRunnable(true);
      fail("Expected exception did not get thrown");
    } catch (RuntimeException expected) {
      Assert.assertNotEquals(0, span.getError());
    }
  }
}
