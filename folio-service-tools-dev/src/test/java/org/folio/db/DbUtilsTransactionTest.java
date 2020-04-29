package org.folio.db;

import static org.folio.test.util.TestUtil.STUB_TENANT;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.jetbrains.annotations.NotNull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.folio.rest.persist.PostgresClient;


@RunWith(VertxUnitRunner.class)
public class DbUtilsTransactionTest {

  private static final String TEST_TABLE = "test_table";
  private static final String INVALID_VALUE = "'abc'";
  private static Vertx vertx = Vertx.vertx();

  @BeforeClass
  public static void setUpBeforeClass() throws IOException {
    PostgresClient.getInstance(vertx)
      .startEmbeddedPostgres();
    CompletableFuture<Void> future = new CompletableFuture<>();
    PostgresClient.getInstance(vertx).execute(
      "CREATE TABLE " + TEST_TABLE + "(value INTEGER)",
      event -> future.complete(null));
    future.join();
  }

  @AfterClass
  public static void tearDownAfterClass() {
    PostgresClient
      .stopEmbeddedPostgres();
  }

  @Test
  public void shouldRollbackTransactionIfItFails(TestContext context) {
    DbUtils.executeInTransactionWithVertxFuture(STUB_TENANT, vertx, (client, connection) ->
      executeWithConnection(connection, "INSERT INTO " + TEST_TABLE + "(value) values(5)")
        .compose(o -> executeWithConnection(connection, "INSERT INTO " + TEST_TABLE + "(value) values(" + INVALID_VALUE + ")")))
    .setHandler(assertCount(0, context));
  }

  @Test
  public void shouldCommitTransactionIfItSucceeds(TestContext context) {
    DbUtils.executeInTransactionWithVertxFuture(STUB_TENANT, vertx, (client, connection) ->
      executeWithConnection(connection, "INSERT INTO " + TEST_TABLE + "(value) values(5)"))
      .setHandler((result) -> assertCount(1, context));
  }

  @Test
  public void shouldNotCommitTransactionIfItStillInProgress(TestContext context) {
    DbUtils.executeInTransactionWithVertxFuture(STUB_TENANT, vertx, (client, connection) ->
      executeWithConnection(connection, "INSERT INTO " + TEST_TABLE + "(value) values(5)")
    .compose(o -> assertCount(0, context))
    .compose(o -> executeWithConnection(connection, "INSERT INTO " + TEST_TABLE + "(value) values(5)")))
      .setHandler((result) -> assertCount(2, context));
  }

  private Future<Void> assertCount(int expectedCount, TestContext context) {
      Promise<ResultSet> promise = Promise.promise();
      PostgresClient.getInstance(vertx).select("SELECT COUNT(*) as count FROM " + TEST_TABLE, promise);
      return promise.future().map(resultSet -> {
        String count = resultSet.getRows().get(0).getString("count");
        context.assertEquals(expectedCount, count);
        return null;
    });
  }

  @NotNull
  private Future<Void> executeWithConnection(AsyncResult<SQLConnection> connection, String sql) {
    Promise<Void> promise = Promise.promise();
    PostgresClient.getInstance(vertx).execute(
      connection, sql,
      event -> promise.complete(null));
    return promise.future();
  }

}
