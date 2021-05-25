package org.folio.db;

import static org.folio.test.util.TestUtil.STUB_TENANT;

import java.util.concurrent.CompletableFuture;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.folio.postgres.testing.PostgresTesterContainer;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.SQLConnection;


@RunWith(VertxUnitRunner.class)
public class DbUtilsTransactionTest {

  private static final String TEST_TABLE = "test_table";
  private static final String INVALID_VALUE = "'abc'";

  private static final Vertx VERTX = Vertx.vertx();

  @BeforeClass
  public static void setUpBeforeClass() {
    PostgresClient.setPostgresTester(new PostgresTesterContainer());
    PostgresClient.getInstance(VERTX).startPostgresTester();
    CompletableFuture<Void> future = new CompletableFuture<>();
    PostgresClient.getInstance(VERTX).execute(
      "CREATE TABLE " + TEST_TABLE + "(value INTEGER)",
      event -> future.complete(null));
    future.join();
  }

  @AfterClass
  public static void tearDownAfterClass() {
    PostgresClient.stopPostgresTester();
  }

  @Test
  public void shouldRollbackTransactionIfItFails(TestContext context) {
    DbUtils.executeInTransactionWithVertxFuture(STUB_TENANT, VERTX, (client, connection) ->
      executeWithConnection(connection, "INSERT INTO " + TEST_TABLE + "(value) values(5)")
        .compose(
          o -> executeWithConnection(connection, "INSERT INTO " + TEST_TABLE + "(value) values(" + INVALID_VALUE + ")")))
      .onComplete(v -> assertCount(0, context));
  }

  @Test
  public void shouldCommitTransactionIfItSucceeds(TestContext context) {
    DbUtils.executeInTransactionWithVertxFuture(STUB_TENANT, VERTX, (client, connection) ->
      executeWithConnection(connection, "INSERT INTO " + TEST_TABLE + "(value) values(5)"))
      .onComplete((result) -> assertCount(1, context));
  }

  @Test
  public void shouldNotCommitTransactionIfItStillInProgress(TestContext context) {
    DbUtils.executeInTransactionWithVertxFuture(STUB_TENANT, VERTX, (client, connection) ->
      executeWithConnection(connection, "INSERT INTO " + TEST_TABLE + "(value) values(5)")
        .compose(o -> assertCount(0, context))
        .compose(o -> executeWithConnection(connection, "INSERT INTO " + TEST_TABLE + "(value) values(5)")))
      .onComplete((result) -> assertCount(2, context));
  }

  private Future<Void> assertCount(int expectedCount, TestContext context) {
    Promise<Row> promise = Promise.promise();
    PostgresClient.getInstance(VERTX).selectSingle("SELECT COUNT(*) as count FROM " + TEST_TABLE, promise);
    return promise.future().map(row -> {
      String count = row.getString("count");
      context.assertEquals(expectedCount, count);
      return null;
    });
  }

  private Future<Void> executeWithConnection(AsyncResult<SQLConnection> connection, String sql) {
    Promise<Void> promise = Promise.promise();
    PostgresClient.getInstance(VERTX).execute(
      connection, sql,
      event -> promise.complete(null));
    return promise.future();
  }

}
