package org.honton.chas.datadog.apm.example.server;

import javax.inject.Inject;
import org.honton.chas.datadog.apm.TraceClientFilter;
import org.honton.chas.datadog.apm.example.api.Hello;
import org.jglue.cdiunit.AdditionalClasspaths;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiRunner.class)
@AdditionalClasspaths({TraceClientFilter.class, HelloClient.class})
public class HelloClientTest {

  @Inject Hello client;

  @Test
  public void testInjection() {
    Assert.assertNotNull(client);
  }
}
