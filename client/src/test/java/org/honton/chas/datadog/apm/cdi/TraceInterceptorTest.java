package org.honton.chas.datadog.apm.cdi;

import org.honton.chas.datadog.apm.api.Span;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;

public class TraceInterceptorTest {

  private TracerTestImpl tracer;
  private TraceInterceptor traceInterceptor;

  @Before
  public void setupTracer() {
    tracer = new TracerTestImpl();
    traceInterceptor = new TraceInterceptor();
    traceInterceptor.tracer = tracer;
  }

  private void test(Object response) throws Exception {

    InvocationContext ctx = Mockito.mock(InvocationContext.class);
    if(response instanceof Exception) {
      Mockito.when(ctx.proceed()).thenThrow((Exception)response);
    }
    else {
      Mockito.when(ctx.proceed()).thenReturn(response);
    }

    Method method = getClass().getMethod("testWithReturn");
    Mockito.when(ctx.getMethod()).thenReturn(method);

    if(response instanceof Exception) {
      try {
        traceInterceptor.invokeWithReporting(ctx);
        Assert.fail("expecting exception");
      }
      catch(Exception e) {
        Assert.assertSame(response, e);
      }
    }
    else {
      Assert.assertSame(response, traceInterceptor.invokeWithReporting(ctx));
    }

    Span span = tracer.getCapturedSpan();
    Assert.assertEquals("service", span.getService());
    Assert.assertEquals(getClass().getCanonicalName(), span.getResource());
    Assert.assertEquals(method.getName(), span.getOperation());
  }

  @Test
  public void testWithReturn() throws Exception {
    test(new Object());
  }

  @Test
  public void testWithException() throws Exception {
    test(new RuntimeException("what a mess"));
    Span span = tracer.getCapturedSpan();
    Assert.assertEquals("what a mess", span.getMeta().get("error.msg"));
    Assert.assertNotEquals(0, span.getError());
  }
}
