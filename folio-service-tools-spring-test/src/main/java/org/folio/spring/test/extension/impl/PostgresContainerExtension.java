package org.folio.spring.test.extension.impl;

import java.util.Map;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresContainerExtension implements BeforeAllCallback, AfterAllCallback {

  private static final String URL_PROPERTY_NAME = "spring.datasource.url";
  private static final String USERNAME_PROPERTY_NAME = "spring.datasource.username";
  private static final String PASSWORD_PROPERTY_NAME = "spring.datasource.password";
  private static final String ENV_NAME = "TESTCONTAINERS_POSTGRES_IMAGE";
  private static final String POSTGRES_DEFAULT_IMAGE = "postgres:12-alpine";
  private static final String POSTGRES_IMAGE = postgresImage(System.getenv());
  private static final PostgreSQLContainer<?> CONTAINER = new PostgreSQLContainer<>(POSTGRES_IMAGE)
    .withDatabaseName("folio_test").withUsername("folio_admin").withPassword("password");

  @Override
  public void beforeAll(ExtensionContext context) {
    if (!CONTAINER.isRunning()) {
      CONTAINER.start();
    }

    System.setProperty(URL_PROPERTY_NAME, CONTAINER.getJdbcUrl());
    System.setProperty(USERNAME_PROPERTY_NAME, CONTAINER.getUsername());
    System.setProperty(PASSWORD_PROPERTY_NAME, CONTAINER.getPassword());
  }

  @Override
  public void afterAll(ExtensionContext context) {
    System.clearProperty(URL_PROPERTY_NAME);
    System.clearProperty(USERNAME_PROPERTY_NAME);
    System.clearProperty(PASSWORD_PROPERTY_NAME);
  }

  static String postgresImage(Map<String,String> env) {
    return env.getOrDefault(ENV_NAME, POSTGRES_DEFAULT_IMAGE);
  }
}
