package org.honton.chas.datadog.apm.sender;

import java.util.ArrayList;
import java.util.List;

import org.honton.chas.datadog.apm.api.Span;
import org.honton.chas.datadog.apm.api.Trace;

/**
 * A queue for multiple suppliers and a single consumer.
 */
public class TraceQueue {

  private List<Span> spans;

  /**
   * Supply a span. Should cause minimal wait.
   *
   * @param span The trace to supply
   */
  public void supply(Span span) {
    synchronized (this) {
      if (spans == null) {
        // there were no traces, notify consumer there are now traces
        notify();
        spans = new ArrayList<>();
      }
      spans.add(span);
    }
  }

  /**
   * Consume traces. Waits for traces as needed.
   *
   * @return All available traces
   * @throws InterruptedException
   */
  public List<Span> consume() throws InterruptedException {
    List<Span> rc;
    synchronized (this) {
      // wait for an available span
      while (spans == null) {
        wait();
      }
      rc = spans;
      spans = null;
    }
    return rc;
  }
}
