package org.folio.spring.tools.kafka;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.folio.spring.tools.kafka.KafkaUtils.getTenantTopicName;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class KafkaAdminService {

  private final KafkaAdmin kafkaAdmin;
  private final KafkaAdminClient kafkaAdminClient;
  private final BeanFactory beanFactory;
  private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
  private final FolioKafkaProperties kafkaProperties;

  /**
   * Creates kafka topics using existing configuration in application.kafka.topics.
   */
  public void createTopics(String tenantId) {
    var configTopics = kafkaProperties.getTopics();
    var newTopics = toTenantSpecificTopic(configTopics, tenantId);

    log.info("Creating topics for kafka [topics: {}]", newTopics);
    var configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
    newTopics.forEach(newTopic -> {
      var beanName = newTopic.name() + ".topic";
      if (!configurableBeanFactory.containsBean(beanName)) {
        configurableBeanFactory.registerSingleton(beanName, newTopic);
      }
    });
    kafkaAdmin.initialize();
  }

  public void deleteTopicsByTenant(String tenantId) {
    List<String> topicsToDelete = kafkaProperties.getTopics().stream()
                                                             .filter(t -> t.getName().contains("." + tenantId + "."))
                                                             .map(FolioKafkaProperties.KafkaTopic::getName)
                                                             .toList();
    log.info("Deleting topics for tenantId {}: [topics: {}]", tenantId, topicsToDelete);
    kafkaAdminClient.deleteTopics(topicsToDelete);
  }

  /**
   * Restarts kafka event listeners in module.
   */
  public void restartEventListeners() {
    kafkaListenerEndpointRegistry.getAllListenerContainers().forEach(container -> {
        log.info("Restarting kafka consumer to start listening created topics [ids: {}]",
          container.getListenerId());
        container.stop();
        container.start();
      }
    );
  }

  private List<NewTopic> toTenantSpecificTopic(List<FolioKafkaProperties.KafkaTopic> localConfigTopics,
                                               String tenantId) {
    return localConfigTopics.stream()
      .map(topic -> toKafkaTopic(topic, tenantId)).toList();
  }

  private NewTopic toKafkaTopic(FolioKafkaProperties.KafkaTopic topic, String tenantId) {
    return new NewTopic(
      getTenantTopicName(topic.getName(), tenantId),
      ofNullable(topic.getNumPartitions()),
      ofNullable(topic.getReplicationFactor())
    );
  }
}
