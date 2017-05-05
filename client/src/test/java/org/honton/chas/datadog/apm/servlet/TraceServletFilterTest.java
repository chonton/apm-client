package org.honton.chas.datadog.apm.servlet;

import org.honton.chas.datadog.apm.SpanBuilder;
import org.honton.chas.datadog.apm.api.Span;
import org.honton.chas.datadog.apm.cdi.TracerImpl;
import org.honton.chas.datadog.apm.cdi.TracerTestImpl;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TraceServletFilterTest {

  private SpanBuilder activeInFilter;

  private Span test(String clientTraceId, String clientSpanId, int statusCode) throws IOException, ServletException {

    final TracerTestImpl tracer = new TracerTestImpl();

    TraceServletFilter tsf = new TraceServletFilter();
    tsf.setTracer(tracer);

    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);

    Mockito.when(req.getServerName()).thenReturn("example.com");
    Mockito.when(req.getServerPort()).thenReturn(7110);
    Mockito.when(req.getMethod()).thenReturn("GET");
    Mockito.when(req.getRequestURI()).thenReturn("/some/path");
    Mockito.when(req.getScheme()).thenReturn("https");

    Mockito.when(req.getHeader(Mockito.eq(TracerImpl.SPAN_ID))).thenReturn(clientSpanId);
    Mockito.when(req.getHeader(Mockito.eq(TracerImpl.TRACE_ID))).thenReturn(clientTraceId);

    HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
    Mockito.when(resp.getStatus()).thenReturn(statusCode);

    Assert.assertNull(tracer.getCurrentSpan());
    FilterChain filter = new FilterChain() {
      @Override public void doFilter(ServletRequest request, ServletResponse response)
        throws IOException, ServletException {
        activeInFilter = tracer.getCurrentSpan();
      }
    };
    tsf.doFilter(req, resp, filter);
    Assert.assertNull(tracer.getCurrentSpan());

    Assert.assertNotNull(activeInFilter);

    Span span = tracer.getCapturedSpan();
    Assert.assertEquals("service", span.getService());
    Assert.assertEquals("example.com:7110", span.getResource());
    Assert.assertEquals("GET /some/path", span.getOperation());
    return span;
  }

  @Test
  public void testNoClientTrace() throws IOException, ServletException {
    Span span = test(null, null, 200);
    Assert.assertNull(span.getParentId());
    Assert.assertNotNull(span.getTraceId());
    Assert.assertNotNull(span.getSpanId());
    Assert.assertEquals(0, span.getError());
  }

  @Test
  public void testWithClientTrace() throws IOException, ServletException {
    Span span = test("fdfd", "5a5a", 400);
    Assert.assertEquals(0xfdfdL, span.getTraceId());
    Assert.assertEquals(0x5a5aL, span.getParentId().longValue());
    Assert.assertEquals(1, span.getError());
  }

}
