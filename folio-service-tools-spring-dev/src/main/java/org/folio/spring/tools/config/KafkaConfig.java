package org.folio.spring.tools.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@ConditionalOnClass({KafkaTemplate.class})
@ConditionalOnProperty(prefix = "folio.kafka", name = "topics")
@ComponentScan(basePackages = "org.folio.spring.tools.kafka")
public class KafkaConfig { }
