package org.honton.chas.datadog.apm.sender;

import lombok.SneakyThrows;
import org.honton.chas.datadog.apm.SpanBuilderTest;
import org.honton.chas.datadog.apm.api.Trace;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TraceQueueTest {

  TraceQueue queue = new TraceQueue();
  List<Trace> expected;

  private void addTraces(int n) {
    expected = new ArrayList<>(n);
    for(int i=0; i< n; ++i) {
      Trace single = SpanBuilderTest.getTestTrace();
      queue.supply(single);
      expected.add(single);
    }
  }

  @Test
  public void testImmediatePickup() throws InterruptedException {
    addTraces(2);
    Assert.assertEquals(expected, queue.consume());
  }

  @Test(expected = InterruptedException.class)
  public void testInterrupted() throws InterruptedException {
    Thread.currentThread().interrupt();
    queue.consume();
  }

  @Test(timeout = 2000)
  public void testWaitForConsume() throws InterruptedException {
    long start = System.currentTimeMillis();
    new Thread() {
      @Override
      @SneakyThrows
      public void run() {
        sleep(500);
        addTraces(2);
      }
    }.start();
    queue.consume();

    long total = System.currentTimeMillis() - start;
    Assert.assertTrue(total>=500);
  }
}
