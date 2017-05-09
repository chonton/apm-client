package org.honton.chas.datadog.apm.cdi;

import org.honton.chas.datadog.apm.SpanBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class TracerThreadTest {

  private TracerTestImpl tracer;
  private SpanBuilder rootBuilder;

  @Before
  public void setupTracer() {
    tracer = new TracerTestImpl();
  }

  private void testImport(SpanBuilder.SpanContext ctx) {
    SpanBuilder currentSpan = tracer.importCurrentSpan(ctx, "ExportedResource", "exportedResource");
    Assert.assertEquals(rootBuilder.traceId(), currentSpan.traceId());
    Assert.assertEquals(rootBuilder.spanId(), currentSpan.parentId().longValue());

    tracer.closeCurrentSpan();
    Assert.assertEquals(currentSpan.spanId(), tracer.getCapturedSpan().getSpanId());
  }

  @Test
  public void testExport() throws Exception {
    rootBuilder = tracer.createSpan().resource("ImportedResource").operation("importedResource");

    final SpanBuilder.SpanContext ctx= tracer.exportCurrentSpan();
    Thread t = new Thread() {
      @Override
      public void run() {
        testImport(ctx);
      }
    };
    t.start();
    t.join(TimeUnit.SECONDS.toMillis(30));
    Assert.assertFalse(t.isAlive());

    tracer.closeCurrentSpan();
    Assert.assertEquals(rootBuilder.spanId(), tracer.getCapturedSpan().getSpanId());
  }
}
