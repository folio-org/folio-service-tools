package org.folio.test.junit;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class TestStartLoggingRule extends TestWatcher {

  private static final Logger logger = LoggerFactory.getLogger(TestStartLoggingRule.class);

  @Override
  protected void starting(Description description) {
    logger.info("********** Running test method: {}.{} ********** ", description.getClassName(),
      description.getMethodName());
  }

}
