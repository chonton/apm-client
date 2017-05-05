package org.honton.chas.datadog.apm.jaxrs;

import org.honton.chas.datadog.apm.SpanBuilder;
import org.honton.chas.datadog.apm.TraceOperation;
import org.honton.chas.datadog.apm.Tracer;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Trace import for jaxrs implementations
 */
@Provider
@Priority(Priorities.AUTHENTICATION-1)
public class TraceContainerFilter implements ContainerRequestFilter, ContainerResponseFilter {

  private Tracer tracer;

  @Inject
  void setTracer(Tracer tracer) {
    this.tracer = tracer;
  }

  @Context
  ResourceInfo resourceInfo;

  private TraceOperation getTraceOperationAnnotation() {
    Method resourceMethod = resourceInfo.getResourceMethod();
    TraceOperation traceOperation = resourceMethod.getAnnotation(TraceOperation.class);
    if (traceOperation != null) {
      return traceOperation;
    }
    Class<?> resourceClass = resourceInfo.getResourceClass();
    return resourceClass.getAnnotation(TraceOperation.class);
  }

  @Override
  public void filter(final ContainerRequestContext req) throws IOException {
    TraceOperation traceOperation = getTraceOperationAnnotation();
    if (traceOperation == null || traceOperation.value()) {
      SpanBuilder sb = tracer.importSpan(new Tracer.HeaderAccessor() {
        @Override
        public String getValue(String name) {
          return req.getHeaderString(name);
        }
      });

      sb.resource(resourceInfo.getResourceClass().getSimpleName())
          .operation(resourceInfo.getResourceMethod().getName());

      sb.type(traceOperation != null && !traceOperation.type().isEmpty() ?traceOperation.type() :TraceOperation.WEB);
    }
  }

  @Override
  public void filter(ContainerRequestContext req, ContainerResponseContext resp) throws IOException {
    TraceOperation traceOperation = getTraceOperationAnnotation();
    if (traceOperation == null || traceOperation.value()) {
      SpanBuilder currentSpan = tracer.getCurrentSpan();
      int status = resp.getStatus();
      if (status < 200 || status >= 400) {
        currentSpan.error(true);
      }
      tracer.closeSpan(currentSpan);
    }
  }
}
