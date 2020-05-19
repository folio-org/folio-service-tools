package org.folio.test.util;

import static org.folio.test.util.TestUtil.STUB_TENANT;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.vertx.core.Vertx;

import org.folio.cql2pgjson.CQL2PgJSON;
import org.folio.cql2pgjson.exception.FieldException;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.cql.CQLWrapper;

public class DBTestUtil {

  private static final String JSONB_COLUMN = "jsonb";

  private DBTestUtil() {
  }

  public static void save(String stubId, Object object, Vertx vertx, String tableName) {
    save(stubId, object, vertx, tableName, STUB_TENANT);
  }

  public static void save(String stubId, Object object, Vertx vertx, String tableName, String tenantId) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    PostgresClient.getInstance(vertx, tenantId).save(tableName, stubId, object,
      event -> future.complete(null));
    future.join();
  }

  public static <T> List<T> getAll(Class<T> valueType, Vertx vertx, String tableName) {
    return getAll(valueType, vertx, tableName, STUB_TENANT);
  }

  public static <T> List<T> getAll(Class<T> valueType, Vertx vertx, String tableName, String tenantId) {
    CompletableFuture<List<T>> future = new CompletableFuture<>();
    PostgresClient.getInstance(vertx, tenantId).get(tableName, valueType, new CQLWrapper().setWhereClause(""),
      false, results -> future.complete(results.result().getResults()));
    return future.join();
  }

  public static void deleteFromTable(Vertx vertx, String tableName) {
    deleteFromTable(vertx, tableName, STUB_TENANT);
  }

  public static void deleteFromTable(Vertx vertx, String tableName, String tenantId) {
    try {
      CompletableFuture<Void> future = new CompletableFuture<>();
      PostgresClient.getInstance(vertx, tenantId).delete(tableName,
        new CQLWrapper(new CQL2PgJSON(JSONB_COLUMN), "cql.allRecords=1"),
        event -> future.complete(null));
      future.join();
    } catch (FieldException e) {
      throw new IllegalStateException(e);
    }
  }
}
