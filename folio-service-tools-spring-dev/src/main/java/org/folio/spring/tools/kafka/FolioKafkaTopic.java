package org.folio.spring.tools.kafka;

import org.apache.commons.lang3.StringUtils;

public interface FolioKafkaTopic {

  String topicName();

  String envId();

  default String fullTopicName(String tenantId) {
    var envId = envId();
    var topicName = topicName();
    if (StringUtils.isAnyBlank(envId, tenantId, topicName)) {
      throw new IllegalArgumentException("envId, tenantId, topicName can't be blank");
    }
    return String.join(".", envId, tenantId, topicName);
  }

}
