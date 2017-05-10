package org.honton.chas.datadog.apm.sender;

import lombok.extern.slf4j.Slf4j;
import org.honton.chas.datadog.apm.TraceConfiguration;
import org.honton.chas.datadog.apm.api.*;
import org.honton.chas.datadog.apm.jackson.MsgPackProvider;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.client.WebTarget;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
      try {
        Collection<Trace> traces = toTraces(spans);
        log.debug("traces: {}", traces);
        send(traces);
      } catch (RuntimeException re) {
        log.info("writer worker problem sending to " + apmUri, re);
        backoffExpiration = System.currentTimeMillis() + backoffDuration;
      }
    }
  }

  private static Collection<Trace> toTraces(List<Span> spans) {
    List<Trace> traces = new ArrayList<>(spans.size());
    for(Span span : spans) {
      traces.add(new Trace(span));
    }
    return traces;
  }

  private void send(Collection<Trace> traces) {
    try {
      apmApi.reportTraces(traces);
    } catch (NotFoundException | NotSupportedException cee) {
      // 404, 415
      if (apmApi instanceof ApmApi0_3) {
        log.info("falling back to json");
        fallbackTo0_2();
        send(traces);
      }
    } catch (BadRequestException bre) {
      log.error("{}: {}", bre.getMessage(), traces);
    }
  }

  private void fallbackTo0_2() {
    apmApi = getProxy(ApmApi0_2.class, new JacksonShim());
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

  private <T> T getProxy(Class<T> proxyType, Object provider) {
    ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder();
    clientBuilder.register(provider);

    ResteasyClient client = clientBuilder.build();
    WebTarget target = client.target(apmUri);
    ResteasyWebTarget rtarget = (ResteasyWebTarget) target;

    return rtarget.proxy(proxyType);
  }
}
