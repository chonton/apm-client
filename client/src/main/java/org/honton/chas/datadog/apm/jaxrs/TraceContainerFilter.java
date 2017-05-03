package org.honton.chas.datadog.apm.jaxrs;

import java.io.IOException;
import java.net.URI;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import org.honton.chas.datadog.apm.SpanBuilder;
import org.honton.chas.datadog.apm.TraceOperation;
import org.honton.chas.datadog.apm.Tracer;

/**
 * Trace export for jaxrs implementations
 */
@Provider
public class TraceContainerFilter implements ContainerRequestFilter, ContainerResponseFilter {

  private Tracer tracer;

  @Inject
  void setTracer(Tracer tracer) {
    this.tracer = tracer;
  }

  @Context
  ResourceInfo resourceInfo;

  private boolean shouldTrace() {
    TraceOperation traceOperation = resourceInfo.getResourceMethod().getAnnotation(TraceOperation.class);
    return traceOperation == null || traceOperation.value();
  }

  @Override
  public void filter(final ContainerRequestContext req) throws IOException {
    if (shouldTrace()) {
      SpanBuilder sb = tracer.importSpan(new Tracer.HeaderAccessor() {
        @Override public String getValue(String name) {
          return req.getHeaderString(name);
        }
      });

      UriInfo uriInfo = req.getUriInfo();
      URI uri = uriInfo.getRequestUri();
      sb.resource("SR:" + uri.getHost() + ':' + uri.getPort())
        .operation(req.getMethod() + ':' + uriInfo.getPath());
    }
  }

  @Override
  public void filter(ContainerRequestContext req, ContainerResponseContext resp) throws IOException {
    if (shouldTrace()) {
      SpanBuilder currentSpan = tracer.getCurrentSpan();
      int status = resp.getStatus();
      if (status < 200 || status >= 400) {
        currentSpan.error(true);
      }
      tracer.closeSpan(currentSpan);
    }
  }
}
