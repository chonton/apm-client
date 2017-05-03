package org.honton.chas.datadog.apm.jaxrs;

import org.honton.chas.datadog.apm.SpanBuilder;
import org.honton.chas.datadog.apm.TraceOperation;
import org.honton.chas.datadog.apm.Tracer;

import javax.inject.Inject;
import javax.ws.rs.container.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;

/**
 * Trace import for jaxrs implementations
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
    Method resourceMethod = resourceInfo.getResourceMethod();
    TraceOperation traceOperation = resourceMethod.getAnnotation(TraceOperation.class);
    if (traceOperation == null) {
      Class<?> resourceClass = resourceInfo.getResourceClass();
      traceOperation = resourceClass.getAnnotation(TraceOperation.class);
      if (traceOperation == null) {
        return true;
      }
    }
    return traceOperation.value();
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
      sb.resource(req.getMethod() + ' ' + uriInfo.getPath())
          .operation(uri.getHost() + ':' + uri.getPort());
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
