package org.folio.spring.tools.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.junit.jupiter.api.Test;

class FolioKafkaPropertiesTest {

  @Test
  void constructorTest() {
    var kafkaListenerProperties = new FolioKafkaProperties.KafkaListenerProperties();
    kafkaListenerProperties.setConcurrency(5);
    kafkaListenerProperties.setTopicPattern("test-topic");
    kafkaListenerProperties.setGroupId("test-group");
    kafkaListenerProperties.setMaxPollRecords(200);
    kafkaListenerProperties.setMaxPollIntervalMs(60_000);
    kafkaListenerProperties.setAutoOffsetReset(OffsetResetStrategy.LATEST);

    var folioKafkaProperties = new FolioKafkaProperties();
    folioKafkaProperties.setListener(Map.of("events", kafkaListenerProperties));

    assertThat(folioKafkaProperties).isNotNull();
  }
}
