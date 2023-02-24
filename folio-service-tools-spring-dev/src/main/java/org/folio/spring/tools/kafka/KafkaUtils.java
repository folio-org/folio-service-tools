package org.folio.spring.tools.kafka;

import lombok.experimental.UtilityClass;
import org.folio.spring.tools.config.properties.FolioEnvironment;

@UtilityClass
public class KafkaUtils {

  /**
   * Returns topic name in the format - `{env}.{tenant}.{topic-name}`
   *
   * @param initialName initial topic name as {@link String}
   * @param tenantId    tenant id as {@link String}
   * @return topic name as {@link String} object
   */
  public static String getTenantTopicName(String initialName, String tenantId) {
    return String.format("%s.%s.%s", FolioEnvironment.getFolioEnvName(), tenantId, initialName);
  }
}