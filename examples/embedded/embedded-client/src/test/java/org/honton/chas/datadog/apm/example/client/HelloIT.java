package org.honton.chas.datadog.apm.example.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(CdiRunner.class)
public class HelloIT {

  private static final int APM_PORT = 8126;

  @Inject
  private ProxyFactory proxyFactory;

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final TypeReference<List<List<Span>>> LIST_LIST_SPAN =
      new TypeReference<List<List<Span>>>() {
      };

  Hello getHello() {
    return proxyFactory.getProxy("http://localhost:5555", Hello.class);
  }

  @Before
  public void emulateV0_2() {
    MockServerClient client = new MockServerClient("localhost", APM_PORT).reset();

    client.when(
        HttpRequest.request()
            .withMethod("PUT")
            .withPath("/v0.3/traces"), Times.unlimited(), TimeToLive.exactly(TimeUnit.MINUTES, 1l))
        .respond(HttpResponse.response()
            .withStatusCode(404)
            .withHeaders(new Header("Content-Type", "text/plain")));

    client.when(
        HttpRequest.request()
            .withMethod("PUT")
            .withPath("/v0.2/traces"), Times.unlimited(), TimeToLive.exactly(TimeUnit.MINUTES, 1l))
        .respond(HttpResponse.response()
            .withStatusCode(200)
            .withHeaders(new Header("Content-Type", "text/plain"))
            .withBody("OK\n").withDelay(new Delay(TimeUnit.MILLISECONDS, 20)));
  }

  private List<Span> requestsToSpans(HttpRequest[] requests) throws IOException {
    List<Span> spans = new ArrayList<>();
    for (HttpRequest request : requests) {
      String body = request.getBodyAsString();
      List<List<Span>> traces = MAPPER.readValue(body, LIST_LIST_SPAN);
      for (List<Span> trace : traces) {
        for (Span span : trace) {
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
    List<Span> spans = getSpans(4);
    Assert.assertEquals(4, spans.size());

    Span echo = null;
    Span intercepted = null;
    Span client = null;
    Span server = null;
    /*
    Span(service=greetings-server, resource=Greeting, operation=kr, type=unknown)
    Span(service=greetings-server, resource=HelloService, operation=greeting, type=unknown)
    Span(service=greetings-client, resource=localhost:5555, operation=GET /greetings, type=web)
    Span(service=greetings-client, resource=localhost:5555, operation=GET /echo, type=web)
     */
    for (Span span : spans) {
      if (span.getService().equals("greetings-server")) {
        if (span.getResource().equals("HelloService")) {
          server = span;
        } else {
          intercepted = span;
        }
      } else if (span.getService().equals("greetings-client")) {
        if (span.getOperation().equals("GET /echo")) {
          echo = span;
        } else {
          client = span;
        }
      }
    }

    Assert.assertEquals("localhost:5555", echo.getResource());
    Assert.assertEquals("GET /echo", echo.getOperation());
    Assert.assertNotNull(echo.getSpanId());
    Assert.assertNull(echo.getParentId());

    Assert.assertEquals("localhost:5555", client.getResource());
    Assert.assertEquals("GET /greetings", client.getOperation());
    Assert.assertNotNull(client.getTraceId());
    Assert.assertNotNull(client.getSpanId());
    Assert.assertNull(client.getParentId());

    Assert.assertEquals("HelloService", server.getResource());
    Assert.assertEquals("greeting", server.getOperation());
    Assert.assertEquals(client.getTraceId(), server.getTraceId());
    Assert.assertEquals(client.getSpanId(), (long) server.getParentId());

    Assert.assertEquals("greetings-server", intercepted.getService());
    Assert.assertEquals("Greeting", intercepted.getResource());
    Assert.assertEquals("kr", intercepted.getOperation());
    Assert.assertEquals(server.getTraceId(), intercepted.getTraceId());
    Assert.assertEquals(server.getSpanId(), (long) intercepted.getParentId());
  }

  @Test
  public void testCallService() {
    final Hello hello = getHello();
    Assert.assertNotNull(hello);
    Assert.assertEquals("Hello World!", hello.greeting());
    Assert.assertEquals("kjh", hello.echo("kjh"));
  }
}
