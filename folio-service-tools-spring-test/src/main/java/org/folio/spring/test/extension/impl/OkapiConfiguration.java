package org.folio.spring.test.extension.impl;

import com.github.tomakehurst.wiremock.WireMockServer;

public record OkapiConfiguration(WireMockServer wireMockServer, int port) {

  public String getOkapiUrl() {
    return "http://localhost:" + port;
  }
}
