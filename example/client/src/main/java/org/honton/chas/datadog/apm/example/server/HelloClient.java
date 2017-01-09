package org.honton.chas.datadog.apm.example.server;

import javax.inject.Inject;
import javax.ws.rs.client.WebTarget;
import org.honton.chas.datadog.apm.TraceClientFilter;
import org.honton.chas.datadog.apm.example.api.Hello;
import org.honton.chas.datadog.apm.jackson.MsgPackProvider;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class HelloClient implements Hello {

  private Hello proxy;

  @Inject
  void setTraceFilter(TraceClientFilter filter) {
    ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder();
    clientBuilder.register(new MsgPackProvider());
    clientBuilder.register(filter);

    ResteasyClient client = clientBuilder.build();
    WebTarget target = client.target("http://localhost:5555");
    ResteasyWebTarget rtarget = (ResteasyWebTarget) target;

    proxy = rtarget.proxy(Hello.class);
  }

  @Override
  public String greeting() {
    return proxy.greeting();
  }
}
