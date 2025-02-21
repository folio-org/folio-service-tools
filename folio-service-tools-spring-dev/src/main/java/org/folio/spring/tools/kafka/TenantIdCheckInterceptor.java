package org.folio.spring.tools.kafka;

import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.util.Map;

import static org.folio.spring.tools.kafka.FolioKafkaProperties.TENANT_ID;

/**
 * In order to register this interceptor simply add configuration to application.yml
 * <pre>
 *spring:
 *   kafka:
 *     producer:
 *       properties:
 *         interceptor:
 *           classes: org.folio.spring.tools.kafka.TenantIdCheckInterceptor
 * </pre>
 */
@Log4j2
public class TenantIdCheckInterceptor implements ProducerInterceptor<String, String> {

  protected static final String TENANT_ID_ERROR_MESSAGE = "Kafka record does not have a tenant identifying header: " + TENANT_ID + ". " +
    "Use FolioMessageProducer<T extends BaseKafkaMessage> to build the record. TopicName={}";
  @Override
  public ProducerRecord<String, String> onSend(ProducerRecord<String, String> producerRecord) {
    Headers headers = producerRecord.headers();
    boolean isTenantIdHeaderExist = false;
    for (Header header : headers) {
      if (header.key().equals(TENANT_ID)) {
        isTenantIdHeaderExist = true;
        break;
      }
    }
    if (!isTenantIdHeaderExist) {
      log.error(TENANT_ID_ERROR_MESSAGE, producerRecord.topic());
    }

    return producerRecord;
  }

  @Override
  public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
    // no-op
  }

  @Override
  public void close() {
    // no-op
  }

  @Override
  public void configure(Map<String, ?> configs) {
    // no-op
  }
}
