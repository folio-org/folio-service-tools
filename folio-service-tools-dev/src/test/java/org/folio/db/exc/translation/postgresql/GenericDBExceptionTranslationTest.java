package org.folio.db.exc.translation.postgresql;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.folio.db.ErrorConstants.INVALID_TEXT_REPRESENTATION_ERROR_CODE;
import static org.folio.db.ErrorFactory.getDataLengthMismatch;
import static org.folio.db.ErrorFactory.getErrorMapWithMessage;
import static org.folio.db.ErrorFactory.getErrorMapWithSqlStateNull;
import static org.folio.db.ErrorFactory.getErrorMapWithSqlStateOnly;
import static org.folio.db.ErrorFactory.getUniqueViolationErrorMap;

import com.github.jasync.sql.db.postgresql.exceptions.GenericDatabaseException;
import com.github.jasync.sql.db.postgresql.messages.backend.ErrorMessage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import org.folio.common.pf.PartialFunction;
import org.folio.db.exc.DatabaseException;
import org.folio.test.junit.TestStartLoggingRule;

public class GenericDBExceptionTranslationTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();

  @Test
  public void shouldReturnDatabaseExceptionWithUniqueViolationCode(){
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getUniqueViolationErrorMap()));
    final PartialFunction<GenericDatabaseException, DatabaseException> asPartial = GenericDBExceptionTranslation.asPartial();
    final DatabaseException apply = asPartial.apply(exception);

    assertThat(apply.getSqlState(), equalTo(PSQLState.UNIQUE_VIOLATION.getCode()));
  }

  @Test
  public void shouldReturnTrueWhenExceptionIsGenericException(){
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getDataLengthMismatch()));
    final boolean isGenericException = new GenericDBExceptionTranslation.TPredicate().test(exception);
    assertTrue(isGenericException);
  }

  @Test
  public void shouldReturnTrueWhenExceptionIsNotGenericException(){
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getErrorMapWithSqlStateNull()));
    final boolean isGenericException = new GenericDBExceptionTranslation.TPredicate().test(exception);
    assertTrue(isGenericException);
  }

  @Test
  public void shouldReturnTrueWhenExceptionIsNull(){
    final boolean isGenericException = new GenericDBExceptionTranslation.TPredicate().test(null);
    assertTrue(isGenericException);
  }

  @Test
  public void shouldReturnDatabaseExceptionWithFieldsWhenExceptionIsGenericDatabase(){
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getDataLengthMismatch()));
    final DatabaseException databaseException = new GenericDBExceptionTranslation.TFunction().apply(exception);

    assertThat(databaseException.getSqlState(), equalTo(PSQLState.STRING_DATA_RIGHT_TRUNCATION.getCode()));
    assertThat(databaseException.getMessage(), containsString("value too long"));
  }

  @Test
  public void shouldReturnDatabaseExceptionWithSqlStateOnlyWhenExceptionIsGenericDatabase(){
    GenericDatabaseException exception = new GenericDatabaseException(
      new ErrorMessage(getErrorMapWithSqlStateOnly(INVALID_TEXT_REPRESENTATION_ERROR_CODE)));
    final DatabaseException databaseException = new GenericDBExceptionTranslation.TFunction().apply(exception);

    assertThat(databaseException.getSqlState(), equalTo(PSQLState.INVALID_TEXT_REPRESENTATION.getCode()));
    assertNull(databaseException.getMessage());
  }

  @Test
  public void shouldReturnDatabaseExceptionWithMessageOnlyWhenExceptionIsGenericDatabase(){
    final String errorMessage = "test error message";
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getErrorMapWithMessage(errorMessage)));
    final DatabaseException databaseException = new GenericDBExceptionTranslation.TFunction().apply(exception);

    assertThat(databaseException.getMessage(), equalTo(errorMessage));
    assertNull(databaseException.getSqlState());
  }

}
