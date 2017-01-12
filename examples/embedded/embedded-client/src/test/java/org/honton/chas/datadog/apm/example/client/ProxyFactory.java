package org.honton.chas.datadog.apm.example.client;

import javax.inject.Inject;
import org.honton.chas.datadog.apm.TraceClientFilter;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

public class ProxyFactory {

  private ResteasyClientBuilder clientBuilder;

  @Inject
  void setTraceFilter(TraceClientFilter filter) {
    clientBuilder = new ResteasyClientBuilder();
    clientBuilder.register(filter);
  }

  public <T> T getProxy(String url, Class<T> cls) {
    return clientBuilder.build().target(url).proxy(cls);
  }
}
