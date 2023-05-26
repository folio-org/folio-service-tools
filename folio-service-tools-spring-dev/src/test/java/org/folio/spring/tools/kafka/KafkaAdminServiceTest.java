package org.folio.spring.tools.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.folio.spring.tools.config.properties.FolioEnvironment;
import org.junit.jupiter.api.Test;
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
  @MockBean
  private KafkaAdminClient kafkaAdminClient;

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
    kafkaTopic.setName("folio.test_tenant.topic");
    when(kafkaProperties.getTopics()).thenReturn(List.of(kafkaTopic));

    kafkaAdminService.deleteTopicsByTenant("test_tenant");
    verify(kafkaAdminClient).deleteTopics(List.of("folio.test_tenant.topic"));
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
