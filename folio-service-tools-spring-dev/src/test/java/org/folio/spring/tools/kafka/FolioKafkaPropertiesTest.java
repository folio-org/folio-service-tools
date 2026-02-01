package org.folio.spring.tools.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class FolioKafkaPropertiesTest {

  @Test
  void constructorTest() {
    var initialValues = new FolioKafkaProperties.KafkaListenerProperties();
    initialValues.setConcurrency(5);
    initialValues.setTopicPattern("test-topic");
    initialValues.setGroupId("test-group");
    initialValues.setMaxPollRecords(200);
    initialValues.setMaxPollIntervalMs(60_000);
    initialValues.setAutoOffsetReset(FolioKafkaProperties.OffsetResetStrategy.LATEST);

    var folioKafkaProperties = new FolioKafkaProperties();
    folioKafkaProperties.setListener(Map.of("events", initialValues));

    assertThat(folioKafkaProperties).isNotNull();
    assertThat(folioKafkaProperties.getListener().get("events").getConcurrency()).isEqualTo(initialValues.getConcurrency());
    assertThat(folioKafkaProperties.getListener().get("events").getTopicPattern()).isEqualTo(initialValues.getTopicPattern());
    assertThat(folioKafkaProperties.getListener().get("events").getGroupId()).isEqualTo(initialValues.getGroupId());
    assertThat(folioKafkaProperties.getListener().get("events").getMaxPollRecords()).isEqualTo(initialValues.getMaxPollRecords());
    assertThat(folioKafkaProperties.getListener().get("events").getMaxPollIntervalMs()).isEqualTo(initialValues.getMaxPollIntervalMs());
    assertThat(folioKafkaProperties.getListener().get("events").getAutoOffsetReset()).isEqualTo(initialValues.getAutoOffsetReset());
    assertThat(folioKafkaProperties.getListener().get("events").isSharedGroup()).isTrue();
  }

  @Test
  void constructorTestWithSharedGroupFalse() {
    var initialValues = new FolioKafkaProperties.KafkaListenerProperties();
    initialValues.setGroupId("test-group");
    initialValues.setSharedGroup(false);

    var folioKafkaProperties = new FolioKafkaProperties();
    folioKafkaProperties.setListener(Map.of("events", initialValues));

    assertThat(folioKafkaProperties).isNotNull();
    assertThat(folioKafkaProperties.getListener().get("events").getGroupId()).startsWith("test-group-");
    assertThat(folioKafkaProperties.getListener().get("events").isSharedGroup()).isFalse();
  }
}
