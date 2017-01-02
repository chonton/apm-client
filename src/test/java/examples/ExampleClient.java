package examples;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.honton.chas.datadog.apm.TraceClientFilter;

/**
 * JaxRs Client Factory
 */
public class ExampleClient {

  /**
   * Create a JaxRs Client with registered TraceClientFilter
   */
  public Client createClient() {
    return ClientBuilder.newBuilder()
        .register(TraceClientFilter.class)
        .build();
  }
}
