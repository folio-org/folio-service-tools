package org.folio.spring.tools.config;

import org.folio.spring.tools.config.condition.OnKafkaTopicsPropertyCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@ConditionalOnClass({KafkaTemplate.class})
@Conditional(OnKafkaTopicsPropertyCondition.class)
@ComponentScan(basePackages = {"org.folio.spring.tools.kafka"})
public class KafkaConfig {

}
