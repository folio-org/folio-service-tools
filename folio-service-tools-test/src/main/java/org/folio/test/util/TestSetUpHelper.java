package org.folio.test.util;

import static org.folio.test.util.TestUtil.STUB_TENANT;
import static org.folio.test.util.TestUtil.STUB_TOKEN;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.folio.rest.RestVerticle;
import org.folio.rest.client.TenantClient;
import org.folio.rest.jaxrs.model.TenantAttributes;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.tools.PomReader;
import org.folio.rest.tools.utils.NetworkUtils;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.util.Map;

public class TestSetUpHelper {
  private static final String HTTP_PORT = "http.port";

  private static int port;
  private static String host;
  private static boolean started;
  private static Vertx vertx;

  public static void startVertxAndPostgres() {
    startVertxAndPostgres(Collections.emptyMap());
  }

  public static void startVertxAndPostgres(Map<String, String> configProperties) {
    vertx = Vertx.vertx();
    port = NetworkUtils.nextFreePort();
    host = "http://127.0.0.1";


    try {
      PostgresClient.getInstance(vertx)
        .startEmbeddedPostgres();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to start embedded postgres" , e);
    }

    CompletableFuture<Void> future = new CompletableFuture<>();
    vertx.deployVerticle(RestVerticle.class.getName(), getDeploymentOptions(configProperties), event -> {
      TenantClient tenantClient = new TenantClient(host + ":" + port, STUB_TENANT, STUB_TOKEN);
      try {
        tenantClient.postTenant(new TenantAttributes().withModuleTo(PomReader.INSTANCE.getVersion()), res2 -> future.complete(null));
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    future.join();

    started = true;
  }

  public static void stopVertxAndPostgres() {
    CompletableFuture<Void> future = new CompletableFuture<>();
    vertx.close(res -> {
      PostgresClient.stopEmbeddedPostgres();
      future.complete(null);
    });
    future.join();
    started = false;
  }

  public static boolean isStarted() {
    return started;
  }

  public static int getPort() {
    return port;
  }

  public static String getHost() {
    return host;
  }

  public static Vertx getVertx() {
    return vertx;
  }


  private static DeploymentOptions getDeploymentOptions(Map<String, String> configProperties) {
    JsonObject config = new JsonObject()
      .put(HTTP_PORT, port);
    configProperties.forEach(config::put);
    return new DeploymentOptions().setConfig(config
    );
  }
}
