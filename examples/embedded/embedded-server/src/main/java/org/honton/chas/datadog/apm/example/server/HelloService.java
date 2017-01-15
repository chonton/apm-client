package org.honton.chas.datadog.apm.example.server;

import javax.inject.Inject;
import org.honton.chas.datadog.apm.example.api.Hello;

public class HelloService implements Hello {

  @Inject
  private Greeting greeting;

  @Override
  public String greeting() {
    return greeting.kr();
  }
}
