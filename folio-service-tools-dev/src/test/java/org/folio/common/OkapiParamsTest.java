package org.folio.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.folio.test.extensions.TestStartLoggingExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.folio.okapi.common.XOkapiHeaders;


class OkapiParamsTest {

  private static final String TENANT = "TEST";
  private static final String TOKEN = "qRjjl7T3PfnP8wUPhHcVKrnbYZq0rETLg7EcMW7ciF17rE2YD5He0Dj3LfuTRSwX" +
    "LKvcp2eibvNYGqGPewoJhQw3hXAP3i1jjcRhcygQwi3bq1OeaWAhIYrKIVkJ7Wjp" +
    "x9TiWBgus2avFf3V0fvaszfj2UNi4eJYmlkaxptYLODAO4JTfRF4A5jYTmfAd8Jb" +
    "FOVVoTUw7ggl7DY0IzP48hb9SgCtbVcAQysr5HyuZZijIAOGz3UwIQMcxxdHItJo" +
    "N6q1I51fnrNo8v0ZoGLt9nH0QvdZO1BgdvPyUxShvlMbOtYFU5fc6hIFZyZGdLnt";
  private static final String HOST = "localhost";
  private static final int PORT = 8080;
  private static final String URL = String.format("http://%s:%s", HOST, PORT);
  private static final String SOME_HEADER = "SomeHeader";
  private static final String SOME_VALUE = "SomeValue";

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();

  private Map<String, String> headers;
  private CaseInsensitiveMap<String, String> caseInsensitiveHeaders;

  @BeforeEach
  public void setUp() {
    headers = new HashMap<>();
    headers.put(XOkapiHeaders.URL, URL);
    headers.put(XOkapiHeaders.TOKEN, TOKEN);
    headers.put(XOkapiHeaders.TENANT, TENANT);
    headers.put(SOME_HEADER, SOME_VALUE);

    caseInsensitiveHeaders = new CaseInsensitiveMap<>(headers);
  }

  @Test
  void notConstructedIfHeadersNull() {
    assertThrows(NullPointerException.class, () -> new OkapiParams(null));
  }

  @Test
  void notConstructedIfUrlIsMissingInHeaders() {
    headers.remove(XOkapiHeaders.URL);
    assertThrows(IllegalArgumentException.class, () -> new OkapiParams(headers));
  }

  @Test
  void notConstructedIfUrlIsInvalid() {
    headers.put(XOkapiHeaders.URL, "Invalid");

    assertThrows(IllegalArgumentException.class, () -> new OkapiParams(headers));
  }

  @Test
  void constructedFromValidHeaders() {
    OkapiParams params = new OkapiParams(headers);

    assertThat(params.getHost(), is(HOST));
    assertThat(params.getPort(), is(PORT));
    assertThat(params.getToken(), is(TOKEN));
    assertThat(params.getTenant(), is(TENANT));
  }

  @Test
  void constructedIfTokenAndTenantNotProvided() {
    headers.remove(XOkapiHeaders.TENANT);
    headers.remove(XOkapiHeaders.TOKEN);

    OkapiParams params = new OkapiParams(headers);

    assertThat(params.getHost(), is(HOST));
    assertThat(params.getPort(), is(PORT));
    assertThat(params.getToken(), nullValue());
    assertThat(params.getTenant(), nullValue());
  }

  @Test
  void returnsCaseInsensitiveHeaders() {
    OkapiParams params = new OkapiParams(headers);

    Map<String, String> actual = params.getHeaders();

    assertThat(actual.size(), is(caseInsensitiveHeaders.size()));
    caseInsensitiveHeaders.forEach((key, value) -> assertThat(actual.get(key), is(value)));
  }

  @Test
  void returnsACopyOfCaseInsensitiveHeaders() {
    OkapiParams params = new OkapiParams(headers);

    Map<String, String> headers = params.getHeaders();
    headers.remove(SOME_HEADER);

    headers = params.getHeaders();
    assertThat(headers.get(SOME_HEADER), is(SOME_VALUE));
  }
}
