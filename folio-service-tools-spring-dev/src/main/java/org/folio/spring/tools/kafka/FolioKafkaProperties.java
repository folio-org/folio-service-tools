package org.folio.spring.tools.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Application properties for kafka message consumer.
 */
@Data
@Component
@ConfigurationProperties("folio.kafka")
public class FolioKafkaProperties {
  /**
   * Kafka header name for tenant_id
   */
  public static final String TENANT_ID = "folio.tenantId";

  /**
   * Map with settings for application kafka listeners.
   */
  private Map<String, KafkaListenerProperties> listener;

  /**
   * Specifies time to wait before reattempting delivery.
   */
  private long retryIntervalMs = 1000;

  /**
   * How many delivery attempts to perform when message failed.
   */
  private long retryDeliveryAttempts = 5;

  /**
   * What topics should be created by module.
   */
  private List<KafkaTopic> topics;

  /**
   * Contains set of settings for specific kafka listener.
   */
  @Data
  public static class KafkaListenerProperties {

    /**
     * List of topic to listen.
     */
    private String topicPattern;

    /**
     * Number of concurrent consumers in service.
     */
    private Integer concurrency = 5;

    /**
     * The group id.
     */
    private String groupId;

    /**
     * Max amount of record for a single poll.
     */
    private Integer maxPollRecords;

    /**
     * Max processing time for a single poll.
     */
    private Integer maxPollIntervalMs;

    /**
     * Specifies what offset to set in case there's no commited offset.
     * Either earliest available to read all messages still present in topic or latest to read only new messages.
     */
    private OffsetResetStrategy autoOffsetReset;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor(staticName = "of")
  public static class KafkaTopic {

    /**
     * Topic name.
     */
    private String name;

    /**
     * Number of partitions for topic.
     */
    private Integer numPartitions;

    /**
     * Replication factor for topic.
     */
    private Short replicationFactor;
  }
}
