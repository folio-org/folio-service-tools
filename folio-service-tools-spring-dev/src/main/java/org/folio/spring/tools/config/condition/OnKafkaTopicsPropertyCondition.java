package org.folio.spring.tools.config.condition;

import static org.springframework.boot.autoconfigure.condition.ConditionOutcome.match;
import static org.springframework.boot.autoconfigure.condition.ConditionOutcome.noMatch;

import java.util.List;
import org.folio.spring.tools.kafka.FolioKafkaProperties.KafkaTopic;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnKafkaTopicsPropertyCondition extends SpringBootCondition {

  private static final String PROPERTY_NAME = "folio.kafka.topics";
  private static final ConditionMessage.Builder MESSAGE_BUILDER = ConditionMessage.forCondition("Kafka topics");
  private static final Bindable<List<KafkaTopic>> KAFKA_TOPIC_LIST = Bindable.listOf(KafkaTopic.class);

  @Override
  public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
    var property = Binder.get(context.getEnvironment()).bind(PROPERTY_NAME, KAFKA_TOPIC_LIST);
    return property.isBound()
           ? match(MESSAGE_BUILDER.found("property").items(PROPERTY_NAME))
           : noMatch(MESSAGE_BUILDER.didNotFind("property").items(PROPERTY_NAME));
  }
}
