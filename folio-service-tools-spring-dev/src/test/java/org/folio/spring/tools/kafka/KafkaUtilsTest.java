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
  public void tearDown(){
    // revert qualifier since KafkaTopicNameHelper is static and can affect other tests
    setTenantCollectionTopicsQualifier(null);
  }

  @Test
  public void isTenantCollectionEnabled(){
    assertFalse(isTenantCollectionTopicsEnabled());
    setTenantCollectionTopicsQualifier("COLLECTION");
    assertTrue(isTenantCollectionTopicsEnabled());
  }

  @Test
  public void shouldErrorWhenBadTenantCollectionQualifier() {
    assertThrows(RuntimeException.class, () -> setTenantCollectionTopicsQualifier("diku"));
  }

  @Test
  public void shouldFormatTopicName() {
    String topicName = getTenantTopicName("DI_COMPLETED","folio", "TEST");
    assertNotNull(topicName);
    assertEquals("folio.TEST.DI_COMPLETED", topicName);

    // enable tenant collection topics
    setTenantCollectionTopicsQualifier("COLLECTION");
    topicName = getTenantTopicName("DI_COMPLETED","folio", "TEST");
    assertNotNull(topicName);
    assertEquals("folio.COLLECTION.DI_COMPLETED", topicName);
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
