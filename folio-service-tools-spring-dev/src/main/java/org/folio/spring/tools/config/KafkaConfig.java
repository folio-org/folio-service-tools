package org.folio.spring.tools.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@ConditionalOnBean({KafkaTemplate.class})
@ComponentScan(basePackages = "org.folio.spring.tools.kafka")
public class KafkaConfig { }
