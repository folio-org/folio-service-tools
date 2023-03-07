package org.folio.spring.tools.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FolioKafkaTopicTest {

  @Test
  void fullTopicName() {
    var folioKafkaTopic = new FolioKafkaTopic() {

      @Override
      public String topicName() {
        return "test-name";
      }

      @Override
      public String envId() {
        return "test-env";
      }
    };

    var actual = folioKafkaTopic.fullTopicName("test-tenant");
    assertEquals("test-env.test-tenant.test-name", actual);
  }

  @CsvSource({
    "name,env,",
    "name,,tenant",
    ",env,tenant"
  })
  @ParameterizedTest
  void fullTopicName_whenSomeIsNullOrBlank(String name, String env, String tenant) {
    var folioKafkaTopic = new FolioKafkaTopic() {

      @Override
      public String topicName() {
        return name;
      }

      @Override
      public String envId() {
        return env;
      }
    };

    assertThrows(IllegalArgumentException.class, () -> folioKafkaTopic.fullTopicName(tenant));
  }
}
