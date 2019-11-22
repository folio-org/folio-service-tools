package org.folio.db.exc.translation.postgresql;

import static org.folio.db.ErrorFactory.getErrorMapWithPsqlStateNull;
import static org.folio.db.ErrorFactory.getPrimaryKeyErrorMap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.folio.test.junit.TestStartLoggingRule;
import org.junit.Rule;
import org.junit.Test;

import com.github.jasync.sql.db.postgresql.exceptions.GenericDatabaseException;
import com.github.jasync.sql.db.postgresql.messages.backend.ErrorMessage;
import org.junit.rules.TestRule;

public class TranslationUtilsTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();

  @Test
  public void shouldReturnTrueWhenPsqlStateBelongsToStateClass(){
    final boolean isExceptionWithState = TranslationUtils.exceptionWithSQLStateClass(new GenericDatabaseException(
      new ErrorMessage(getPrimaryKeyErrorMap())), PSQLState.UNIQUE_VIOLATION);

    assertTrue(isExceptionWithState);
  }

  @Test
  public void shouldReturnFalseWhenPsqlStateDoesNotBelongToStateClass(){
    final boolean isExceptionWithState = TranslationUtils.exceptionWithSQLStateClass(new GenericDatabaseException(
      new ErrorMessage(getPrimaryKeyErrorMap())), PSQLState.DUPLICATE_COLUMN);

    assertFalse(isExceptionWithState);
  }

  @Test
  public void shouldReturnFalseWhenPsqlStateClassIsNull(){
    final boolean isExceptionWithState = TranslationUtils.exceptionWithSQLStateClass(new GenericDatabaseException(
      new ErrorMessage(getErrorMapWithPsqlStateNull())), PSQLState.INVALID_COLUMN_REFERENCE);

    assertFalse(isExceptionWithState);
  }

  @Test
  public void shouldReturnTrueWhenPsqlStateEqualsToState(){
    final boolean isExceptionWithState = TranslationUtils.exceptionWithSQLState(new GenericDatabaseException(
      new ErrorMessage(getPrimaryKeyErrorMap())), PSQLState.UNIQUE_VIOLATION);

    assertTrue(isExceptionWithState);
  }

  @Test
  public void shouldReturnFalseWhenPsqlStateDoesNotEqualToState(){
    final boolean isExceptionWithState = TranslationUtils.exceptionWithSQLState(new GenericDatabaseException(
      new ErrorMessage(getPrimaryKeyErrorMap())), PSQLState.DATABASE_DROPPED);

    assertFalse(isExceptionWithState);
  }

  @Test
  public void shouldReturnFalseWhenPsqlStateIsNull(){
    final boolean isExceptionWithState = TranslationUtils.exceptionWithSQLState(new GenericDatabaseException(
      new ErrorMessage(getErrorMapWithPsqlStateNull())), PSQLState.DUPLICATE_FILE);

    assertFalse(isExceptionWithState);
  }
}
