package org.folio.db;

import static org.folio.test.util.TestUtil.STUB_TENANT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.folio.postgres.testing.PostgresTesterContainer;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.SQLConnection;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class DbUtilsTransactionTest {

  private static final String TEST_TABLE = "test_table";
  private static final String INVALID_VALUE = "'abc'";

  private static final Vertx VERTX = Vertx.vertx();

  @BeforeAll
  public static void setUpBeforeClass(VertxTestContext context) {
    var schema = PostgresClient.convertToPsqlStandard(STUB_TENANT);
    PostgresClient.setPostgresTester(new PostgresTesterContainer());
    var postgresClient = PostgresClient.getInstance(VERTX);
    postgresClient.startPostgresTester();
    postgresClient.execute(
        "CREATE ROLE " + schema + " PASSWORD '" + STUB_TENANT + "' LOGIN; "
        + "CREATE SCHEMA " + schema + " AUTHORIZATION " + schema + "; "
        + "CREATE TABLE " + schema + "." + TEST_TABLE + " (value INTEGER); "
        + "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA " + schema + " TO " + schema)
    .onComplete(context.succeeding(x -> context.completeNow()));
  }

  @AfterAll
  public static void tearDownAfterClass() {
    PostgresClient.stopPostgresTester();
  }

  @BeforeEach
  public void truncate(VertxTestContext context) {
    PostgresClient.getInstance(VERTX, STUB_TENANT)
    .execute("TRUNCATE " + TEST_TABLE)
    .onComplete(context.succeeding(x -> context.completeNow()));
  }

  @Test
  void shouldRollbackTransactionIfItFails(VertxTestContext context) {
    DbUtils.executeInTransactionWithVertxFuture(STUB_TENANT, VERTX, (client, connection) ->
        executeWithConnection(connection, "INSERT INTO " + TEST_TABLE + "(value) values(5)")
          .compose(
            o -> executeWithConnection(connection, "INSERT INTO " + TEST_TABLE + "(value) values(" + INVALID_VALUE + ")")))
      .onComplete(context.failing(e -> assertCount(0, context)));
  }

  @Test
  void shouldCommitTransactionIfItSucceeds(VertxTestContext context) {
    DbUtils.executeInTransactionWithVertxFuture(STUB_TENANT, VERTX, (client, connection) ->
      executeWithConnection(connection, "INSERT INTO " + TEST_TABLE + "(value) values(5)"))
      .onComplete(context.succeeding(x -> assertCount(1, context)));
  }

  @Test
  void shouldNotCommitTransactionIfItStillInProgress(VertxTestContext context) {
    DbUtils.executeInTransactionWithVertxFuture(STUB_TENANT, VERTX, (client, connection) ->
      executeWithConnection(connection, "INSERT INTO " + TEST_TABLE + "(value) values(5)")
        .compose(o -> assertCount(0, context))
        .compose(o -> executeWithConnection(connection, "INSERT INTO " + TEST_TABLE + "(value) values(5)")))
    .onComplete(context.succeeding(x -> assertCount(2, context)));
  }

  private Future<Void> assertCount(int expectedCount, VertxTestContext context) {
    return PostgresClient.getInstance(VERTX, STUB_TENANT)
        .selectSingle("SELECT COUNT(*) as count FROM " + TEST_TABLE)
        .onComplete(context.succeeding(row -> context.verify(() -> {
          assertThat(row.getInteger("count"), is(expectedCount));
          context.completeNow();
        })))
        .mapEmpty();
  }

  private Future<Void> executeWithConnection(AsyncResult<SQLConnection> connection, String sql) {
    Promise<RowSet<Row>> promise = Promise.promise();
    PostgresClient.getInstance(VERTX, STUB_TENANT).execute(connection, sql, promise);
    return promise.future().mapEmpty();
  }

}
