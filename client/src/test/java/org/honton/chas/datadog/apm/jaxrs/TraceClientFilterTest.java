package org.honton.chas.datadog.apm.jaxrs;

import org.honton.chas.datadog.apm.api.Span;
import org.honton.chas.datadog.apm.cdi.TracerImpl;
import org.honton.chas.datadog.apm.cdi.TracerTestImpl;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.lang.annotation.Annotation;
import java.net.URI;

public class TraceClientFilterTest {

  @Test
  public void test() throws Exception {

    TracerTestImpl tracer = new TracerTestImpl();

    TraceClientFilter tcf = new TraceClientFilter();
    tcf.setTracer(tracer);

    MultivaluedMap<String, Object> headerAccess = new MultivaluedHashMap<>();

    ClientRequestContext requestContext = Mockito.mock(ClientRequestContext.class);
    Mockito.when(requestContext.getUri()).thenReturn(new URI("https://example.com:7110/some/path"));
    Mockito.when(requestContext.getMethod()).thenReturn("GET");
    Mockito.when(requestContext.getHeaders()).thenReturn(headerAccess);
    Mockito.when(requestContext.getEntityAnnotations()).thenReturn(new Annotation[]{});
    tcf.filter(requestContext);
    Assert.assertNotNull(tracer.getCurrentSpan());

    ClientResponseContext responseContext = Mockito.mock(ClientResponseContext.class);
    tcf.filter(requestContext, responseContext);
    Assert.assertNull(tracer.getCurrentSpan());

    Span span = tracer.getCapturedSpan();
    Assert.assertEquals("service", span.getService());
    Assert.assertEquals("example.com:7110", span.getResource());
    Assert.assertEquals("GET /some/path", span.getOperation());
    Assert.assertEquals(Long.parseUnsignedLong((String)headerAccess.getFirst(TracerImpl.TRACE_ID)), span.getTraceId());
    Assert.assertEquals(Long.parseUnsignedLong((String)headerAccess.getFirst(TracerImpl.SPAN_ID)), span.getSpanId());
  }

}
