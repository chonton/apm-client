package org.honton.chas.datadog.apm;

import java.io.IOException;
import java.net.URI;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * Trace import for jax-rs implementations
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class TraceContainerFilter implements ContainerRequestFilter, ContainerResponseFilter {
  
  @Inject
  Tracer tracer;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    URI uri = requestContext.getUriInfo().getRequestUri();
    String resource = uri.getHost() + ':' + uri.getPort();
    String operation = requestContext.getMethod() + ':' + uri.getPath();
    tracer.importSpan(n -> requestContext.getHeaderString(n), resource, operation);
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    tracer.finishSpan();
  }
}
