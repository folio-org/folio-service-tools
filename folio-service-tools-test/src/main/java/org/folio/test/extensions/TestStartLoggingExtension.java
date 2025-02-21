package org.folio.test.extensions;

import org.folio.test.util.TestUtil;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestStartLoggingExtension implements BeforeEachCallback {

  private TestStartLoggingExtension() {}

  public static TestStartLoggingExtension instance() {
    return new TestStartLoggingExtension();
  }

  @Override
  public void beforeEach(ExtensionContext extensionContext) {
    TestUtil.logger().info("********** Running test method: {}.{} ********** ",
      extensionContext.getRequiredTestClass().getName(),
      extensionContext.getRequiredTestMethod().getName());
  }
}
