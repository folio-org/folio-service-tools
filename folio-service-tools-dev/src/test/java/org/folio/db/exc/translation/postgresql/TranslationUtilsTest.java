package org.folio.db.exc.translation.postgresql;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.folio.db.ErrorFactory.getErrorMapWithPsqlStateNull;
import static org.folio.db.ErrorFactory.getPrimaryKeyErrorMap;
import static org.folio.db.exc.translation.postgresql.TranslationUtils.exceptionWithSQLState;
import static org.folio.db.exc.translation.postgresql.TranslationUtils.exceptionWithSQLStateClass;
import static org.folio.rest.persist.PgExceptionUtil.createPgExceptionFromMap;

import io.vertx.pgclient.PgException;
import org.folio.test.extensions.TestStartLoggingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class TranslationUtilsTest {

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();

  @Test
  void shouldReturnTrueWhenPsqlStateBelongsToStateClass() {
    PgException exception = createPgExceptionFromMap(getPrimaryKeyErrorMap());
    boolean isExceptionWithState = exceptionWithSQLStateClass(exception, PSQLState.UNIQUE_VIOLATION);

    assertTrue(isExceptionWithState);
  }

  @Test
  void shouldReturnFalseWhenPsqlStateDoesNotBelongToStateClass() {
    PgException exception = createPgExceptionFromMap(getPrimaryKeyErrorMap());
    boolean isExceptionWithState = exceptionWithSQLStateClass(exception, PSQLState.DUPLICATE_COLUMN);

    assertFalse(isExceptionWithState);
  }

  @Test
  void shouldReturnFalseWhenPsqlStateClassIsNull() {
    PgException exception = createPgExceptionFromMap(getErrorMapWithPsqlStateNull());
    boolean isExceptionWithState = exceptionWithSQLStateClass(exception, PSQLState.INVALID_COLUMN_REFERENCE);

    assertFalse(isExceptionWithState);
  }

  @Test
  void shouldReturnTrueWhenPsqlStateEqualsToState() {
    PgException exception = createPgExceptionFromMap(getPrimaryKeyErrorMap());
    boolean isExceptionWithState = exceptionWithSQLState(exception, PSQLState.UNIQUE_VIOLATION);

    assertTrue(isExceptionWithState);
  }

  @Test
  void shouldReturnFalseWhenPsqlStateDoesNotEqualToState() {
    PgException exception = createPgExceptionFromMap(getPrimaryKeyErrorMap());
    boolean isExceptionWithState = exceptionWithSQLState(exception, PSQLState.DATABASE_DROPPED);

    assertFalse(isExceptionWithState);
  }

  @Test
  void shouldReturnFalseWhenPsqlStateIsNull() {
    PgException exception = createPgExceptionFromMap(getErrorMapWithPsqlStateNull());
    boolean isExceptionWithState = exceptionWithSQLState(exception, PSQLState.DUPLICATE_FILE);

    assertFalse(isExceptionWithState);
  }
}
