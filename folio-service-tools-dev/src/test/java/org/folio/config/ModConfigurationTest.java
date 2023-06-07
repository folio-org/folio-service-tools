package org.folio.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import static org.folio.test.util.TestUtil.mockGet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.apache.http.HttpStatus;
import org.folio.test.extensions.TestStartLoggingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.folio.common.OkapiParams;
import org.folio.okapi.common.XOkapiHeaders;

@ExtendWith(VertxExtension.class)
class ModConfigurationTest {

  private static final String STUB_TENANT = "testlib";
  private static final String STUB_TOKEN = "TEST_OKAPI_TOKEN";
  private static final String HOST = "http://127.0.0.1";

  private static final String CONFIG_MODULE = "NOTES";
  private static final String CONFIG_PROP_CODE = "note.types.number.limit";
  private static final RegexPattern CONFIG_NOTE_TYPE_LIMIT_URL_PATTERN =
    new RegexPattern("/configurations/entries.*");

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();

  @RegisterExtension
  static WireMockExtension wiremock = WireMockExtension.newInstance()
    .options(WireMockConfiguration.wireMockConfig()
      .dynamicPort()
      .notifier(new Slf4jNotifier(true))
      .extensions(new ResponseTemplateTransformer(true)))
    .configureStaticDsl(true)
    .build();

  private OkapiParams okapiParams;
  private ModConfiguration configuration;


  @BeforeEach
  public void setUp() {
    Map<String, String> headers = new HashMap<>();
    headers.put(XOkapiHeaders.TENANT, STUB_TENANT);
    headers.put(XOkapiHeaders.TOKEN, STUB_TOKEN);
    headers.put(XOkapiHeaders.URL, HOST + ":" + wiremock.getPort());

    okapiParams = new OkapiParams(headers);
    configuration = new ModConfiguration(CONFIG_MODULE);
  }

  @Test
  void failIfModuleNotProvidedToConstructor() {
    assertThrows(IllegalArgumentException.class, () -> new ModConfiguration(""));
  }

  @Test
  void shouldReturnPropAsString(VertxTestContext context) throws IOException, URISyntaxException {
    mockGet(CONFIG_NOTE_TYPE_LIMIT_URL_PATTERN, "responses/configuration/get-note-type-limit-5-response.json");

    Handler<AsyncResult<String>> verify = context.succeeding(result -> context.verify(() -> {
      assertNotNull(result);
      assertEquals("5", result);
      context.completeNow();
    }));

    configuration.getString(CONFIG_PROP_CODE, okapiParams).onComplete(verify);
  }

  @Test
  void shouldReturnPropAsInt(VertxTestContext context) throws IOException, URISyntaxException {
    mockGet(CONFIG_NOTE_TYPE_LIMIT_URL_PATTERN, "responses/configuration/get-note-type-limit-5-response.json");

    Handler<AsyncResult<Integer>> verify = context.succeeding(result -> context.verify(() -> {
      assertNotNull(result);
      assertEquals(5, result.intValue());
      context.completeNow();
    }));

    configuration.getInt(CONFIG_PROP_CODE, okapiParams).onComplete(verify);
  }

  @Test
  void shouldReturnPropAsLong(VertxTestContext context) throws IOException, URISyntaxException {
    mockGet(CONFIG_NOTE_TYPE_LIMIT_URL_PATTERN, "responses/configuration/get-note-type-limit-5-response.json");

    Handler<AsyncResult<Long>> verify = context.succeeding(result -> context.verify(() -> {
      assertNotNull(result);
      assertEquals(5L, result.longValue());
      context.completeNow();
    }));

    configuration.getLong(CONFIG_PROP_CODE, okapiParams).onComplete(verify);
  }

  @Test
  void shouldReturnPropAsDouble(VertxTestContext context) throws IOException, URISyntaxException {
    mockGet(CONFIG_NOTE_TYPE_LIMIT_URL_PATTERN, "responses/configuration/get-note-type-limit-5-response.json");

    Handler<AsyncResult<Double>> verify = context.succeeding(result -> context.verify(() -> {
      assertNotNull(result);
      assertEquals(5d, result, 0);
      context.completeNow();
    }));

    configuration.getDouble(CONFIG_PROP_CODE, okapiParams).onComplete(verify);
  }

  @Test
  void shouldReturnDefaultStringInCaseOfFailure(VertxTestContext context) {
    mockGet(CONFIG_NOTE_TYPE_LIMIT_URL_PATTERN, HttpStatus.SC_BAD_REQUEST);

    Handler<AsyncResult<String>> verify = context.succeeding(result -> context.verify(() -> {
      assertNotNull(result);
      assertEquals("10", result);
      context.completeNow();
    }));

    configuration.getString(CONFIG_PROP_CODE, "10", okapiParams).onComplete(verify);
  }

  @Test
  void shouldReturnDefaultIntInCaseOfFailure(VertxTestContext context) {
    mockGet(CONFIG_NOTE_TYPE_LIMIT_URL_PATTERN, HttpStatus.SC_BAD_REQUEST);

    Handler<AsyncResult<Integer>> verify = context.succeeding(result -> context.verify(() -> {
      assertNotNull(result);
      assertEquals(Integer.MAX_VALUE, result.intValue());
      context.completeNow();
    }));

    configuration.getInt(CONFIG_PROP_CODE, Integer.MAX_VALUE, okapiParams).onComplete(verify);
  }

  @Test
  void shouldReturnDefaultLongInCaseOfFailure(VertxTestContext context) {
    mockGet(CONFIG_NOTE_TYPE_LIMIT_URL_PATTERN, HttpStatus.SC_BAD_REQUEST);

    Handler<AsyncResult<Long>> verify = context.succeeding(result -> context.verify(() -> {
      assertNotNull(result);
      assertEquals(Long.MAX_VALUE, result.longValue());
      context.completeNow();
    }));

    configuration.getLong(CONFIG_PROP_CODE, Long.MAX_VALUE, okapiParams).onComplete(verify);
  }

  @Test
  void shouldReturnDefaultDoubleInCaseOfFailure(VertxTestContext context) {
    mockGet(CONFIG_NOTE_TYPE_LIMIT_URL_PATTERN, HttpStatus.SC_BAD_REQUEST);

    Handler<AsyncResult<Double>> verify = context.succeeding(result -> context.verify(() -> {
      assertNotNull(result);
      assertEquals(Double.MAX_VALUE, result, 0);
      context.completeNow();
    }));

    configuration.getDouble(CONFIG_PROP_CODE, Double.MAX_VALUE, okapiParams).onComplete(verify);
  }

  @Test
  void shouldReturnDefaultBooleanInCaseOfFailure(VertxTestContext context) {
    mockGet(CONFIG_NOTE_TYPE_LIMIT_URL_PATTERN, HttpStatus.SC_BAD_REQUEST);

    Handler<AsyncResult<Boolean>> verify = context.succeeding(result -> context.verify(() -> {
      assertNotNull(result);
      assertTrue(result);
      context.completeNow();
    }));

    configuration.getBoolean(CONFIG_PROP_CODE, true, okapiParams).onComplete(verify);
  }

  @Test
  void failIfPropNotFound(VertxTestContext context) throws IOException, URISyntaxException {
    mockGet(CONFIG_NOTE_TYPE_LIMIT_URL_PATTERN, "responses/configuration/get-note-type-limit-empty-response.json");

    Handler<AsyncResult<String>> verify = context.failing(ex -> context.verify(() -> {
      assertTrue(ex instanceof PropertyNotFoundException);
      assertEquals(CONFIG_PROP_CODE, ((PropertyNotFoundException) ex).getPropertyCode());
      context.completeNow();
    }));

    configuration.getString(CONFIG_PROP_CODE, okapiParams).onComplete(verify);
  }

  @Test
  void failIfPropDisabled(VertxTestContext context) throws IOException, URISyntaxException {
    mockGet(CONFIG_NOTE_TYPE_LIMIT_URL_PATTERN, "responses/configuration/get-note-type-limit-disabled-response.json");

    Handler<AsyncResult<String>> verify = context.failing(ex -> context.verify(() -> {
      assertTrue(ex instanceof PropertyNotFoundException);
      assertEquals(CONFIG_PROP_CODE, ((PropertyNotFoundException) ex).getPropertyCode());
      context.completeNow();
    }));

    configuration.getString(CONFIG_PROP_CODE, okapiParams).onComplete(verify);
  }

  @Test
  void failIfResponseNotOk(VertxTestContext context) {
    mockGet(CONFIG_NOTE_TYPE_LIMIT_URL_PATTERN, HttpStatus.SC_BAD_REQUEST);

    Handler<AsyncResult<String>> verify = context.failing(ex -> context.verify(() -> {
      assertTrue(ex instanceof ConfigurationException);
      assertThat(ex.getMessage(), containsString(String.valueOf(HttpStatus.SC_BAD_REQUEST)));
      context.completeNow();
    }));

    configuration.getString(CONFIG_PROP_CODE, okapiParams).onComplete(verify);
  }

  @Test
  void failIfNoValueInConfig(VertxTestContext context) throws IOException, URISyntaxException {
    mockGet(CONFIG_NOTE_TYPE_LIMIT_URL_PATTERN, "responses/configuration/get-note-type-limit-no-value-response.json");

    Handler<AsyncResult<String>> verify = context.failing(ex -> context.verify(() -> {
      assertTrue(ex instanceof ValueNotDefinedException);
      assertEquals(CONFIG_PROP_CODE, ((ValueNotDefinedException) ex).getPropertyCode());
      context.completeNow();
    }));

    configuration.getString(CONFIG_PROP_CODE, okapiParams).onComplete(verify);
  }

  @Test
  void failIfSeveralPropsPresent(VertxTestContext context) throws IOException, URISyntaxException {
    mockGet(CONFIG_NOTE_TYPE_LIMIT_URL_PATTERN, "responses/configuration/get-note-type-limit-not-unique-response.json");

    Handler<AsyncResult<String>> verify = context.failing(ex -> context.verify(() -> {
      assertTrue(ex instanceof PropertyException);
      assertEquals(CONFIG_PROP_CODE, ((PropertyException) ex).getPropertyCode());
      assertThat(ex.getMessage(), containsString("more than one configuration properties found"));
      context.completeNow();
    }));

    configuration.getString(CONFIG_PROP_CODE, okapiParams).onComplete(verify);
  }
}
