package org.honton.chas.datadog.apm;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.honton.chas.datadog.apm.api.Span;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class TraceClientFilterTest {

  private Span span;

  @Test
  public void test() throws URISyntaxException, IOException {

    Tracer tracer = new Tracer("service") {
      @Override
      void queueSpan(Span qs) {
        span = qs;
      }
    };

    TraceClientFilter scf = new TraceClientFilter();
    scf.tracer = tracer;

    MultivaluedMap<String, Object> headerAccess = new MultivaluedHashMap<>();

    ClientRequestContext requestContext = Mockito.mock(ClientRequestContext.class);
    Mockito.when(requestContext.getUri()).thenReturn(new URI("https://example.com:7110/some/path"));
    Mockito.when(requestContext.getMethod()).thenReturn("GET");
    Mockito.when(requestContext.getHeaders()).thenReturn(headerAccess);
    scf.filter(requestContext);
    Assert.assertNotNull(tracer.getCurrentSpan());

    ClientResponseContext responseContext = Mockito.mock(ClientResponseContext.class);
    scf.filter(requestContext, responseContext);
    Assert.assertNull(tracer.getCurrentSpan());

    Assert.assertEquals("service", span.getService());
    Assert.assertEquals("example.com:7110", span.getResource());
    Assert.assertEquals("GET:/some/path", span.getOperation());
    Assert.assertEquals(Long.parseUnsignedLong((String)headerAccess.getFirst(Tracer.TRACE_ID), 16), span.getTraceId());
    Assert.assertEquals(Long.parseUnsignedLong((String)headerAccess.getFirst(Tracer.SPAN_ID), 16), span.getSpanId());
  }

}
