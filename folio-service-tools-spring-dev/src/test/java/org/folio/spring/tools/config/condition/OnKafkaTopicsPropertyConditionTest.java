package org.folio.spring.tools.config.condition;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

class OnKafkaTopicsPropertyConditionTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
    .withUserConfiguration(TestConfig.class);

  @Test
  void propertyNotDefined() {
    this.contextRunner.run(context -> assertThat(context).doesNotHaveBean("foo"));
  }

  @Test
  void propertyDefined() {
    this.contextRunner.withPropertyValues("folio.kafka.topics[0].name=value1")
      .run(context -> assertThat(context).hasBean("foo"));
  }

  @Configuration(proxyBeanMethods = false)
  @Conditional(OnKafkaTopicsPropertyCondition.class)
  static class TestConfig {

    @Bean
    String foo() {
      return "foo";
    }

  }

}
