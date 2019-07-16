package org.folio.db.exc.translation.postgresql;

import static org.folio.db.ErrorFactory.getErrorMapWithPsqlStateNull;
import static org.folio.db.ErrorFactory.getPrimaryKeyErrorMap;

import org.folio.test.junit.TestStartLoggingRule;
import org.junit.Rule;
import org.junit.Test;

import com.github.mauricio.async.db.postgresql.exceptions.GenericDatabaseException;
import com.github.mauricio.async.db.postgresql.messages.backend.ErrorMessage;
import org.junit.rules.TestRule;

public class TranslationUtilsTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();

  @Test
  public void shouldReturnTrueWhenPsqlStateBelongsToStateClass(){
    TranslationUtils.exceptionWithSQLStateClass(new GenericDatabaseException(
      new ErrorMessage(getPrimaryKeyErrorMap())), PSQLState.UNIQUE_VIOLATION);
  }

  @Test
  public void shouldReturnFalseWhenPsqlStateDoesNotBelongToStateClass(){
    TranslationUtils.exceptionWithSQLStateClass(new GenericDatabaseException(
      new ErrorMessage(getPrimaryKeyErrorMap())), PSQLState.DUPLICATE_COLUMN);
  }

  @Test
  public void shouldReturnFalseWhenPsqlStateClassIsNull(){
    TranslationUtils.exceptionWithSQLStateClass(new GenericDatabaseException(
      new ErrorMessage(getErrorMapWithPsqlStateNull())), PSQLState.INVALID_COLUMN_REFERENCE);
  }

  @Test
  public void shouldReturnTrueWhenPsqlStateEqualsToState(){
    TranslationUtils.exceptionWithSQLState(new GenericDatabaseException(
      new ErrorMessage(getPrimaryKeyErrorMap())), PSQLState.UNIQUE_VIOLATION);
  }

  @Test
  public void shouldReturnFalseWhenPsqlStateDoesNotEqualToState(){
    TranslationUtils.exceptionWithSQLState(new GenericDatabaseException(
      new ErrorMessage(getPrimaryKeyErrorMap())), PSQLState.DATABASE_DROPPED);
  }

  @Test
  public void shouldReturnFalseWhenPsqlStateIsNull(){
    TranslationUtils.exceptionWithSQLState(new GenericDatabaseException(
      new ErrorMessage(getErrorMapWithPsqlStateNull())), PSQLState.DUPLICATE_FILE);
  }
}
