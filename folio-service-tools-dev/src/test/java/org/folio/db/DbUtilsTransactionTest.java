package org.folio.db;

import static org.folio.test.util.TestUtil.STUB_TENANT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.junit.AfterClass;
import org.junit.Before;
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
  public static void setUpBeforeClass(TestContext context) {
    var schema = PostgresClient.convertToPsqlStandard(STUB_TENANT);
    PostgresClient.setPostgresTester(new PostgresTesterContainer());
    var postgresClient = PostgresClient.getInstance(VERTX);
    postgresClient.startPostgresTester();
    postgresClient.execute(
        "CREATE ROLE " + schema + " PASSWORD '" + STUB_TENANT + "' LOGIN; "
        + "CREATE SCHEMA " + schema + " AUTHORIZATION " + schema + "; "
        + "CREATE TABLE " + schema + "." + TEST_TABLE + " (value INTEGER); "
        + "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA " + schema + " TO " + schema)
    .onComplete(context.asyncAssertSuccess());
  }

  @AfterClass
  public static void tearDownAfterClass() {
    PostgresClient.stopPostgresTester();
  }

  @Before
  public void truncate(TestContext context) {
    PostgresClient.getInstance(VERTX, STUB_TENANT)
    .execute("TRUNCATE " + TEST_TABLE)
    .onComplete(context.asyncAssertSuccess());
  }

  @Test
  public void shouldRollbackTransactionIfItFails(TestContext context) {
    DbUtils.executeInTransactionWithVertxFuture(STUB_TENANT, VERTX, (client, connection) ->
      executeWithConnection(connection, "INSERT INTO " + TEST_TABLE + "(value) values(5)")
        .compose(
          o -> executeWithConnection(connection, "INSERT INTO " + TEST_TABLE + "(value) values(" + INVALID_VALUE + ")")))
      .onComplete(context.asyncAssertFailure(e -> assertCount(0, context)));
  }

  @Test
  public void shouldCommitTransactionIfItSucceeds(TestContext context) {
    DbUtils.executeInTransactionWithVertxFuture(STUB_TENANT, VERTX, (client, connection) ->
      executeWithConnection(connection, "INSERT INTO " + TEST_TABLE + "(value) values(5)"))
      .onComplete(context.asyncAssertSuccess(x -> assertCount(1, context)));
  }

  @Test
  public void shouldNotCommitTransactionIfItStillInProgress(TestContext context) {
    DbUtils.executeInTransactionWithVertxFuture(STUB_TENANT, VERTX, (client, connection) ->
      executeWithConnection(connection, "INSERT INTO " + TEST_TABLE + "(value) values(5)")
        .compose(o -> assertCount(0, context))
        .compose(o -> executeWithConnection(connection, "INSERT INTO " + TEST_TABLE + "(value) values(5)")))
    .onComplete(context.asyncAssertSuccess(x -> assertCount(2, context)));
  }

  private Future<Void> assertCount(int expectedCount, TestContext context) {
    return PostgresClient.getInstance(VERTX, STUB_TENANT)
        .selectSingle("SELECT COUNT(*) as count FROM " + TEST_TABLE)
        .onComplete(context.asyncAssertSuccess(row -> {
          assertThat(row.getInteger("count"), is(expectedCount));
        }))
        .mapEmpty();
  }

  private Future<Void> executeWithConnection(AsyncResult<SQLConnection> connection, String sql) {
    Promise<RowSet<Row>> promise = Promise.promise();
    PostgresClient.getInstance(VERTX, STUB_TENANT).execute(connection, sql, promise);
    return promise.future().mapEmpty();
  }

}
