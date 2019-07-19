package org.folio.db.exc.translation.postgresql;

import static org.folio.db.ErrorConstants.DATATYPE_MISMATCH_ERROR_CODE;
import static org.folio.db.ErrorConstants.FOREIGN_KEY_VIOLATION_ERROR_CODE;
import static org.folio.db.ErrorConstants.INVALID_TEXT_REPRESENTATION_ERROR_CODE;
import static org.folio.db.ErrorFactory.getDataTypeMismatchViolation;
import static org.folio.db.ErrorFactory.getForeignKeyErrorMap;
import static org.folio.db.ErrorFactory.getUUIDErrorMap;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.vertx.core.Future;
import java.util.function.Function;
import org.folio.db.exc.ConstraintViolationException;
import org.folio.db.exc.DatabaseException;
import org.folio.db.exc.InvalidUUIDException;
import org.folio.test.junit.TestStartLoggingRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;

import com.github.mauricio.async.db.postgresql.exceptions.GenericDatabaseException;
import com.github.mauricio.async.db.postgresql.messages.backend.ErrorMessage;

public class PostgreSQLExceptionTranslatorTest {

  private PostgreSQLExceptionTranslator translator;

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setup(){
    translator = new PostgreSQLExceptionTranslator();
  }

  @Test
  public void shouldReturnTrueWhenExceptionIsAcceptable () {
    com.github.mauricio.async.db.exceptions.DatabaseException exception =
      new com.github.mauricio.async.db.exceptions.DatabaseException("test error", new java.lang.RuntimeException());

    final boolean acceptable = translator.acceptable(exception);
    assertTrue(acceptable);
  }

  @Test
  public void shouldReturnFalseWhenExceptionIsNotAcceptable () {
    final boolean acceptable = translator.acceptable(new IllegalArgumentException());
    assertFalse(acceptable);
  }

  @Test
  public void shouldReturnFalseWhenExceptionIsNull() {
    final boolean acceptable = translator.acceptable(null);
    assertFalse(acceptable);
  }

  @Test
  public void shouldReturnDatabaseExceptionWhenExceptionIsConstraintViolationException() {
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getForeignKeyErrorMap()));
    final DatabaseException databaseException = translator.doTranslation(exception);

    assertThat(databaseException.getSqlState(), equalTo(FOREIGN_KEY_VIOLATION_ERROR_CODE));
    assertTrue(databaseException instanceof ConstraintViolationException);

  }

  @Test
  public void shouldReturnDatabaseExceptionWhenExceptionIsInvalidUUIDException() {
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getUUIDErrorMap()));
    final DatabaseException databaseException = translator.doTranslation(exception);

    assertThat(databaseException.getSqlState(), equalTo(INVALID_TEXT_REPRESENTATION_ERROR_CODE));
    assertTrue(databaseException instanceof InvalidUUIDException);

  }

  @Test
  public void shouldReturnDatabaseExceptionWhenExceptionIsGenericDatabaseException() {
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getDataTypeMismatchViolation()));
    final DatabaseException databaseException = translator.doTranslation(exception);

    assertThat(databaseException.getSqlState(), equalTo(DATATYPE_MISMATCH_ERROR_CODE));
    assertTrue(databaseException instanceof DatabaseException);

  }

  @Test
  public void shouldReturnDatabaseExceptionWhenExceptionIsIllegalArgumentException() {
    final DatabaseException databaseException = translator.doTranslation(new IllegalArgumentException());

    assertNull(databaseException.getSqlState());
    assertTrue(databaseException.getCause() instanceof IllegalArgumentException);

  }

  @Test
  public void shouldReturnDatabaseExceptionWhenExceptionIsNull() {
    final DatabaseException databaseException = translator.doTranslation(null);
    assertNull(databaseException.getSqlState());
  }

  @Test
  public void shouldReturnDatabaseExceptionWhenThrowableIsDatabaseException() {

    com.github.mauricio.async.db.exceptions.DatabaseException exception =
      new com.github.mauricio.async.db.exceptions.DatabaseException("test error", new java.lang.RuntimeException());

    final DatabaseException databaseException = translator.translate(exception);
    assertNull(databaseException.getSqlState());
  }

  @Test
  public void shouldReturnDatabaseExceptionWhenThrowableIsNotAcceptable() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(containsString("Exception is not acceptable and cannot be translated"));

    final DatabaseException databaseException = translator.translate(new IndexOutOfBoundsException());
    assertNull(databaseException.getSqlState());
  }

  @Test
  public void shouldReturnDatabaseExceptionWhenThrowableIsNull() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(containsString("Exception is not acceptable and cannot be translated"));

    final DatabaseException databaseException = translator.translate(null);
    assertNull(databaseException.getSqlState());
  }

  @Test
  public void shouldFailFutureWithIndexOutOfBoundsException() {

    final Function<Throwable, Future<Object>> throwableFutureFunction = translator.translateOrPassBy();
    final Future<Object> apply = throwableFutureFunction.apply(new IndexOutOfBoundsException());

    assertTrue(apply.cause() instanceof IndexOutOfBoundsException);
  }

  @Test
  public void shouldReturnDatabaseExceptionWhenThrowableIsNullo() {

    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getUUIDErrorMap()));

    final Function<Throwable, Future<Object>> throwableFutureFunction = translator.translateOrPassBy();
    final Future<Object> apply = throwableFutureFunction.apply(exception);

    assertTrue(apply.cause() instanceof InvalidUUIDException);
  }

}
