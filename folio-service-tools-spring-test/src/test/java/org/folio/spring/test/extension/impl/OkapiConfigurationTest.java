package org.folio.spring.test.extension.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OkapiConfigurationTest {

  @Test
  void getOkapiUrl_positive() {
    var okapiConfiguration = new OkapiConfiguration(null, 1000);

    assertEquals("http://localhost:1000", okapiConfiguration.getOkapiUrl());
  }
}
