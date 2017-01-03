package org.honton.chas.datadog.apm.sender;

import java.util.concurrent.TimeUnit;

import org.honton.chas.datadog.apm.SpanBuilderTest;
import org.honton.chas.datadog.apm.TraceConfiguration;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.TimeToLive;
import org.mockserver.matchers.Times;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.verify.VerificationTimes;

public class WriterTest {

  private static ClientAndServer mockServer;

  @BeforeClass
  public static void startServer() {
    mockServer = ClientAndServer.startClientAndServer(7755);
  }

  @AfterClass
  public static void stopServer() {
    mockServer.stop();
  }

  private void expectations(int status) {
    MockServerClient client = new MockServerClient("localhost", 7755)
      .reset();

    client.when(HttpRequest.request()
          .withMethod("PUT")
          .withPath("/v0.3/traces")
          .withHeaders(new Header("Content-Type", "application/msgpack")),
          Times.exactly(1), TimeToLive.exactly(TimeUnit.MINUTES, 1l))
      .respond(HttpResponse.response()
          .withStatusCode(status)
          .withHeaders(new Header("Content-Type", "text/plain"))
      );
    client.when(HttpRequest.request()
          .withMethod("PUT")
          .withPath("/v0.2/traces")
          .withHeaders(new Header("Content-Type", "application/json")),
          Times.exactly(1),
          TimeToLive.exactly(TimeUnit.MINUTES, 1l))
      .respond(HttpResponse.response()
          .withStatusCode(200)
          .withHeaders(new Header("Content-Type", "text/plain"))
          .withBody("OK\n")
      );
  }

  private void verifications() throws AssertionError {
    new MockServerClient("localhost", 7755)
    .verify(HttpRequest.request()
          .withMethod("PUT")
          .withPath("/v0.3/traces")
          .withHeaders(new Header("Content-Type", "application/msgpack")),
          VerificationTimes.exactly(1))
    .verify(HttpRequest.request()
          .withMethod("PUT")
          .withPath("/v0.2/traces")
          .withHeaders(new Header("Content-Type", "application/json")),
          VerificationTimes.exactly(1));
  }

  private Writer startWriter(String url) throws InterruptedException {
    Writer writer = new Writer();
    writer.setTraceConfiguration(new TraceConfiguration("service", "http://localhost:7755", TimeUnit.MINUTES.toMillis(1)));
    writer.initialize();

    writer.queue(SpanBuilderTest.getTestSpan());
    // wait for queuing ...
    Thread.sleep(200);
    writer.stop();
    return writer;
  }

  private void testFallback(int status) throws InterruptedException {
    expectations(status);
    startWriter("http://localhost:7755");
    verifications();
  }

  @Test
  public void testFallback404() throws InterruptedException {
    testFallback(404);
  }

  @Test
  public void testFallback415() throws InterruptedException {
    testFallback(415);
  }

  @Test
  public void testNoCollector() throws InterruptedException {
    Writer writer = startWriter("http://localhost:1");
    Assert.assertFalse(writer.isStopped());
    writer.queue(SpanBuilderTest.getTestSpan());
    Thread.sleep(200);
    Assert.assertTrue(writer.isStopped());
  }
}
