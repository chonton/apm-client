package org.honton.chas.datadog.apm;

import java.io.IOException;
import java.net.URI;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import org.honton.chas.datadog.apm.Tracer.HeaderMutator;

/**
 * Trace export for jaxrs implementations
 */
@ApplicationScoped public class TraceClientFilter
  implements ClientRequestFilter, ClientResponseFilter {

  @Inject Tracer tracer;

  @Override public void filter(final ClientRequestContext requestContext) throws IOException {
    URI uri = requestContext.getUri();
    tracer.exportSpan(uri.getHost() + ':' + uri.getPort(),
    requestContext.getMethod() + ':' + uri.getPath().toLowerCase(),
      new HeaderMutator() {
        @Override public void setValue(String name, String value) {
          requestContext.getHeaders().putSingle(name, value);
        }
      });
  }

  @Override
  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
    throws IOException {
    tracer.closeCurrentSpan();
  }
}
