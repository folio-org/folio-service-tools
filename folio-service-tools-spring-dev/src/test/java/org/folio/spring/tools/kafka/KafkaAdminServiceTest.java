package org.folio.spring.tools.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

import java.util.*;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.internals.KafkaFutureImpl;
import org.folio.spring.tools.config.properties.FolioEnvironment;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.listener.MessageListenerContainer;

@Import(KafkaAdminServiceTest.KafkaAdminServiceTestConfiguration.class)
@SpringBootTest(classes = KafkaAdminService.class)
class KafkaAdminServiceTest {

  @Autowired
  private KafkaAdminService kafkaAdminService;
  @Autowired
  private ApplicationContext applicationContext;
  @MockBean
  private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
  @MockBean
  private KafkaAdmin kafkaAdmin;
  @MockBean
  private FolioKafkaProperties kafkaProperties;

  @Test
  void createKafkaTopics_positive() {
    when(kafkaProperties.getTopics()).thenReturn(List.of(
      FolioKafkaProperties.KafkaTopic.of("topic1", 10, null),
      FolioKafkaProperties.KafkaTopic.of("topic2", null, (short) 2),
      FolioKafkaProperties.KafkaTopic.of("topic3", 30, (short) -1)));

    kafkaAdminService.createTopics("test_tenant");
    verify(kafkaAdmin).initialize();

    var beansOfType = applicationContext.getBeansOfType(NewTopic.class);
    assertThat(beansOfType.values()).containsExactlyInAnyOrderElementsOf(List.of(
      new NewTopic("folio.test_tenant.topic1", Optional.of(10), Optional.empty()),
      new NewTopic("folio.test_tenant.topic2", Optional.empty(), Optional.of((short) 2)),
      new NewTopic("folio.test_tenant.topic3", Optional.of(30), Optional.of((short) -1))
    ));
  }

  @Test
  void deleteKafkaTopics_positive() {
    FolioKafkaProperties.KafkaTopic kafkaTopic = new FolioKafkaProperties.KafkaTopic();
    kafkaTopic.setName("test_topic");
    when(kafkaProperties.getTopics()).thenReturn(List.of(kafkaTopic));

    var listTopicResult = mock(ListTopicsResult.class);
    var future = KafkaFuture.completedFuture(Set.of("test_topic"));
    when(listTopicResult.names()).thenReturn(future);
    kafkaAdminService.deleteTopics("test_tenant");

    try(var kafkaClient = mock(KafkaAdminClient.class)) {
      verify(kafkaClient).listTopics();
      verify(kafkaClient).deleteTopics(List.of("test_topic"));
    }
  }

  @Test
  void deleteKafkaTopics_positive_withNoTopicsFound() {
    FolioKafkaProperties.KafkaTopic kafkaTopic = new FolioKafkaProperties.KafkaTopic();
    kafkaTopic.setName("test_topic");

    when(kafkaProperties.getTopics()).thenReturn(List.of(kafkaTopic));
    var kafkaClient = mock(AdminClient.class);
    try (var ignored = mockStatic(AdminClient.class, (invocation) -> kafkaClient)) {
      kafkaAdminService.deleteTopics("test_tenant");
    }

    verify(kafkaClient).listTopics();
  }

  @Test
  void deleteKafkaTopics_negative_shouldHandleException() {
    var kafkaTopic = new FolioKafkaProperties.KafkaTopic();
    kafkaTopic.setName("test_topic");

    var kafkaClient = mock(AdminClient.class);
    when(kafkaProperties.getTopics()).thenReturn(List.of(kafkaTopic));
    try (var ignored = mockStatic(AdminClient.class, (invocation) -> kafkaClient)) {
      when(kafkaClient.deleteTopics(anyCollection()))
        .thenThrow(new IllegalStateException());

      kafkaAdminService.deleteTopics("test_tenant");
    }

    verify(kafkaClient).listTopics();
  }

  @Test
  void deleteKafkaTopics_negative_shouldCallAnyMethodWithNoTenant() {
    var kafkaTopic = new FolioKafkaProperties.KafkaTopic();
    kafkaTopic.setName("test_topic");

    var kafkaClient = mock(AdminClient.class);
    when(kafkaProperties.getTopics()).thenReturn(List.of(kafkaTopic));
    try (var ignored = mockStatic(AdminClient.class, (invocation) -> kafkaClient)) {
      when(kafkaClient.deleteTopics(anyCollection()))
        .thenThrow(new IllegalStateException());

      kafkaAdminService.deleteTopics("");
    }

    verify(kafkaClient, never()).listTopics();
  }

  @Test
  void restartEventListeners() {
    var mockListenerContainer = mock(MessageListenerContainer.class);
    when(kafkaListenerEndpointRegistry.getAllListenerContainers()).thenReturn(List.of(mockListenerContainer));
    kafkaAdminService.restartEventListeners();
    verify(mockListenerContainer).start();
    verify(mockListenerContainer).stop();
  }

  @TestConfiguration
  static class KafkaAdminServiceTestConfiguration {

    @Bean(name = "folio.test_tenant.topic3.topic")
    NewTopic firstTopic() {
      return new NewTopic("folio.test_tenant.topic3", 30, (short) -1);
    }

    @Bean
    FolioEnvironment appConfigurationProperties() {
      var config = new FolioEnvironment();
      config.setEnvironment("folio");
      return config;
    }
  }
}
