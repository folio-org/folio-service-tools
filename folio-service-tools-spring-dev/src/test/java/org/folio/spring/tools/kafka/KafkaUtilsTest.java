package org.folio.spring.tools.kafka;

import org.folio.spring.integration.XOkapiHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.folio.spring.tools.kafka.FolioKafkaProperties.TENANT_ID;
import static org.folio.spring.tools.kafka.KafkaUtils.getTenantTopicName;
import static org.folio.spring.tools.kafka.KafkaUtils.getTenantTopicNameWithNamespace;
import static org.folio.spring.tools.kafka.KafkaUtils.isTenantCollectionTopicsEnabled;
import static org.folio.spring.tools.kafka.KafkaUtils.setTenantCollectionTopicsQualifier;
import static org.folio.spring.tools.kafka.KafkaUtils.toKafkaHeaders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KafkaUtilsTest {
  @AfterEach
  void tearDown(){
    // revert qualifier since KafkaTopicNameHelper is static and can affect other tests
    setTenantCollectionTopicsQualifier(null);
  }

  @Test
  void isTenantCollectionEnabled(){
    assertFalse(isTenantCollectionTopicsEnabled());
    setTenantCollectionTopicsQualifier("COLLECTION");
    assertTrue(isTenantCollectionTopicsEnabled());
  }

  @Test
  void shouldErrorWhenBadTenantCollectionQualifier() {
    assertThrows(RuntimeException.class, () -> setTenantCollectionTopicsQualifier("diku"));
  }

  @Test
  void shouldFormatTopicName() {
    String initialName = "DI_COMPLETED";
    String envName = "folio";
    String namespace = "Default";
    String tenantId = "TEST";

    String topicName = getTenantTopicName(initialName, envName, tenantId);
    assertNotNull(topicName);
    assertEquals("folio.TEST.DI_COMPLETED", topicName);

    String topicNameWithNamespace = getTenantTopicNameWithNamespace(initialName, envName, tenantId, namespace);
    assertNotNull(topicNameWithNamespace);
    assertEquals("folio.Default.TEST.DI_COMPLETED", topicNameWithNamespace);

    // enable tenant collection topics
    setTenantCollectionTopicsQualifier("COLLECTION");
    topicName = getTenantTopicName(initialName, envName, tenantId);
    assertNotNull(topicName);
    assertEquals("folio.COLLECTION.DI_COMPLETED", topicName);

    topicNameWithNamespace = getTenantTopicNameWithNamespace(initialName, envName, tenantId, namespace);
    assertNotNull(topicNameWithNamespace);
    assertEquals("folio.Default.COLLECTION.DI_COMPLETED", topicNameWithNamespace);
  }

  @Test
  void toKafkaHeadersShouldIncludeTenantIdHeader() {
    Map<String, Collection<String>> requestHeaders = new HashMap<>();
    requestHeaders.put("SomeKey", List.of("SomeValue"));
    requestHeaders.put(XOkapiHeaders.TENANT, List.of("TEST"));
    var headers = toKafkaHeaders(requestHeaders);
    var header = headers.stream().filter(h -> TENANT_ID.equals(h.key())).findFirst().orElseThrow();
    assertEquals("TEST", new String(header.value(), StandardCharsets.UTF_8));
  }
}
