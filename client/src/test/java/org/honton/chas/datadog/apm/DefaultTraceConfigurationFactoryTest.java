package org.honton.chas.datadog.apm;

import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.jglue.cdiunit.AdditionalClasspaths;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the default configuration
 */
@RunWith(CdiRunner.class)
@AdditionalClasspaths(Tracer.class)
public class DefaultTraceConfigurationFactoryTest {

    @Inject
    TraceConfiguration defaults;

    @Test
    public void testDefaults() {
        Assert.assertEquals("http://localhost:7777", defaults.getCollectorUrl());
        Assert.assertEquals("service", defaults.getService());
        Assert.assertEquals(TimeUnit.MINUTES.toMillis(15), defaults.getBackoffDuration());
    }
}