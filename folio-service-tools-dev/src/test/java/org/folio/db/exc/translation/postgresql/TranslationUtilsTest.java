package org.folio.db.exc.translation.postgresql;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.folio.db.ErrorFactory.getErrorMapWithPsqlStateNull;
import static org.folio.db.ErrorFactory.getPrimaryKeyErrorMap;
import static org.folio.db.exc.translation.postgresql.TranslationUtils.exceptionWithSQLState;
import static org.folio.db.exc.translation.postgresql.TranslationUtils.exceptionWithSQLStateClass;
import static org.folio.rest.persist.PgExceptionUtil.createPgExceptionFromMap;

import io.vertx.pgclient.PgException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import org.folio.test.junit.TestStartLoggingRule;

public class TranslationUtilsTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();

  @Test
  public void shouldReturnTrueWhenPsqlStateBelongsToStateClass() {
    PgException exception = createPgExceptionFromMap(getPrimaryKeyErrorMap());
    boolean isExceptionWithState = exceptionWithSQLStateClass(exception, PSQLState.UNIQUE_VIOLATION);

    assertTrue(isExceptionWithState);
  }

  @Test
  public void shouldReturnFalseWhenPsqlStateDoesNotBelongToStateClass() {
    PgException exception = createPgExceptionFromMap(getPrimaryKeyErrorMap());
    boolean isExceptionWithState = exceptionWithSQLStateClass(exception, PSQLState.DUPLICATE_COLUMN);

    assertFalse(isExceptionWithState);
  }

  @Test
  public void shouldReturnFalseWhenPsqlStateClassIsNull() {
    PgException exception = createPgExceptionFromMap(getErrorMapWithPsqlStateNull());
    boolean isExceptionWithState = exceptionWithSQLStateClass(exception, PSQLState.INVALID_COLUMN_REFERENCE);

    assertFalse(isExceptionWithState);
  }

  @Test
  public void shouldReturnTrueWhenPsqlStateEqualsToState() {
    PgException exception = createPgExceptionFromMap(getPrimaryKeyErrorMap());
    boolean isExceptionWithState = exceptionWithSQLState(exception, PSQLState.UNIQUE_VIOLATION);

    assertTrue(isExceptionWithState);
  }

  @Test
  public void shouldReturnFalseWhenPsqlStateDoesNotEqualToState() {
    PgException exception = createPgExceptionFromMap(getPrimaryKeyErrorMap());
    boolean isExceptionWithState = exceptionWithSQLState(exception, PSQLState.DATABASE_DROPPED);

    assertFalse(isExceptionWithState);
  }

  @Test
  public void shouldReturnFalseWhenPsqlStateIsNull() {
    PgException exception = createPgExceptionFromMap(getErrorMapWithPsqlStateNull());
    boolean isExceptionWithState = exceptionWithSQLState(exception, PSQLState.DUPLICATE_FILE);

    assertFalse(isExceptionWithState);
  }
}
