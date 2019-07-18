package org.folio.db.exc.translation.postgresql;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.folio.db.ErrorFactory.getErrorMapWithSqlStateNull;
import static org.folio.db.ErrorFactory.getIntegrityViolationErrorMap;
import static org.folio.db.ErrorFactory.getUUIDErrorMap;

import com.github.mauricio.async.db.postgresql.exceptions.GenericDatabaseException;
import com.github.mauricio.async.db.postgresql.messages.backend.ErrorMessage;
import org.junit.Test;

import org.folio.common.pf.PartialFunction;
import org.folio.db.exc.DatabaseException;
import org.folio.db.exc.InvalidUUIDException;

public class InvalidUUIDTranslationTest {

  @Test
  public void shouldReturnDatabaseExceptionWithUUUIDViolationCode() {

    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getUUIDErrorMap()));
    final PartialFunction<GenericDatabaseException, DatabaseException> partial = InvalidUUIDTranslation.asPartial();
    final DatabaseException apply = partial.apply(exception);

    assertThat(apply.getSqlState(), equalTo(PSQLState.INVALID_TEXT_REPRESENTATION.getCode()));
  }

  @Test
  public void shouldReturnTrueWhenExceptionIsUUIDViolation() {
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getUUIDErrorMap()));
    final InvalidUUIDTranslation.TPredicate predicate = new InvalidUUIDTranslation.TPredicate();
    final boolean isUUIDException = predicate.test(exception);

    assertTrue(isUUIDException);
  }

  @Test
  public void shouldReturnFalseWhenExceptionIsNotUUIDViolation() {
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getIntegrityViolationErrorMap()));
    final InvalidUUIDTranslation.TPredicate predicate = new InvalidUUIDTranslation.TPredicate();
    final boolean isUUIDException = predicate.test(exception);

    assertFalse(isUUIDException);
  }

  @Test
  public void shouldReturnFalseWhenExceptionIsNull() {
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getErrorMapWithSqlStateNull()));
    final InvalidUUIDTranslation.TPredicate predicate = new InvalidUUIDTranslation.TPredicate();
    final boolean isUUIDException = predicate.test(exception);

    assertFalse(isUUIDException);
  }

  @Test
  public void shouldReturnUUIDExceptionWithNoFieldsWhenExceptionIsNull(){
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getUUIDErrorMap()));
    final InvalidUUIDTranslation.TFunction function = new InvalidUUIDTranslation.TFunction();
    final InvalidUUIDException apply = function.apply(exception);

    assertThat(apply.getSqlState(), equalTo(PSQLState.INVALID_TEXT_REPRESENTATION.getCode()));
    assertThat(apply.getInvalidValue(), equalTo("INVALID"));

  }
}
