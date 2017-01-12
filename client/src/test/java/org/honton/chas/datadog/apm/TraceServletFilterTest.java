package org.honton.chas.datadog.apm;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.honton.chas.datadog.apm.api.Span;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class TraceServletFilterTest {

  private Span span;
  private SpanBuilder activeInFilter;

  private void test(String clientTraceId, String clientSpanId, int statusCode) throws IOException, ServletException {

    final Tracer tracer = new Tracer() {
        @Override
        void queueSpan(Span qs) {
            span = qs;
        }
    };
    tracer.setTraceConfiguration(TraceConfigurationFactory.DEFAULTS);

    TraceServletFilter tsf = new TraceServletFilter();
    tsf.setTracer(tracer);

    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);

    Mockito.when(req.getServerName()).thenReturn("example.com");
    Mockito.when(req.getServerPort()).thenReturn(7110);
    Mockito.when(req.getMethod()).thenReturn("GET");
    Mockito.when(req.getRequestURI()).thenReturn("/some/path");
    Mockito.when(req.getScheme()).thenReturn("https");

    Mockito.when(req.getHeader(Mockito.eq(Tracer.SPAN_ID))).thenReturn(clientSpanId);
    Mockito.when(req.getHeader(Mockito.eq(Tracer.TRACE_ID))).thenReturn(clientTraceId);

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

    Assert.assertEquals("service", span.getService());
    Assert.assertEquals("example.com:7110", span.getResource());
    Assert.assertEquals("GET:/some/path", span.getOperation());
  }

  @Test
  public void testNoClientTrace() throws IOException, ServletException {
    test(null, null, 200);
    Assert.assertEquals(span.getTraceId(), span.getSpanId());
    Assert.assertNull(span.getParentId());
    Assert.assertEquals(0, span.getError());
  }

  @Test
  public void testWithClientTrace() throws IOException, ServletException {
    test("fdfd", "5a5a", 400);
    Assert.assertEquals(0xfdfdL, span.getTraceId());
    Assert.assertEquals(0x5a5aL, span.getParentId().longValue());
    Assert.assertEquals(400, span.getError());
  }

}
