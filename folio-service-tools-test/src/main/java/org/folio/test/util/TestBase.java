package org.folio.test.util;

import static io.restassured.RestAssured.given;

import static org.folio.test.util.TestUtil.STUB_TENANT;

import java.util.HashMap;

import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;

import org.folio.okapi.common.XOkapiHeaders;
import org.folio.test.junit.TestStartLoggingRule;

/**
 * Base test class for tests that use wiremock and vertx http servers,
 * test that inherits this class must use VertxUnitRunner as test runner
 * <p>
 * <p>
 * BeforeClass and AfterClass methods have to run in order for this class to work correctly,
 * if superclass declares its own @BeforeClass or @AfterClass methods, then they shouldn't hide existing
 * methods (i.e. they should have different names).
 * Alternatively superclass can hide existing methods and call TestBase#setUpClass/TestBase#tearDownClass methods directly.
 *
 * @see BeforeClass
 * @see AfterClass
 */
public class TestBase {

  protected static final Header JSON_CONTENT_TYPE_HEADER = new Header(HttpHeaders.CONTENT_TYPE,
    ContentType.APPLICATION_JSON.getMimeType());

  private static final String STUB_TOKEN = "TEST_OKAPI_TOKEN";
  protected static HashMap<String, String> configProperties = new HashMap<>();
  protected static int port;
  protected static String host;
  protected static Vertx vertx;

  private static boolean needTeardown;

  @Rule
  public TestRule watcher = TestStartLoggingRule.instance();

  @Rule
  public WireMockRule wiremockServer = new WireMockRule(
    WireMockConfiguration.wireMockConfig()
      .dynamicPort()
      .notifier(new Slf4jNotifier(true)));


  /**
   * @param context This parameter is added so that superclasses can hide this method by declaring their own
   *                setUpClass(TestContext context) method.
   */
  @BeforeClass
  public static void setUpClass(TestContext context) {
    if (!TestSetUpHelper.isStarted()) {
      TestSetUpHelper.startVertxAndPostgres(configProperties);
      needTeardown = true;
    } else {
      needTeardown = false;
    }
    port = TestSetUpHelper.getPort();
    host = TestSetUpHelper.getHost();
    vertx = TestSetUpHelper.getVertx();
  }

  @AfterClass
  public static void tearDownClass(TestContext context) {
    if (needTeardown) {
      TestSetUpHelper.stopVertxAndPostgres();
    }
  }


  protected RequestSpecification getRequestSpecification() {
    return new RequestSpecBuilder()
      .addHeader(XOkapiHeaders.TENANT, STUB_TENANT)
      .addHeader(XOkapiHeaders.TOKEN, STUB_TOKEN)
      .addHeader(XOkapiHeaders.URL, getWiremockUrl())
      .setBaseUri(host + ":" + port)
      .setPort(port)
      .log(LogDetail.ALL)
      .build();
  }

  protected RequestSpecification givenWithUrl() {
    return new RequestSpecBuilder()
      .addHeader(XOkapiHeaders.URL, getWiremockUrl())
      .setBaseUri(host + ":" + port)
      .setPort(port)
      .log(LogDetail.ALL)
      .build();
  }

  /**
   * Returns url of Wiremock server used in this test
   */
  protected String getWiremockUrl() {
    return host + ":" + wiremockServer.port();
  }

  protected ExtractableResponse<Response> getWithOk(String resourcePath) {
    return getWithStatus(resourcePath, HttpStatus.SC_OK);
  }

  protected ExtractableResponse<Response> deleteWithNoContent(String resourcePath) {
    return deleteWithStatus(resourcePath, HttpStatus.SC_NO_CONTENT);
  }

  protected ExtractableResponse<Response> putWithNoContent(String resourcePath, String putBody, Header... headers) {
    return putWithStatus(resourcePath, putBody, HttpStatus.SC_NO_CONTENT, headers);
  }

  protected ExtractableResponse<Response> getWithStatus(String resourcePath, int expectedStatus) {
    return given()
      .spec(getRequestSpecification())
      .when()
      .get(resourcePath)
      .then()
      .log().ifValidationFails()
      .statusCode(expectedStatus).extract();
  }

  protected ValidatableResponse getWithValidateBody(String resourcePath, int expectedStatus) {
    return given()
      .spec(getRequestSpecification())
      .when()
      .get(resourcePath)
      .then()
      .log().ifValidationFails()
      .statusCode(expectedStatus);
  }

  protected ExtractableResponse<Response> putWithStatus(String resourcePath, String putBody,
                                                        int expectedStatus, Header... headers) {
    return given()
      .spec(getRequestSpecification())
      .header(JSON_CONTENT_TYPE_HEADER)
      .headers(new Headers(headers))
      .body(putBody)
      .when()
      .put(resourcePath)
      .then()
      .log().ifValidationFails()
      .statusCode(expectedStatus)
      .extract();
  }

  protected ExtractableResponse<Response> postWithStatus(String resourcePath, String postBody,
                                                         int expectedStatus, Header... headers) {
    return given()
      .spec(getRequestSpecification())
      .header(JSON_CONTENT_TYPE_HEADER)
      .headers(new Headers(headers))
      .body(postBody)
      .when()
      .post(resourcePath)
      .then()
      .log().ifValidationFails()
      .statusCode(expectedStatus)
      .extract();
  }

  protected ExtractableResponse<Response> deleteWithStatus(String resourcePath, int expectedStatus) {
    return given()
      .spec(getRequestSpecification())
      .when()
      .delete(resourcePath)
      .then()
      .log().ifValidationFails()
      .statusCode(expectedStatus)
      .extract();
  }
}
