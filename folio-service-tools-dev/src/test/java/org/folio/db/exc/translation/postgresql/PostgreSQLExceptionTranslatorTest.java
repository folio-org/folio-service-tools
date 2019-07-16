package org.folio.db.exc.translation.postgresql;

import com.github.mauricio.async.db.postgresql.exceptions.GenericDatabaseException;
import com.github.mauricio.async.db.postgresql.messages.backend.ErrorMessage;
import org.folio.db.exc.ConstraintViolationException;
import org.folio.db.exc.DatabaseException;
import org.folio.db.exc.InvalidUUIDException;
import org.folio.test.junit.TestStartLoggingRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import static org.folio.db.ErrorConstants.DATATYPE_MISMATCH_ERROR_CODE;
import static org.folio.db.ErrorConstants.FOREIGN_KEY_VIOLATION_ERROR_CODE;
import static org.folio.db.ErrorConstants.INVALID_TEXT_REPRESENTATION_ERROR_CODE;
import static org.folio.db.ErrorFactory.get;
import static org.folio.db.ErrorFactory.getForeingKeyErrorMap;
import static org.folio.db.ErrorFactory.getUUIDErrorMap;
import static org.hamcrest.Matchers.equalTo;

public class PostgreSQLExceptionTranslatorTest {

  private PostgreSQLExceptionTranslator translator;

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();

  @Before
  public void setup(){
    translator = new PostgreSQLExceptionTranslator();
  }

  @Test
  public void shouldReturnTrueWhenExceptionIsAcceptable () {
    com.github.mauricio.async.db.exceptions.DatabaseException exception =
      new com.github.mauricio.async.db.exceptions.DatabaseException("test error", new java.lang.RuntimeException());

    final boolean acceptable = translator.acceptable(exception);
    org.junit.Assert.assertTrue(acceptable);
  }

  @Test
  public void shouldReturnFalseWhenExceptionIsNotAcceptable () {
    final boolean acceptable = translator.acceptable(new IllegalArgumentException());
    org.junit.Assert.assertFalse(acceptable);
  }

  @Test
  public void shouldReturnFalseWhenExceptionIsNull() {
    final boolean acceptable = translator.acceptable(null);
    org.junit.Assert.assertFalse(acceptable);
  }

  @Test
  public void shouldReturnDatabaseExceptionWhenExceptionIsConstraintViolationException() {
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getForeingKeyErrorMap()));
    final DatabaseException databaseException = translator.doTranslation(exception);

    org.junit.Assert.assertThat(databaseException.getSqlState(), equalTo(FOREIGN_KEY_VIOLATION_ERROR_CODE));
    org.junit.Assert.assertTrue(databaseException instanceof ConstraintViolationException);

  }

  @Test
  public void shouldReturnDatabaseExceptionWhenExceptionIsInvalidUUIDException() {
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getUUIDErrorMap()));
    final DatabaseException databaseException = translator.doTranslation(exception);

    org.junit.Assert.assertThat(databaseException.getSqlState(), equalTo(INVALID_TEXT_REPRESENTATION_ERROR_CODE));
    org.junit.Assert.assertTrue(databaseException instanceof InvalidUUIDException);

  }

  @Test
  public void shouldReturnDatabaseExceptionWhenExceptionIsGenericDatabaseException() {
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(get()));
    final DatabaseException databaseException = translator.doTranslation(exception);

    org.junit.Assert.assertThat(databaseException.getSqlState(), equalTo(DATATYPE_MISMATCH_ERROR_CODE));
    org.junit.Assert.assertTrue(databaseException instanceof DatabaseException);

  }

  @Test
  public void shouldReturnDatabaseExceptionWhenExceptionIsIllegalArgumentException() {
    final DatabaseException databaseException = translator.doTranslation(new IllegalArgumentException());

    org.junit.Assert.assertNull(databaseException.getSqlState());
    org.junit.Assert.assertTrue(databaseException.getCause() instanceof IllegalArgumentException);

  }

  @Test
  public void shouldReturnDatabaseExceptionWhenExceptionIsNull() {
    final DatabaseException databaseException = translator.doTranslation(null);
    org.junit.Assert.assertNull(databaseException.getSqlState());
  }

  @Test
  public void shouldReturnDatabaseExceptionWhenThrowableIsDatabaseException() {

    com.github.mauricio.async.db.exceptions.DatabaseException exception =
      new com.github.mauricio.async.db.exceptions.DatabaseException("test error", new java.lang.RuntimeException());

    final DatabaseException databaseException = translator.translate(exception);
    org.junit.Assert.assertNull(databaseException.getSqlState());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldReturnDatabaseExceptionWhenThrowableIsNotAcceptable() {

    final DatabaseException databaseException = translator.translate(new IndexOutOfBoundsException());
    org.junit.Assert.assertNull(databaseException.getSqlState());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldReturnDatabaseExceptionWhenThrowableIsNull() {

    final DatabaseException databaseException = translator.translate(null);
    org.junit.Assert.assertNull(databaseException.getSqlState());
  }
}
