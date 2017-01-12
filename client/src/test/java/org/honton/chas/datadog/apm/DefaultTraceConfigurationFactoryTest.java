package org.honton.chas.datadog.apm;

import java.util.concurrent.TimeUnit;
import org.junit.Assert;

/**
 * Test the default configuration
 */
//@RunWith(CdiRunner.class)
//AdditionalClasspaths(Tracer.class)
public class DefaultTraceConfigurationFactoryTest {

    //@Inject
    TraceConfiguration defaults;

    //@Test
    public void testDefaults() {
        Assert.assertEquals("http://localhost:7777", defaults.getCollectorUrl());
        Assert.assertEquals("service", defaults.getService());
        Assert.assertEquals(TimeUnit.MINUTES.toMillis(15), defaults.getBackoffDuration());
    }
}