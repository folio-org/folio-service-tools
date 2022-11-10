package org.folio.spring.tools.config;

import org.folio.spring.tools.kafka.FolioKafkaProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@ComponentScan(basePackages = "org.folio.spring.tools.batch")
@ConditionalOnProperty(prefix = "folio.retry", name = "enabled", havingValue = "true")
public class RetryTemplateConfiguration {

  public static final String DEFAULT_RETRY_TEMPLATE_NAME = "defaultRetryTemplate";
  public static final String DEFAULT_KAFKA_RETRY_TEMPLATE_NAME = "defaultKafkaRetryTemplate";

  @Bean(name = DEFAULT_RETRY_TEMPLATE_NAME)
  public RetryTemplate defaultRetryTemplate() {
    return RetryTemplate.builder()
      .maxAttempts(5)
      .fixedBackoff(1000)
      .build();
  }

  @Bean(name = DEFAULT_KAFKA_RETRY_TEMPLATE_NAME)
  @ConditionalOnBean(FolioKafkaProperties.class)
  public RetryTemplate defaultKafkaRetryTemplate(FolioKafkaProperties folioKafkaProperties) {
    return RetryTemplate.builder()
      .maxAttempts((int) folioKafkaProperties.getRetryDeliveryAttempts())
      .fixedBackoff(folioKafkaProperties.getRetryIntervalMs())
      .build();
  }
}
