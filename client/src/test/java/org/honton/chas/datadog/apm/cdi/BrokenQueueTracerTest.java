package org.honton.chas.datadog.apm.cdi;

import java.io.IOException;
import java.net.URISyntaxException;

import org.honton.chas.datadog.apm.TraceConfigurationFactory;
import org.honton.chas.datadog.apm.api.Span;
import org.honton.chas.datadog.apm.cdi.TracerImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BrokenQueueTracerTest {

  private static final String RE_MESSAGE = "Expected exception to show up as ERROR in log";
  private TracerImpl tracer;

  @Before
  public void setupTracer() {
    tracer = new TracerImpl() {
      @Override
      void queueSpan(Span qs) {
        throw new RuntimeException(RE_MESSAGE);
      }
    };
    tracer.setTraceConfiguration(TraceConfigurationFactory.DEFAULTS);
  }

  private void testRunnable(final boolean throwException) throws URISyntaxException, IOException {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        Assert.assertNotNull(tracer.getCurrentSpan());
        if (throwException) {
          throw new RuntimeException();
        }
      }
    };
    tracer.executeRunnable("resource", "operation", runnable);
  }

  @Test
  public void testRunnable() throws Exception {
    try {
      testRunnable(false);
    } catch (RuntimeException expected) {
      Assert.assertEquals(RE_MESSAGE, expected.getMessage());
      Assert.assertNull(tracer.getCurrentSpan());
    }
  }
}
