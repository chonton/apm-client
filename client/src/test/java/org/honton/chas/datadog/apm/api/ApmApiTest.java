package org.honton.chas.datadog.apm.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.WebTarget;
import org.honton.chas.datadog.apm.SpanBuilderTest;
import org.honton.chas.datadog.apm.jackson.MsgPackProvider;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.TimeToLive;
import org.mockserver.matchers.Times;
import org.mockserver.model.Delay;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

public class ApmApiTest {

  private ClientAndServer mockServer;

  @Before
  public void startServer() {
    mockServer = ClientAndServer.startClientAndServer(7755);

    new MockServerClient("localhost", 7755)
        .when(HttpRequest.request().withMethod("PUT"), 
            Times.exactly(1),
            TimeToLive.exactly(TimeUnit.MINUTES, 1l))
        .respond(HttpResponse.response().withStatusCode(200)
            .withHeaders(new Header("Content-Type", "text/plain")).withBody("OK\n")
            .withDelay(new Delay(TimeUnit.SECONDS, 1)));
  }

  @After
  public void stopServer() {
    mockServer.stop();
  }

  private <T> T getProxy(Class<T> proxyType) {
    ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder();
    clientBuilder.register(new MsgPackProvider());

    ResteasyClient client = clientBuilder.build();
    WebTarget target = client.target("http://localhost:7755");
    ResteasyWebTarget rtarget = (ResteasyWebTarget) target;

    return rtarget.proxy(proxyType);
  }

  @Test
  public void testService0_2() {
    ApmApi0_2 apmApi = getProxy(ApmApi0_2.class);

    Map<String, ServiceData> apps = new HashMap<>();
    apps.put("tw-name", new ServiceData("tw", "web"));
    apps.put("td-name", new ServiceData("td", "database"));
    String serviceOK = apmApi.reportServices(apps);
    Assert.assertEquals(ApmApi.SUCCESS, serviceOK);
  }

  @Test
  public void testSpans0_2() {
    ApmApi0_2 apmApi = getProxy(ApmApi0_2.class);
    String spanOK = apmApi.reportTraces(SpanBuilderTest.getTestTraces("0_2"));
    Assert.assertEquals(ApmApi.SUCCESS, spanOK);
  }

  @Test
  public void testSpans0_3() {
    ApmApi0_3 apmApi = getProxy(ApmApi0_3.class);
    String spanOK = apmApi.reportTraces(SpanBuilderTest.getTestTraces("0_3"));
    Assert.assertEquals(ApmApi.SUCCESS, spanOK);
  }
}
