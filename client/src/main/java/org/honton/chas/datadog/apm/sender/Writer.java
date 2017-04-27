package org.honton.chas.datadog.apm.sender;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.client.WebTarget;
import lombok.extern.slf4j.Slf4j;
import org.honton.chas.datadog.apm.TraceConfiguration;
import org.honton.chas.datadog.apm.api.ApmApi;
import org.honton.chas.datadog.apm.api.ApmApi0_2;
import org.honton.chas.datadog.apm.api.ApmApi0_3;
import org.honton.chas.datadog.apm.api.Span;
import org.honton.chas.datadog.apm.api.Trace;
import org.honton.chas.datadog.apm.jackson.MsgPackProvider;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

@Slf4j
public class Writer {

  private volatile TraceQueue queue;

  private Thread worker;
  private ApmApi apmApi;
  long backoffExpiration;
  private long backoffDuration;
  private String apmUri;

  @Inject
  void setTraceConfiguration(TraceConfiguration configuration) {
    backoffDuration = configuration.getBackoffDuration();
    apmUri = configuration.getCollectorUrl();
  }

  @PostConstruct
  void initialize() {
    initializeWith0_3();
    queue = new TraceQueue();
    startWorker();
  }

  /**
   * Queue a span for sending
   *
   * @param span The span to send to the APM collector
   */
  public void queue(Span span) {
    // queue == null is signal that worker worker is no longer running
    TraceQueue q = queue;
    if (q != null) {
      q.supply(span);
    }
  }

  /**
   * Stop the worker worker
   */
  void stop() {
    worker.interrupt();
  }

  /**
   * Has the worker been stopped?
   */
  boolean isStopped() {
    return queue == null;
  }

  /**
   * Single round of consuming traces from queue and sending to collector
   *
   * @return false, if worker worker has been shutdown
   */
  List<Span> deQueue() {
    try {
      return queue.consume();
    } catch (InterruptedException ie) {
      queue = null;
      return null;
    }
  }

  private void trySend(List<Span> spans) {
    if (System.currentTimeMillis() > backoffExpiration) {
      Trace[] traces = new Trace[spans.size()];
      int i = 0;
      for(Span span : spans) {
        traces[i++] = new Trace(span);
      }
      try {
        send(traces);
      } catch (RuntimeException re) {
        log.info("writer worker problem sending to " + apmUri, re);
        backoffExpiration = System.currentTimeMillis() + backoffDuration;
      }
    }
  }

  private void send(Trace... traces) {
    try {
      apmApi.reportTraces(traces);
    } catch (NotFoundException | NotSupportedException cee) {
      // 404, 415
      if (apmApi instanceof ApmApi0_3) {
        log.info("falling back to json");
        fallbackTo0_2();
        send(traces);
      }
    }
  }

  private void fallbackTo0_2() {
    apmApi = getProxy(ApmApi0_2.class);
  }

  private void initializeWith0_3() {
    apmApi = getProxy(ApmApi0_3.class, new MsgPackProvider());
  }

  private void startWorker() {
    worker = new Thread("APM writer") {
      {
        setDaemon(true);
      }

      @Override
      public void run() {
        for(;;) {
          List<Span> spans = deQueue();
          if(spans == null) {
            log.error("writer worker shutdown");
            break;
          }
          trySend(spans);
        }
      }
    };
    worker.start();
  }

  private <T> T getProxy(Class<T> proxyType, Object... providers) {
    ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder();
    for (Object provider : providers) {
      clientBuilder.register(provider);
    }

    ResteasyClient client = clientBuilder.build();
    WebTarget target = client.target(apmUri);
    ResteasyWebTarget rtarget = (ResteasyWebTarget) target;

    return rtarget.proxy(proxyType);
  }
}
