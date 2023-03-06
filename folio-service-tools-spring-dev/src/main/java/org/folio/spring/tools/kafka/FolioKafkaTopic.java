package org.folio.spring.tools.kafka;

public interface FolioKafkaTopic {

  String topicName();

  String envId();

  default String fullTopicName(String tenantId) {
    return String.join(".", envId(), tenantId, topicName());
  }

}
