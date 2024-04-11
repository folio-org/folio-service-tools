package org.folio.spring.tools.kafka;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;
import org.folio.spring.tools.config.properties.FolioEnvironment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.listener.MessageListenerContainer;

@Import(KafkaAdminServiceTest.KafkaAdminServiceTestConfiguration.class)
@SpringBootTest(classes = KafkaAdminService.class)
class KafkaAdminServiceTest {

  private static final String TEST_TENANT = "test_tenant";

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

    kafkaAdminService.createTopics(TEST_TENANT);
    verify(kafkaAdmin).initialize();

    var beansOfType = applicationContext.getBeansOfType(NewTopic.class);
    assertThat(beansOfType.values()).containsExactlyInAnyOrderElementsOf(List.of(
      new NewTopic("folio.test_tenant.topic1", Optional.of(10), Optional.empty()),
      new NewTopic("folio.test_tenant.topic2", Optional.empty(), Optional.of((short) 2)),
      new NewTopic("folio.test_tenant.topic3", Optional.of(30), Optional.of((short) -1))
    ));
  }

  @Test
  void deleteKafkaTopics_positive()  {
    FolioKafkaProperties.KafkaTopic kafkaTopic = new FolioKafkaProperties.KafkaTopic();
    kafkaTopic.setName("test_topic");

    var future = KafkaFuture.completedFuture(Set.of("folio.test_tenant.test_topic"));
    var listTopicResult = mock(ListTopicsResult.class);
    when(listTopicResult.names()).thenReturn(future);

    when(kafkaProperties.getTopics()).thenReturn(List.of(kafkaTopic));
    var kafkaClient = mock(AdminClient.class);
    try (var ignored = mockStatic(AdminClient.class, (invocation) -> kafkaClient)) {
      var deleteTopicsResult = mock(DeleteTopicsResult.class);
      when(kafkaClient.listTopics()).thenReturn(listTopicResult);
      when(kafkaClient.deleteTopics(anyCollection())).thenReturn(deleteTopicsResult);
      when(deleteTopicsResult.all()).thenReturn(KafkaFuture.completedFuture(mock(Void.class)));
      kafkaAdminService.deleteTopics(TEST_TENANT);
    }

    verify(kafkaClient).listTopics();
    verify(kafkaClient).deleteTopics(List.of("folio.test_tenant.test_topic"));
  }

  @Test
  void deleteKafkaTopics_positive_withNoMatchingTopic()  {
    FolioKafkaProperties.KafkaTopic kafkaTopic = new FolioKafkaProperties.KafkaTopic();
    kafkaTopic.setName("test_topic");

    var future = KafkaFuture.completedFuture(Set.of("folio.test_tenant.test_topic2"));
    var listTopicResult = mock(ListTopicsResult.class);
    when(listTopicResult.names()).thenReturn(future);

    when(kafkaProperties.getTopics()).thenReturn(List.of(kafkaTopic));
    var kafkaClient = mock(AdminClient.class);
    try (var ignored = mockStatic(AdminClient.class, (invocation) -> kafkaClient)) {
      when(kafkaClient.listTopics()).thenReturn(listTopicResult);
      kafkaAdminService.deleteTopics(TEST_TENANT);
    }

    verify(kafkaClient).listTopics();
    verify(kafkaClient, never()).deleteTopics(List.of("folio.test_tenant.test_topic"));
  }

  @Test
  void deleteKafkaTopics_positive_withNoTopicsFound()  {
    FolioKafkaProperties.KafkaTopic kafkaTopic = new FolioKafkaProperties.KafkaTopic();
    kafkaTopic.setName("test_topic");

    when(kafkaProperties.getTopics()).thenReturn(List.of(kafkaTopic));
    var kafkaClient = mock(AdminClient.class);
    try (var ignored = mockStatic(AdminClient.class, (invocation) -> kafkaClient)) {
      kafkaAdminService.deleteTopics(TEST_TENANT);
    }

    verify(kafkaClient).listTopics();
  }

  @Test
  void deleteKafkaTopics_negative_shouldHandleException() throws ExecutionException, InterruptedException, KafkaException {
    var kafkaTopic = new FolioKafkaProperties.KafkaTopic();
    kafkaTopic.setName("test_topic");
    var future = KafkaFuture.completedFuture(Set.of("folio.test_tenant.test_topic"));
    var listTopicResult = mock(ListTopicsResult.class);
    when(listTopicResult.names()).thenReturn(future);

    var kafkaClient = mock(AdminClient.class);
    when(kafkaProperties.getTopics()).thenReturn(List.of(kafkaTopic));
    try (var ignored = mockStatic(AdminClient.class, (invocation) -> kafkaClient)) {

      when(kafkaClient.listTopics()).thenReturn(listTopicResult);
      given(listTopicResult.names().get()).willAnswer(invocation -> {throw new InterruptedException();});

      assertThrows(InterruptedException.class, ()->{
        kafkaAdminService.deleteTopics(TEST_TENANT);
      });

    }

    verify(kafkaClient).listTopics();
  }

  @Test
  void deleteKafkaTopics_negative_shouldHandleExceptionWithNoDeleteResult() {
    var kafkaTopic = new FolioKafkaProperties.KafkaTopic();
    kafkaTopic.setName("test_topic");

    var future = KafkaFuture.completedFuture(Set.of("folio.test_tenant.test_topic"));
    var listTopicResult = mock(ListTopicsResult.class);
    when(listTopicResult.names()).thenReturn(future);

    var kafkaClient = mock(AdminClient.class);
    when(kafkaProperties.getTopics()).thenReturn(List.of(kafkaTopic));
    try (var ignored = mockStatic(AdminClient.class, (invocation) -> kafkaClient)) {

      var deleteTopicsResult = mock(DeleteTopicsResult.class);
      when(kafkaClient.listTopics()).thenReturn(listTopicResult);
      when(kafkaClient.deleteTopics(anyCollection())).thenReturn(deleteTopicsResult);
      when(deleteTopicsResult.all()).thenThrow(new KafkaException("There was an error while deleting topics by tenant: test_tenant"));

      Exception exception = assertThrows(KafkaException.class, ()->{
        kafkaAdminService.deleteTopics(TEST_TENANT);
      });

      assertEquals("There was an error while deleting topics by tenant: test_tenant", exception.getMessage());
    }

    verify(kafkaClient).listTopics();
  }

  @Test
  void deleteKafkaTopics_negative_shouldNotCallAnyMethodWithNoTenant()  {
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
  void deleteKafkaTopics_positive_shouldNotDeleteTopics_whenTenantCollectionFeatureIsEnabled()  {
    try (var ignored = mockStatic(KafkaUtils.class)) {
      when(KafkaUtils.isTenantCollectionTopicsEnabled()).thenReturn(true);

      kafkaAdminService.deleteTopics(TEST_TENANT);
    }

    verify(kafkaAdmin, never()).describeTopics(any());
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
