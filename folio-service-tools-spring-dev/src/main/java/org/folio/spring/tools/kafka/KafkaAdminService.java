package org.folio.spring.tools.kafka;

import static java.util.Optional.ofNullable;
import static org.folio.spring.tools.kafka.KafkaUtils.getTenantTopicName;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class KafkaAdminService {

  private final KafkaAdmin kafkaAdmin;
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

  public void deleteTopics(String tenantId) {
    if (tenantId == null || tenantId.isEmpty()) {
      log.warn("Invalid tenantId: {}", tenantId);
      return;
    }

    List<String> topicsToDelete = kafkaProperties.getTopics().stream()
      .map(topic -> getTenantTopicName(topic.getName(), tenantId))
      .toList();

    log.info("Deleting topics for tenantId {}: [topics: {}]", tenantId, topicsToDelete);

    try (AdminClient kafkaClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
      ListTopicsResult listTopicsResult = kafkaClient.listTopics();
      if (listTopicsResult == null || listTopicsResult.names() == null) {
        log.warn("No existing topics to delete for tenantId: {}", tenantId);
        return;
      }
      Set<String> existingTopics;
      try {
        existingTopics = listTopicsResult.names().get();
      } catch (InterruptedException | ExecutionException e) {
        Thread.currentThread().interrupt();
        throw new KafkaException(String.format("No existing topics to delete for tenantId: %s", tenantId));
      }
      List<String> topicsToBeDeleted = topicsToDelete.stream()
        .filter(existingTopics::contains)
        .toList();

      if (topicsToBeDeleted.isEmpty()) {
        log.warn("No existing topics to delete for tenantId: {}", tenantId);
        return;
      }

      DeleteTopicsResult deleteTopicsResult = kafkaClient.deleteTopics(topicsToBeDeleted);

      processDeleteResult(topicsToBeDeleted, deleteTopicsResult, tenantId);
    }
  }

  private static void processDeleteResult(List<String> topicsToBeDeleted,
                                          DeleteTopicsResult deleteTopicsResult, String tenantId) {
    try {
      deleteTopicsResult.all().get();
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new KafkaException(String.format("There was an error while deleting topics by tenant: %s", tenantId));
    }
    log.info("Topics deleted successfully: {}", topicsToBeDeleted);
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
      .map(topic -> toKafkaTopic(topic, tenantId))
      .toList();
  }

  private NewTopic toKafkaTopic(FolioKafkaProperties.KafkaTopic topic, String tenantId) {
    return new NewTopic(
      getTenantTopicName(topic.getName(), tenantId),
      ofNullable(topic.getNumPartitions()),
      ofNullable(topic.getReplicationFactor())
    );
  }
}
