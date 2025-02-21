package org.folio.spring.tools.kafka;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.tools.config.properties.FolioEnvironment;
import org.springframework.messaging.MessageHeaders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.folio.spring.tools.kafka.FolioKafkaProperties.TENANT_ID;

@UtilityClass
public class KafkaUtils {
  private static final String TENANT_COLLECTION_TOPICS_ENV_VAR_NAME = "KAFKA_PRODUCER_TENANT_COLLECTION";
  private static final String TENANT_COLLECTION_MATCH_REGEX = "[A-Z][A-Z0-9]{0,30}";
  private static boolean TENANT_COLLECTION_TOPICS_ENABLED;

  private static String tenantCollectionTopicQualifier;

  static {
    tenantCollectionTopicQualifier = System.getenv(TENANT_COLLECTION_TOPICS_ENV_VAR_NAME);
    setTenantCollectionTopicsQualifier(tenantCollectionTopicQualifier);
  }

  static void setTenantCollectionTopicsQualifier(String value) {
    tenantCollectionTopicQualifier = value;
    TENANT_COLLECTION_TOPICS_ENABLED = !StringUtils.isEmpty(tenantCollectionTopicQualifier);

    if(TENANT_COLLECTION_TOPICS_ENABLED &&
      !tenantCollectionTopicQualifier.matches(TENANT_COLLECTION_MATCH_REGEX)){
      throw new IllegalArgumentException(
        String.format("%s environment variable does not match %s",
          TENANT_COLLECTION_TOPICS_ENV_VAR_NAME,
          TENANT_COLLECTION_MATCH_REGEX));
    }
  }

  public static boolean isTenantCollectionTopicsEnabled() {
    return TENANT_COLLECTION_TOPICS_ENABLED;
  }

  /**
   * Returns topic name in the format - `{env}.{tenant}.{topic-name}`
   *
   * @param initialName initial topic name as {@link String}
   * @param tenantId    tenant id as {@link String}
   * @return topic name as {@link String} object
   */
  public static String getTenantTopicName(String initialName, String tenantId) {
    return getTenantTopicName(initialName, FolioEnvironment.getFolioEnvName(), tenantId);
  }

  public static String getTenantTopicName(String initialName, String envName, String tenantId) {
    var tenantToUse = TENANT_COLLECTION_TOPICS_ENABLED ? tenantCollectionTopicQualifier : tenantId;

    return String.join(".", envName, tenantToUse, initialName);
  }

  public static String getTenantTopicNameWithNamespace(String initialName, String envName, String tenantId, String namespace) {
    var tenantToUse = TENANT_COLLECTION_TOPICS_ENABLED ? tenantCollectionTopicQualifier : tenantId;

    return String.join(".", envName, namespace, tenantToUse, initialName);
  }

  public static List<Header> toKafkaHeaders(Map<String, Collection<String>> requestHeaders) {
    if (requestHeaders == null || requestHeaders.isEmpty()) {
      return Collections.emptyList();
    }
    var list = new ArrayList<>(requestHeaders.entrySet()
      .stream()
      .map(header -> (Header) new RecordHeader(header.getKey(), retrieveFirstSafe(header.getValue()).getBytes(StandardCharsets.UTF_8)))
      .toList());

    var tenantValues = requestHeaders.get(XOkapiHeaders.TENANT);
    if (tenantValues != null && !tenantValues.isEmpty()) {
      list.add(new RecordHeader(TENANT_ID, retrieveFirstSafe(tenantValues).getBytes(StandardCharsets.UTF_8)));
    }

    return list;
  }

  public static String getHeaderValue(String headerName, MessageHeaders headers) {
    return headers.entrySet().stream()
      .filter(header -> header.getKey().equalsIgnoreCase(headerName))
      .map(header -> new String((byte[]) header.getValue(), StandardCharsets.UTF_8))
      .findFirst()
      .orElse(null);
  }

  private String retrieveFirstSafe(Collection<String> strings) {
    return strings != null && !strings.isEmpty() ? strings.iterator().next() : "";
  }
}
