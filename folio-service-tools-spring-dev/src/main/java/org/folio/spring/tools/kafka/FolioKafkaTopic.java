package org.folio.spring.tools.kafka;

import org.apache.commons.lang3.StringUtils;
import org.folio.spring.tools.config.properties.FolioEnvironment;

import static org.folio.spring.tools.kafka.KafkaUtils.getTenantTopicName;

public interface FolioKafkaTopic {

  String topicName();

  default String envId() {
    return FolioEnvironment.getFolioEnvName();
  }

  default String fullTopicName(String tenantId) {
    var envId = envId();
    var topicName = topicName();
    if (StringUtils.isAnyBlank(envId, tenantId, topicName)) {
      throw new IllegalArgumentException("envId, tenantId, topicName can't be blank");
    }
    return getTenantTopicName(topicName, envId, tenantId);
  }

}
