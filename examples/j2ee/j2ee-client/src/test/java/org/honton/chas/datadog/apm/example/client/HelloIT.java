package org.honton.chas.datadog.apm.example.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.honton.chas.datadog.apm.api.Span;
import org.honton.chas.datadog.apm.example.api.Hello;
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
public class HelloIT {

  private static final int APM_PORT = 8126;
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final TypeReference<List<List<Span>>> LIST_LIST_SPAN =
    new TypeReference<List<List<Span>>>() {};

  @Inject
  private ProxyFactory proxyFactory;

  Hello getHello() {
    return proxyFactory.getProxy("http://localhost:5555", Hello.class);
  }

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


  private List<Span> requestsToSpans(HttpRequest[] requests) throws IOException {
    List<Span> spans = new ArrayList<>();
    for (HttpRequest request : requests) {
      String body = request.getBodyAsString();
      List<List<Span>> traces = MAPPER.readValue(body, LIST_LIST_SPAN);
      for(List<Span> trace : traces) {
        for(Span span : trace) {
          spans.add(span);
        }
      }
    }
    return spans;
  }

  private List<Span> getSpans(int count) throws InterruptedException, IOException {
    HttpRequest v2traces = HttpRequest.request().withMethod("PUT").withPath("/v0.2/traces");
    // wait for server to deliver traces
    for (long expire = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10); System.currentTimeMillis() < expire; Thread.sleep(200)) {
      HttpRequest[] requests = new MockServerClient("localhost", APM_PORT).retrieveRecordedRequests(v2traces);
      List<Span> spans = requestsToSpans(requests);
      if (spans.size() == count) {
        return spans;
      }
    }
    throw new AssertionError("Did not get " + count + " spans within 10 seconds");
  }

  @After
  public void verifyMockWasCalled() throws InterruptedException, IOException {
    List<Span> spans = getSpans(3);

    Span intercepted = null;
    Span client = null;
    Span server = null;

    for(Span span : spans) {
      if(span.getOperation().equals("kr")) {
        intercepted = span;
      }
      else if( span.getService().equals("greetings-server")){
        server = span;
      } else if( span.getService().equals("greetings-client")){
        client = span;
      }
    }

    Assert.assertEquals("CS:localhost:5555", client.getResource());
    Assert.assertEquals("GET:/greetings", client.getOperation());
    Assert.assertEquals(client.getTraceId(), client.getSpanId());
    Assert.assertNull(client.getParentId());

    Assert.assertEquals("SR:localhost:5555", server.getResource());
    Assert.assertEquals("GET:/greetings", server.getOperation());
    Assert.assertEquals(client.getTraceId(), server.getTraceId());
    Assert.assertEquals(client.getSpanId(), (long)server.getParentId());

    Assert.assertEquals("greetings-server", intercepted.getService());
    Assert.assertEquals("org.honton.chas.datadog.apm.example.server.Greeting", intercepted.getResource());
    Assert.assertEquals(server.getTraceId(), intercepted.getTraceId());
    Assert.assertEquals(server.getSpanId(), (long)intercepted.getParentId());
  }

  @Test
  public void testCallService() {
    Assert.assertEquals("Hello World!", getHello().greeting());
  }
}
