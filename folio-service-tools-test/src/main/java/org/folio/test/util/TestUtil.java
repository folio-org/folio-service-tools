package org.folio.test.util;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import io.vertx.core.json.Json;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestUtil {

  public static final String STUB_TENANT = "fs";
  public static final String STUB_TOKEN = "TEST_OKAPI_TOKEN";
  private static final Logger LOG = LogManager.getLogger("Test");

  private TestUtil() {
  }

  public static Logger logger() {
    return LOG;
  }

  /**
   * Reads file from classpath as String
   */
  public static String readFile(String filename) throws IOException, URISyntaxException {
    return FileUtils.readFileToString(getFile(filename), StandardCharsets.UTF_8);
  }

  /**
   * Reads json file from classpath and parses it into object of specified class
   */
  public static <T> T readJsonFile(String filename, Class<T> valueType) throws IOException, URISyntaxException {
    return new ObjectMapper().readValue(FileUtils.readFileToString(getFile(filename), StandardCharsets.UTF_8), valueType);
  }

  /**
   * Returns File object corresponding to the file on classpath with specified filename
   */
  public static File getFile(String filename) throws URISyntaxException {
    return new File(TestUtil.class.getClassLoader()
      .getResource(filename).toURI());
  }

  public static String toJson(Object object) {
    return Json.encode(object);
  }


  public static void mockGet(StringValuePattern urlPattern, String responseFile) throws IOException, URISyntaxException {
    mockGetWithBody(urlPattern, readFile(responseFile));
  }

  public static void mockGetWithBody(StringValuePattern urlPattern, String body) {
    stubFor(get(new UrlPathPattern(urlPattern, (urlPattern instanceof RegexPattern)))
      .willReturn(new ResponseDefinitionBuilder()
        .withBody(body)));
  }

  public static void mockGet(StringValuePattern urlPattern, int status) {
    stubFor(get(new UrlPathPattern(urlPattern, (urlPattern instanceof RegexPattern)))
      .willReturn(new ResponseDefinitionBuilder().withStatus(status)));
  }

  public static void mockPost(StringValuePattern urlPattern, ContentPattern<?> body, String response, int status) throws IOException, URISyntaxException {
    stubFor(post(new UrlPathPattern(urlPattern, (urlPattern instanceof RegexPattern)))
      .withRequestBody(body)
      .willReturn(new ResponseDefinitionBuilder()
        .withBody(readFile(response))
        .withStatus(status)));
  }

  public static void mockPut(StringValuePattern urlPattern, ContentPattern<?> content, int status) {
    stubFor(put(new UrlPathPattern(urlPattern, (urlPattern instanceof RegexPattern)))
      .withRequestBody(content)
      .willReturn(new ResponseDefinitionBuilder()
        .withStatus(status)));
  }

  public static void mockPut(StringValuePattern urlPattern, int status) {
    stubFor(put(new UrlPathPattern(urlPattern, (urlPattern instanceof RegexPattern)))
      .willReturn(new ResponseDefinitionBuilder()
        .withStatus(status)));
  }

  public static void mockResponseList(UrlPathPattern urlPattern, ResponseDefinitionBuilder... responses) {
    int scenarioStep = 0;
    String scenarioName = "Scenario -" + UUID.randomUUID();
    for (ResponseDefinitionBuilder response : responses) {
      if (scenarioStep == 0) {
        stubFor(
          get(urlPattern)
            .inScenario(scenarioName)
            .willSetStateTo(String.valueOf(++scenarioStep))
            .willReturn(response));
      } else {
        stubFor(
          get(urlPattern)
            .inScenario(scenarioName)
            .whenScenarioStateIs(String.valueOf(scenarioStep))
            .willSetStateTo(String.valueOf(++scenarioStep))
            .willReturn(response));
      }
    }
  }

}
