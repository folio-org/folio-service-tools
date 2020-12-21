package org.folio.test.util;

import static org.folio.test.util.TestUtil.STUB_TENANT;
import static org.folio.test.util.TestUtil.STUB_TOKEN;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import org.folio.rest.RestVerticle;
import org.folio.rest.client.TenantClient;
import org.folio.rest.jaxrs.model.TenantAttributes;
import org.folio.rest.jaxrs.model.TenantJob;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.tools.PomReader;
import org.folio.rest.tools.utils.NetworkUtils;

public class TestSetUpHelper {

  private static final String HTTP_PORT = "http.port";
  private static final int TENANT_OP_WAITINGTIME = 60000;

  private static int port;
  private static String host;
  private static boolean started;
  private static Vertx vertx;
  private static PostgresClient pgClient;

  public static void startVertxAndPostgres() {
    startVertxAndPostgres(Collections.emptyMap());
  }

  public static void startVertxAndPostgres(Map<String, String> configProperties) {
    vertx = Vertx.vertx();
    port = NetworkUtils.nextFreePort();
    host = "http://127.0.0.1";

    try {
      // this's going to start embedded Postgres (see PostgresClient constructor)
      // or use the one from env properties
      pgClient = PostgresClient.getInstance(vertx);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to initialize postgres client", e);
    }

    CompletableFuture<Void> future = new CompletableFuture<>();
    vertx.deployVerticle(RestVerticle.class.getName(), getDeploymentOptions(configProperties), event -> {
      TenantClient tenantClient = new TenantClient(host + ":" + port, STUB_TENANT, STUB_TOKEN);
      try {
        TenantAttributes tenantAttributes = new TenantAttributes().withModuleTo(PomReader.INSTANCE.getVersion());
        tenantClient.postTenant(tenantAttributes, res1 -> {
          if (res1.succeeded()) {
            String jobId = res1.result().bodyAsJson(TenantJob.class).getId();
            tenantClient.getTenantByOperationId(jobId, TENANT_OP_WAITINGTIME, res2 -> {
              if (res2.succeeded()) {
                future.complete(null);
              } else {
                future.completeExceptionally(new IllegalStateException("Failed to get tenant"));
              }
            });
          } else {
            future.completeExceptionally(new IllegalStateException("Failed to create tenant job"));
          }
        });
      } catch (Exception e) {
        future.completeExceptionally(e);
      }
    });
    future.join();

    started = true;
  }

  public static void stopVertxAndPostgres() {
    CompletableFuture<Void> future = new CompletableFuture<>();
    vertx.close(res -> {
      PostgresClient.stopEmbeddedPostgres();
      pgClient = null;

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

  public static PostgresClient getPgClient() {
    return pgClient;
  }

  private static DeploymentOptions getDeploymentOptions(Map<String, String> configProperties) {
    JsonObject config = new JsonObject()
      .put(HTTP_PORT, port);
    configProperties.forEach(config::put);
    return new DeploymentOptions().setConfig(config
    );
  }
}
