package org.honton.chas.datadog.apm.sender;

import java.util.ArrayList;
import java.util.List;

import org.honton.chas.datadog.apm.api.Trace;

/**
 * A queue for multiple suppliers and a single consumer.
 */
public class TraceQueue {

  private List<Trace> traces;

  /**
   * Supply a span. Should cause minimal wait.
   *
   * @param trace The trace to supply
   */
  public void supply(Trace trace) {
    synchronized (this) {
      if (traces == null) {
        // there were no traces, notify consumer there are now traces
        notify();
        traces = new ArrayList<>();
      }
      traces.add(trace);
    }
  }

  /**
   * Consume traces. Waits for traces as needed.
   *
   * @return All available traces
   * @throws InterruptedException
   */
  public List<Trace> consume() throws InterruptedException {
    List<Trace> rc;
    synchronized (this) {
      // wait for an available span
      while (traces == null) {
        wait();
      }
      rc = traces;
      traces = null;
    }
    return rc;
  }
}
