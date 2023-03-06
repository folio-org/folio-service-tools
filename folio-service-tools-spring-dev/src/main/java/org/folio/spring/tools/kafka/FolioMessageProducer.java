package org.folio.spring.tools.kafka;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerRecord;
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

  public void sendMessages(List<T> msgBodies) {
    log.info("Sending events to Kafka [topic: {}, number: {}]", topic.topicName(), msgBodies.size());
    log.trace("Sending events to Kafka [topic: {}, bodies: {}]", topic.topicName(), msgBodies);
    msgBodies.stream()
      .map(this::toProducerRecord)
      .forEach(template::send);
  }

  private ProducerRecord<String, T> toProducerRecord(T msgBody) {
    final var tenantId = context.getTenantId();

    msgBody.setTenant(tenantId);
    msgBody.setTs(DateUtils.currentTsInString());

    var producerRecord = new ProducerRecord<String, T>(topic.fullTopicName(tenantId), msgBody);

    KafkaUtils.toKafkaHeaders(context.getOkapiHeaders())
      .forEach(header -> producerRecord.headers().add(header));
    return producerRecord;
  }

}
