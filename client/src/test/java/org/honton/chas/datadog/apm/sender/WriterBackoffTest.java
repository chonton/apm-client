package org.honton.chas.datadog.apm.sender;

import java.util.concurrent.TimeUnit;

import org.honton.chas.datadog.apm.SpanBuilderTest;
import org.honton.chas.datadog.apm.TraceConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.TimeToLive;
import org.mockserver.matchers.Times;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.verify.VerificationTimes;

public class WriterBackoffTest {

  private ClientAndServer mockServer;

  @After
  public void stopServer() {
    if(mockServer != null) {
      mockServer.stop();
    }
  }

  private void startServer() {
    mockServer = ClientAndServer.startClientAndServer(7755);

    MockServerClient client = new MockServerClient("localhost", 7755)
      .reset();

    client.when(HttpRequest.request()
          .withMethod("PUT")
          .withPath("/v0.3/traces")
          .withHeaders(new Header("Content-Type", "application/msgpack")),
          Times.exactly(1), TimeToLive.exactly(TimeUnit.MINUTES, 1l))
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
          VerificationTimes.exactly(1));
  }

  private Writer startWriter() throws InterruptedException {
    Writer writer = new Writer();
    writer.setTraceConfiguration(new TraceConfiguration("service", "http://localhost:7755", 100));
    writer.initialize();
    return writer;
  }

  @Test
  public void testBackoff() throws InterruptedException {
    Writer writer = startWriter();
    Assert.assertEquals(0, writer.backoffExpiration);

    writer.queue(SpanBuilderTest.getTestSpan());
    Thread.sleep(200);
    Assert.assertNotEquals(0, writer.backoffExpiration);

    startServer();
    writer.queue(SpanBuilderTest.getTestSpan());
    Thread.sleep(100);
    verifications();
  }
}
