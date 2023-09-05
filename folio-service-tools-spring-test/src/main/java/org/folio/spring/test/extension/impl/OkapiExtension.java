package org.folio.spring.test.extension.impl;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.Optional.ofNullable;
import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.makeAccessible;
import static org.springframework.util.ReflectionUtils.setField;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.Extension;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.PrimitiveIterator;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class OkapiExtension implements BeforeAllCallback, AfterAllCallback {

  private static final String OKAPI_URL_PROPERTY = "folio.okapi-url";
  private final WireMockServer wireMock;

  public OkapiExtension() {
    this(new Extension[0]);
  }

  public OkapiExtension(Extension... wiremockExtensions) {
    wireMock = new WireMockServer(wireMockConfig().port(nextFreePort()).extensions(wiremockExtensions));
  }

  private static int nextFreePort() {
    return nextFreePort(ThreadLocalRandom.current() //NOSONAR
      .ints(49152, 65535).iterator());
  }

  private static int nextFreePort(PrimitiveIterator.OfInt portsToTry) {
    var maxTries = 10000;
    for (int i = 0; i < maxTries; i++) {
      int port = portsToTry.nextInt();
      if (isLocalPortFree(port)) {
        return port;
      }
    }
    return 8081;
  }

  /**
   * Check a local TCP port.
   *
   * @param port the TCP port number, must be from 1 ... 65535
   * @return true if the port is free (unused), false if the port is already in use
   */
  private static boolean isLocalPortFree(int port) {
    try {
      new ServerSocket(port).close();
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    wireMock.stop();
    System.clearProperty(OKAPI_URL_PROPERTY);
  }

  @Override
  public void beforeAll(ExtensionContext extensionContext) {
    wireMock.start();

    var okapiConfiguration = new OkapiConfiguration(wireMock, wireMock.port());
    System.setProperty(OKAPI_URL_PROPERTY, okapiConfiguration.getOkapiUrl());

    ofNullable(findField(extensionContext.getRequiredTestClass(), null, OkapiConfiguration.class))
      .ifPresent(field -> {
        makeAccessible(field);
        setField(field, null, okapiConfiguration);
      });
  }
}
