package org.honton.chas.datadog.apm.jaxrs;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import org.honton.chas.datadog.apm.TraceOperation;
import org.honton.chas.datadog.apm.Tracer;

/**
 * Trace export for jaxrs implementations
 */
@Provider
public class TraceClientFilter implements ClientRequestFilter, ClientResponseFilter {

  private Tracer tracer;

  @Inject
  void setTracer(Tracer tracer) {
    this.tracer = tracer;
  }

  @Context
  ResourceInfo resourceInfo;

  private boolean shouldTrace() {
    Class cls = resourceInfo.getResourceClass();
    Method method = resourceInfo.getResourceMethod();
    if(method == null) {
      return true;
    }
    TraceOperation traceOperation = method.getAnnotation(TraceOperation.class);
    return traceOperation == null || traceOperation.value();
  }

  @Override
  public void filter(final ClientRequestContext req) throws IOException {
    if (shouldTrace()) {
      URI uri = req.getUri();
      tracer.exportSpan("CS:" + uri.getHost() + ':' + uri.getPort(),
        req.getMethod() + ':' + uri.getPath().toLowerCase(), new Tracer.HeaderMutator() {
          @Override public void setValue(String name, String value) {
            req.getHeaders().putSingle(name, value);
          }
        });
    }
  }

  @Override
  public void filter(ClientRequestContext req, ClientResponseContext resp) throws IOException {
    if (shouldTrace()) {
      int status = resp.getStatus();
      tracer.getCurrentSpan().error(status < 200 || status >= 400);
      tracer.closeCurrentSpan();
    }
  }
}
