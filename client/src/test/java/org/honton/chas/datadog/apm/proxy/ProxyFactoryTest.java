package org.honton.chas.datadog.apm.proxy;

import lombok.Getter;
import org.honton.chas.datadog.apm.TraceOperation;
import org.honton.chas.datadog.apm.api.Span;
import org.honton.chas.datadog.apm.cdi.TracerTestImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;

/**
 * Test proxy creation
 */
public class ProxyFactoryTest {

  private TracerTestImpl tracer;
  private ProxyFactory proxyFactory;

  public interface Consumer<T> {
    void accept(T t);
  }

  public interface Repeat {
    @TraceOperation(false)
    String echo(String t);
  }

  static class Sample implements Callable<String>, Consumer<String>, Repeat {
    @Getter
    private String saved;

    @Override
    public void accept(String s) {
      if(s.startsWith("what")) {
        throw new RuntimeException(s);
      }
      saved = s;
    }

    @Override
    public String call() throws Exception {
      return saved;
    }

    @Override
    public String echo(String t) {
      return t;
    }
  }

  @Before
  public void setupTracer() {
    tracer = new TracerTestImpl();
    proxyFactory = new ProxyFactory();
    proxyFactory.tracer = tracer;
  }

  private Span test(String response) throws Exception {
    Sample instance = new Sample();
    Consumer<String> proxy = proxyFactory.createProxy(instance, Consumer.class);
    try {
      proxy.accept(response);
      Assert.assertEquals(response, instance.getSaved());
    }
    catch (RuntimeException expected) {
    }
    Span span = tracer.getCapturedSpan();
    Assert.assertEquals("service", span.getService());
    Assert.assertEquals(Consumer.class.getCanonicalName(), span.getResource());
    Assert.assertEquals("accept", span.getOperation());
    return span;
  }

  @Test
  public void testWithReturn() throws Exception {
    test("hello");
  }

  @Test
  public void testWithException() throws Exception {
    Span span = test("what a mess");
    Assert.assertEquals("what a mess", span.getMeta().get("error.msg"));
    Assert.assertNotEquals(0, span.getError());
  }

  @Test
  public void testMultiInterface() throws Exception {
    Sample instance = new Sample();
    Object proxy = proxyFactory.createProxy(instance, Consumer.class, Callable.class);

    ((Consumer)proxy).accept("hello");
    Span span = tracer.getCapturedSpan();
    Assert.assertEquals("service", span.getService());
    Assert.assertEquals(Consumer.class.getCanonicalName(), span.getResource());
    Assert.assertEquals("accept", span.getOperation());

    Assert.assertEquals("hello", ((Callable)proxy).call());
    span = tracer.getCapturedSpan();
    Assert.assertEquals("service", span.getService());
    Assert.assertEquals(Callable.class.getCanonicalName(), span.getResource());
    Assert.assertEquals("call", span.getOperation());
  }

  @Test
  public void testNoTrace() throws Exception {
    Sample instance = new Sample();
    Repeat proxy = proxyFactory.createProxy(instance, Repeat.class);
    Assert.assertEquals("xx", proxy.echo("xx"));

    Assert.assertNull(tracer.getCapturedSpan());
  }
}
