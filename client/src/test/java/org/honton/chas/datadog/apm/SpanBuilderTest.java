package org.honton.chas.datadog.apm;

import java.util.Collections;
import java.util.Map;
import org.honton.chas.datadog.apm.api.Span;
import org.honton.chas.datadog.apm.api.Trace;
import org.junit.Assert;
import org.junit.Test;

public class SpanBuilderTest {

  public static Trace[] getTestTraces(String suffix) {
    SpanBuilder root = SpanBuilder.createRoot();
    root.operation("root_operation_" + suffix).resource("root-resource_" + suffix).type(TraceOperation.WEB);

    SpanBuilder child = root.createChild();
    child.operation("child_operation_" + suffix).resource("child_resource_" + suffix).type(TraceOperation.SQL);
    Span childSpan = child.finishSpan("test-service");

    Span rootSpan = root.finishSpan("test-service");
    Trace trace = new Trace(childSpan, rootSpan);
    return new Trace[]{trace};
  }

  public static Span getTestSpan() {
    return SpanBuilder.createRoot().finishSpan("service");
  }

  @Test
  public void testRootChildRelations() {
    SpanBuilder rootBuilder = SpanBuilder.createRoot();
    Assert.assertNull(rootBuilder.parent());
    Assert.assertNull(rootBuilder.parentId());

    SpanBuilder childBuilder = rootBuilder.createChild();
    Assert.assertSame(rootBuilder, childBuilder.parent());
    
    Span childSpan = childBuilder.finishSpan("service");
    Span rootSpan = rootBuilder.finishSpan("service");

    Assert.assertNull(rootSpan.getParentId());
    Assert.assertNotNull(rootSpan.getTraceId());
    Assert.assertNotNull(rootSpan.getSpanId());

    Assert.assertEquals(rootSpan.getSpanId(), childSpan.getParentId().longValue());
    Assert.assertEquals(rootSpan.getTraceId(), childSpan.getTraceId());
    Assert.assertNotEquals(childSpan.getTraceId(), childSpan.getSpanId());

    Assert.assertTrue(rootSpan.getStart() <= childSpan.getStart());
    Assert.assertTrue(rootSpan.getDuration() >= childSpan.getDuration());
  }

  @Test
  public void testMeta() {
    SpanBuilder builder = SpanBuilder.createRoot();
    builder.meta("key", "value");
    Span span = builder.finishSpan("service");

    Assert.assertEquals(Collections.singletonMap("key", "value"), span.getMeta());
    builder.meta("another", "should not appear");
    Assert.assertNull(span.getMeta().get("another"));
  }

  @Test
  public void testMetrics() {
    SpanBuilder builder = SpanBuilder.createRoot();
    builder.metric("columbus", 1492);
    Span span = builder.finishSpan("service");

    Assert.assertEquals(Collections.singletonMap("columbus", 1492), span.getMetrics());
    builder.metric("another", Math.PI);
    Assert.assertNull(span.getMetrics().get("another"));
  }

  @Test
  public void testSetterGetter() {
    SpanBuilder builder = SpanBuilder.createRoot();
    Assert.assertEquals(0, builder.error());
    Assert.assertEquals(1, builder.error(true).error());

    Assert.assertNull(builder.resource());
    Assert.assertEquals("resource", builder.resource("resource").resource());

    Assert.assertNull(builder.operation());
    Assert.assertEquals("operation", builder.operation("operation").operation());

    Assert.assertNull(builder.type());
    Assert.assertEquals("type", builder.type("type").type());

    Assert.assertNull(builder.meta());
    Map<String, String> stringString = Collections.emptyMap();
    Assert.assertSame(stringString, builder.meta(stringString).meta());

    Map<String, Number> stringNumber = Collections.emptyMap();
    Assert.assertNull(builder.metrics());
    Assert.assertSame(stringNumber, builder.metrics(stringNumber).metrics());
  }


  @Test
  public void testUnsetType() {
    SpanBuilder builder = SpanBuilder.createRoot();
    Assert.assertEquals(TraceOperation.UNKNOWN, builder.finishSpan("service").getType());

    builder = SpanBuilder.createRoot().type("");
    Assert.assertEquals(TraceOperation.UNKNOWN, builder.finishSpan("service").getType());
  }


  @Test
  public void testSetException() {
    SpanBuilder builder = SpanBuilder.createRoot();
    builder.exception(new RuntimeException("message"));

    Assert.assertEquals("message", builder.meta().get("error.msg"));
    Assert.assertEquals("java.lang.RuntimeException", builder.meta().get("error.type"));
    Assert.assertTrue(builder.meta().get("error.stack").contains("testSetException"));
  }

}
