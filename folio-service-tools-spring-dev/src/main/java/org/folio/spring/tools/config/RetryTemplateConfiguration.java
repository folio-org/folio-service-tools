package org.folio.spring.tools.config;

import java.time.Duration;
import org.folio.spring.tools.kafka.FolioKafkaProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;

@Configuration
@ComponentScan(basePackages = "org.folio.spring.tools.batch")
@ConditionalOnProperty(prefix = "folio.retry", name = "enabled", havingValue = "true")
public class RetryTemplateConfiguration {

  public static final String DEFAULT_RETRY_TEMPLATE_NAME = "defaultRetryTemplate";
  public static final String DEFAULT_KAFKA_RETRY_TEMPLATE_NAME = "defaultKafkaRetryTemplate";

  @Bean(name = DEFAULT_RETRY_TEMPLATE_NAME)
  public RetryTemplate defaultRetryTemplate() {
    return new RetryTemplate(RetryPolicy.builder()
      .maxRetries(5)
      .delay(Duration.ofMillis(1000))
      .build());
  }

  @Bean(name = DEFAULT_KAFKA_RETRY_TEMPLATE_NAME)
  @ConditionalOnBean(FolioKafkaProperties.class)
  public RetryTemplate defaultKafkaRetryTemplate(FolioKafkaProperties folioKafkaProperties) {
    return new RetryTemplate(RetryPolicy.builder()
      .maxRetries(folioKafkaProperties.getRetryDeliveryAttempts())
      .delay(Duration.ofMillis(folioKafkaProperties.getRetryIntervalMs()))
      .build());
  }
}
