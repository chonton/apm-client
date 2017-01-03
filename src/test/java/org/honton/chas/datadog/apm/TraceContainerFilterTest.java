package org.honton.chas.datadog.apm;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.UriInfo;

import org.honton.chas.datadog.apm.api.Span;
import org.junit.Test;
import org.mockito.Mockito;

import org.junit.Assert;

public class TraceContainerFilterTest {

  private Span span;

  private void test(String clientTraceId, String clientSpanId) throws IOException, URISyntaxException {

    Tracer tracer = new Tracer() {
      @Override
      void queueSpan(Span qs) {
        span = qs;
      }
    };
    tracer.setTraceConfiguration(TraceConfiguration.getDefault());

    TraceContainerFilter scf = new TraceContainerFilter();
    scf.tracer = tracer;

    UriInfo uriInfo = Mockito.mock(UriInfo.class);
    Mockito.when(uriInfo.getRequestUri()).thenReturn(new URI("https://example.com:7110/some/path"));

    ContainerRequestContext requestContext = Mockito.mock(ContainerRequestContext.class);
    Mockito.when(requestContext.getUriInfo()).thenReturn(uriInfo);
    Mockito.when(requestContext.getMethod()).thenReturn("GET");
    Mockito.when(requestContext.getHeaderString(Mockito.eq(Tracer.SPAN_ID))).thenReturn(clientSpanId);
    Mockito.when(requestContext.getHeaderString(Mockito.eq(Tracer.TRACE_ID))).thenReturn(clientTraceId);
    scf.filter(requestContext);
    Assert.assertNotNull(tracer.getCurrentSpan());

    ContainerResponseContext responseContext = Mockito.mock(ContainerResponseContext.class);
    scf.filter(requestContext, responseContext);
    Assert.assertNull(tracer.getCurrentSpan());

    Assert.assertEquals("service", span.getService());
    Assert.assertEquals("example.com:7110", span.getResource());
    Assert.assertEquals("GET:/some/path", span.getOperation());
  }

  @Test
  public void testNoClientTrace() throws IOException, URISyntaxException {
    test(null, null);
    Assert.assertEquals(span.getTraceId(), span.getSpanId());
    Assert.assertNull(span.getParentId());
  }

  @Test
  public void testWithClientTrace() throws IOException, URISyntaxException {
    test("fdfd", "5a5a");
    Assert.assertEquals(0xfdfdL, span.getTraceId());
    Assert.assertEquals(0x5a5aL, span.getParentId().longValue());
  }

}
