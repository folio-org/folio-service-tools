package org.folio.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import org.folio.okapi.common.XOkapiHeaders;
import org.folio.test.junit.TestStartLoggingRule;


public class OkapiParamsTest {

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

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();

  private Map<String, String> headers;
  private CaseInsensitiveMap<String, String> caseInsensitiveHeaders;

  @Before
  public void setUp() {
    headers = new HashMap<>();
    headers.put(XOkapiHeaders.URL, URL);
    headers.put(XOkapiHeaders.TOKEN, TOKEN);
    headers.put(XOkapiHeaders.TENANT, TENANT);
    headers.put(SOME_HEADER, SOME_VALUE);

    caseInsensitiveHeaders = new CaseInsensitiveMap<>(headers);
  }

  @Test(expected = NullPointerException.class)
  public void notConstructedIfHeadersNull() {
    new OkapiParams(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void notConstructedIfUrlIsMissingInHeaders() {
    headers.remove(XOkapiHeaders.URL);

    new OkapiParams(headers);
  }

  @Test(expected = IllegalArgumentException.class)
  public void notConstructedIfUrlIsInvalid() {
    headers.put(XOkapiHeaders.URL, "Invalid");

    new OkapiParams(headers);
  }

  @Test
  public void constructedFromValidHeaders() {
    OkapiParams params = new OkapiParams(headers);

    assertThat(params.getHost(), is(HOST));
    assertThat(params.getPort(), is(PORT));
    assertThat(params.getToken(), is(TOKEN));
    assertThat(params.getTenant(), is(TENANT));
  }

  @Test
  public void constructedIfTokenAndTenantNotProvided() {
    headers.remove(XOkapiHeaders.TENANT);
    headers.remove(XOkapiHeaders.TOKEN);

    OkapiParams params = new OkapiParams(headers);

    assertThat(params.getHost(), is(HOST));
    assertThat(params.getPort(), is(PORT));
    assertThat(params.getToken(), nullValue());
    assertThat(params.getTenant(), nullValue());
  }

  @Test
  public void returnsCaseInsensitiveHeaders() {
    OkapiParams params = new OkapiParams(headers);

    Map<String, String> actual = params.getHeaders();

    assertThat(actual.size(), is(caseInsensitiveHeaders.size()));
    caseInsensitiveHeaders.forEach((key, value) -> assertThat(actual.get(key), is(value)));
  }

  @Test
  public void returnsACopyOfCaseInsensitiveHeaders() {
    OkapiParams params = new OkapiParams(headers);

    Map<String, String> headers = params.getHeaders();
    headers.remove(SOME_HEADER);

    headers = params.getHeaders();
    assertThat(headers.get(SOME_HEADER), is(SOME_VALUE));
  }
}
