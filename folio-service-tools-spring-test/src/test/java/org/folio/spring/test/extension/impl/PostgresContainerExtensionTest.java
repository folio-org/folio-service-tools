package org.folio.spring.test.extension.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PostgresContainerExtensionTest {

  private final PostgresContainerExtension extension = new PostgresContainerExtension();

  @Test
  void beforeAllAddSystemProperties_positive() {
    extension.beforeAll(null);
    assertThat(System.getProperty("spring.datasource.url")).contains("folio_test");
    assertThat(System.getProperty("spring.datasource.username")).isEqualTo("folio_admin");
    assertThat(System.getProperty("spring.datasource.password")).isEqualTo("password");
  }

  @Test
  void afterAllAddSystemProperties_positive() {
    extension.afterAll(null);
    assertThat(System.getProperty("spring.datasource.url")).isNull();
  }

  @ParameterizedTest
  @MethodSource
  void postgresImage(Map<String, String> env, String expected) {
    assertThat(PostgresContainerExtension.postgresImage(env)).isEqualTo(expected);
  }

  static Stream<Arguments> postgresImage() {
    return Stream.of(
        Arguments.of(Map.of(), "postgres:12-alpine"),
        Arguments.of(Map.of("foo", "bar"), "postgres:12-alpine"),
        Arguments.of(Map.of("foo", "bar", "TESTCONTAINERS_POSTGRES_IMAGE", "x:y-z"), "x:y-z")
        );
  }
}
