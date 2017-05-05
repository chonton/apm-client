package org.honton.chas.datadog.apm.jaxrs;

import org.honton.chas.datadog.apm.Tracer;

import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;

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

  public void filter(final ClientRequestContext req) throws IOException {
    URI uri = req.getUri();
    tracer.exportSpan(uri.getHost() + ':' + uri.getPort(), req.getMethod() + ' ' + uri.getPath(),
        new Tracer.HeaderMutator() {
        @Override
        public void setValue(String name, String value) {
          req.getHeaders().putSingle(name, value);
        }
      });
  }

  @Override
  public void filter(ClientRequestContext req, ClientResponseContext resp) throws IOException {
    int status = resp.getStatus();
    tracer.getCurrentSpan().error(status < 200 || status >= 400);
    tracer.closeCurrentSpan();
  }
}
