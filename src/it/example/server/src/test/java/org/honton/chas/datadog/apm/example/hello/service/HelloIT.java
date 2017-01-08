package org.honton.chas.datadog.apm.example.hello.service;

import org.honton.chas.datadog.apm.example.hello.Hello;
import org.honton.chas.datadog.apm.example.hello.HelloClient;
import org.junit.Test;

import org.junit.Assert;

public class HelloIT {

  @Test
  public void testCallService() {
    Hello client = new HelloClient();
    Assert.assertEquals("Hello World!", client.greeting());
  }
}
