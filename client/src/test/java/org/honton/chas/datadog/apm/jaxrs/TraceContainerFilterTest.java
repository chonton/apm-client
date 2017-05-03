package org.honton.chas.datadog.apm.jaxrs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.UriInfo;
import org.honton.chas.datadog.apm.api.Span;
import org.honton.chas.datadog.apm.cdi.TracerImpl;
import org.honton.chas.datadog.apm.cdi.TracerTestImpl;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Testing TraceContainerFilter
 */
public class TraceContainerFilterTest {

  public Span test(String clientTraceId, String clientSpanId, int statusCode) throws Exception {

    TracerTestImpl tracer = new TracerTestImpl();

    ResourceInfo resourceInfo  = Mockito.mock(ResourceInfo.class);
    Method method = TraceContainerFilterTest.class.getMethod("testNoClientTrace");
    Mockito.when(resourceInfo.getResourceMethod()).thenReturn(method);
    Mockito.when(resourceInfo.getResourceClass()).thenReturn((Class)method.getDeclaringClass());

    TraceContainerFilter scf = new TraceContainerFilter();
    scf.setTracer(tracer);
    scf.resourceInfo = resourceInfo;

    UriInfo uriInfo  = Mockito.mock(UriInfo.class);
    Mockito.when(uriInfo.getRequestUri()).thenReturn(new URI("https://example.com:7110/some/path"));
    Mockito.when(uriInfo.getPath()).thenReturn("/path");

    ContainerRequestContext requestContext = Mockito.mock(ContainerRequestContext.class);
    Mockito.when(requestContext.getUriInfo()).thenReturn(uriInfo);
    Mockito.when(requestContext.getMethod()).thenReturn("GET");

    Mockito.when(requestContext.getHeaderString(Mockito.eq(TracerImpl.SPAN_ID))).thenReturn(clientSpanId);
    Mockito.when(requestContext.getHeaderString(Mockito.eq(TracerImpl.TRACE_ID))).thenReturn(clientTraceId);

    scf.filter(requestContext);
    Assert.assertNotNull(tracer.getCurrentSpan());

    ContainerResponseContext responseContext = Mockito.mock(ContainerResponseContext.class);
    Mockito.when(responseContext.getEntityAnnotations()).thenReturn(new Annotation[]{});
    Mockito.when(responseContext.getStatus()).thenReturn(statusCode);

    scf.filter(requestContext, responseContext);
    Assert.assertNull(tracer.getCurrentSpan());

    Span span = tracer.getCapturedSpan();
    Assert.assertEquals("service", span.getService());
    Assert.assertEquals("GET /path", span.getResource());
    Assert.assertEquals("example.com:7110", span.getOperation());
    return span;
  }

  @Test
  public void testNoClientTrace() throws Exception {
    Span span = test(null, null, 200);
    Assert.assertNull(span.getParentId());
    Assert.assertEquals(0, span.getError());
  }

  @Test
  public void testWithClientTrace() throws Exception {
    Span span = test("fdfd", "5a5a", 400);
    Assert.assertEquals(0xfdfdL, span.getTraceId());
    Assert.assertEquals(0x5a5aL, span.getParentId().longValue());
    Assert.assertEquals(1, span.getError());
  }
}
