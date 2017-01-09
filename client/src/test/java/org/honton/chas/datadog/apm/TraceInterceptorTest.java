package org.honton.chas.datadog.apm;

import java.lang.reflect.Method;

import javax.interceptor.InvocationContext;

import org.honton.chas.datadog.apm.api.Span;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class TraceInterceptorTest {

  private Span span;

  private void test(Object response) throws Exception {

    Tracer tracer = new Tracer() {
      @Override
      void queueSpan(Span qs) {
        span = qs;
      }
    };
    tracer.setTraceConfiguration(DefaultTraceConfigurationFactory.DEFAULTS);

    TraceInterceptor ti = new TraceInterceptor();
    ti.tracer = tracer;

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
        ti.invokeWithReporting(ctx);
        Assert.fail("expecting exception");
      }
      catch(Exception e) {
        Assert.assertSame(response, e);
      }
    }
    else {
      Assert.assertSame(response, ti.invokeWithReporting(ctx));
    }

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
    Assert.assertEquals("what a mess", span.getMeta().get("error.msg"));
    Assert.assertNotEquals(0, span.getError());
  }
}
