package org.honton.chas.datadog.apm.example.integration;

import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.honton.chas.datadog.apm.TraceClientFilter;
import org.honton.chas.datadog.apm.example.api.Hello;
import org.honton.chas.datadog.apm.example.server.HelloClient;
import org.jglue.cdiunit.AdditionalClasspaths;
import org.jglue.cdiunit.CdiRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.matchers.TimeToLive;
import org.mockserver.matchers.Times;
import org.mockserver.model.Delay;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

@RunWith(CdiRunner.class)
@AdditionalClasspaths({TraceClientFilter.class, HelloClient.class, TraceConfigurationFactory.class})
public class HelloIT {

  private static final int APM_PORT = 7777;

  @Inject
  Hello client;

  @Before
  public void emulateV0_2() {
    MockServerClient client = new MockServerClient("localhost", APM_PORT)
      .reset();

    client
      .when(HttpRequest.request().withMethod("PUT").withPath("/v0.3/traces"),
          Times.unlimited(), TimeToLive.exactly(TimeUnit.MINUTES, 1l))
      .respond(HttpResponse.response().withStatusCode(404)
        .withHeaders(new Header("Content-Type", "text/plain"))
    );

    client
      .when(HttpRequest.request().withMethod("PUT").withPath("/v0.2/traces"),
        Times.unlimited(), TimeToLive.exactly(TimeUnit.MINUTES, 1l))
      .respond(HttpResponse.response().withStatusCode(200)
        .withHeaders(new Header("Content-Type", "text/plain")).withBody("OK\n")
        .withDelay(new Delay(TimeUnit.MILLISECONDS, 20)));
  }

  @After
  public void verifyMockWasCalled() {
    HttpRequest[] requests = new MockServerClient("localhost", APM_PORT).retrieveRecordedRequests(null);
    for(HttpRequest request : requests) {
      System.out.println(request);
    }
  }

  @Test
  public void testCallService() {
    Assert.assertEquals("Hello World!", client.greeting());
  }
}
