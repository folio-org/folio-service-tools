package org.folio.spring.tools.kafka;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.tools.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

@Log4j2
@RequiredArgsConstructor
public class FolioMessageProducer<T extends BaseKafkaMessage> {

  private final KafkaTemplate<String, T> template;
  private final FolioKafkaTopic topic;

  @Autowired
  private FolioExecutionContext context;

  @Setter
  private Predicate<Header> headerPredicate;
  @Setter
  private Function<T, String> keyMapper;

  /**
   * Populates bodies with `tenant` and `ts` fields from {@link BaseKafkaMessage}
   * If no headerPredicate was set via {@link org.folio.spring.tools.kafka.FolioMessageProducer#setHeaderPredicate}
   * then all Okapi headers added to the Kafka message
   *
   * @param msgBodies msg bodies that will be sent via KafkaTemplate
   */
  public void sendMessages(List<T> msgBodies) {
    log.info("Sending events to Kafka [topic: {}, number: {}]", topic.topicName(), msgBodies.size());
    log.trace("Sending events to Kafka [topic: {}, bodies: {}]", topic.topicName(), msgBodies);
    msgBodies.stream()
      .map(this::toProducerRecord)
      .forEach(template::send);
  }

  public void setContext(FolioExecutionContext context) {
    Objects.requireNonNull(context);
    this.context = context;
  }

  private ProducerRecord<String, T> toProducerRecord(T msgBody) {
    final var tenantId = context.getTenantId();

    msgBody.setTenant(tenantId);
    msgBody.setTs(DateUtils.currentTsInString());

    ProducerRecord<String, T> producerRecord;
    if (keyMapper == null) {
      producerRecord = new ProducerRecord<>(topic.fullTopicName(tenantId), msgBody);
    } else {
      producerRecord = new ProducerRecord<>(topic.fullTopicName(tenantId), keyMapper.apply(msgBody), msgBody);
    }

    KafkaUtils.toKafkaHeaders(headerPredicate == null ? context.getOkapiHeaders() : context.getAllHeaders()).stream()
      .filter(header -> headerPredicate == null || headerPredicate.test(header))
      .forEach(header -> producerRecord.headers().add(header));
    return producerRecord;
  }

}
