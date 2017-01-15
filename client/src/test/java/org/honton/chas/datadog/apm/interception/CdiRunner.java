package org.honton.chas.datadog.apm.interception;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * Unit test runner for CDI.
 */
public class CdiRunner extends BlockJUnit4ClassRunner {

  private static final WeldContainer CONTAINER = new Weld().initialize();

  public CdiRunner(Class<Object> clazz) throws InitializationError {
    super(clazz);
  }

  private <T> T getBean(Class<T> type) {
    return CONTAINER.instance().select(type).get();
  }

  @Override
  protected Object createTest() {
    return getBean(getTestClass().getJavaClass());
  }
}
