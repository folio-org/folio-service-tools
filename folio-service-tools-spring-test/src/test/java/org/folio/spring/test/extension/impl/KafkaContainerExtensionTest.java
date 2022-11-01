package org.folio.spring.test.extension.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class KafkaContainerExtensionTest {

  private final KafkaContainerExtension extension = new KafkaContainerExtension();

  @Test
  void beforeAllAddSystemProperties_positive() {
    extension.beforeAll(null);
    assertThat(System.getProperty("spring.kafka.bootstrap-servers")).contains("PLAINTEXT://");
  }

  @Test
  void afterAllAddSystemProperties_positive() {
    extension.afterAll(null);
    assertThat(System.getProperty("spring.kafka.bootstrap-servers")).isNull();
  }
}
