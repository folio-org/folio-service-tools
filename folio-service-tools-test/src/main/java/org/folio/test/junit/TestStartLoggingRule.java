package org.folio.test.junit;

import static org.folio.test.util.TestUtil.logger;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class TestStartLoggingRule extends TestWatcher {

  private static final TestStartLoggingRule INSTANCE = new TestStartLoggingRule();


  public static TestStartLoggingRule instance() {
    return INSTANCE;
  }

  private TestStartLoggingRule() {
  }

  @Override
  protected void starting(Description description) {
    logger().info("********** Running test method: {}.{} ********** ", description.getClassName(),
      description.getMethodName());
  }

}
