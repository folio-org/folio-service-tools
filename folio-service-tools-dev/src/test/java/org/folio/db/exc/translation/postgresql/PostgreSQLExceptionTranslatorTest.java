package org.folio.db.exc.translation.postgresql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import static org.folio.db.ErrorConstants.DATATYPE_MISMATCH_ERROR_CODE;
import static org.folio.db.ErrorConstants.ERROR_TYPE;
import static org.folio.db.ErrorConstants.FOREIGN_KEY_VIOLATION_ERROR_CODE;
import static org.folio.db.ErrorConstants.INVALID_PASSWORD_ERROR_CODE;
import static org.folio.db.ErrorConstants.INVALID_TEXT_REPRESENTATION_ERROR_CODE;
import static org.folio.db.ErrorConstants.NOT_NULL_VIOLATION_ERROR_CODE;
import static org.folio.db.ErrorFactory.getDataTypeMismatchViolation;
import static org.folio.db.ErrorFactory.getForeignKeyErrorMap;
import static org.folio.db.ErrorFactory.getInvalidPasswordErrorMap;
import static org.folio.db.ErrorFactory.getUUIDErrorMap;
import static org.folio.rest.persist.PgExceptionUtil.createPgExceptionFromMap;

import java.util.function.Function;

import io.vertx.core.Future;
import io.vertx.pgclient.PgException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;

import org.folio.db.exc.AuthorizationException;
import org.folio.db.exc.ConstraintViolationException;
import org.folio.db.exc.DatabaseException;
import org.folio.db.exc.InvalidUUIDException;
import org.folio.test.junit.TestStartLoggingRule;

public class PostgreSQLExceptionTranslatorTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private PostgreSQLExceptionTranslator translator;

  @Before
  public void setup() {
    translator = new PostgreSQLExceptionTranslator();
  }

  @Test
  public void shouldReturnTrueWhenExceptionIsAcceptable() {
    PgException exception = new PgException("test error", ERROR_TYPE, NOT_NULL_VIOLATION_ERROR_CODE, "test detail");

    boolean acceptable = translator.acceptable(exception);
    assertTrue(acceptable);
  }

  @Test
  public void shouldReturnFalseWhenExceptionIsNotAcceptable() {
    boolean acceptable = translator.acceptable(new IllegalArgumentException());
    assertFalse(acceptable);
  }

  @Test
  public void shouldReturnFalseWhenExceptionIsNull() {
    boolean acceptable = translator.acceptable(null);
    assertFalse(acceptable);
  }

  @Test
  public void shouldReturnDatabaseExceptionWhenExceptionIsConstraintViolationException() {
    PgException exception = createPgExceptionFromMap((getForeignKeyErrorMap()));
    DatabaseException databaseException = translator.doTranslation(exception);

    assertThat(databaseException.getSqlState(), equalTo(FOREIGN_KEY_VIOLATION_ERROR_CODE));
    assertTrue(databaseException instanceof ConstraintViolationException);

  }

  @Test
  public void shouldReturnDatabaseExceptionWhenExceptionIsInvalidUUIDException() {
    PgException exception = createPgExceptionFromMap((getUUIDErrorMap()));
    DatabaseException databaseException = translator.doTranslation(exception);

    assertThat(databaseException.getSqlState(), equalTo(INVALID_TEXT_REPRESENTATION_ERROR_CODE));
    assertTrue(databaseException instanceof InvalidUUIDException);

  }

  @Test
  public void shouldReturnDatabaseExceptionWhenExceptionIsPgException() {
    PgException exception = createPgExceptionFromMap((getDataTypeMismatchViolation()));
    DatabaseException databaseException = translator.doTranslation(exception);

    assertThat(databaseException.getSqlState(), equalTo(DATATYPE_MISMATCH_ERROR_CODE));
    assertThat(databaseException, instanceOf(DatabaseException.class));
  }

  @Test
  public void shouldReturnDatabaseExceptionWhenExceptionIsAuthorizationException() {
    PgException exception = createPgExceptionFromMap((getInvalidPasswordErrorMap()));
    DatabaseException databaseException = translator.doTranslation(exception);

    assertThat(databaseException.getSqlState(), equalTo(INVALID_PASSWORD_ERROR_CODE));
    assertTrue(databaseException instanceof AuthorizationException);
  }

  @Test
  public void shouldReturnDatabaseExceptionWhenExceptionIsIllegalArgumentException() {
    DatabaseException databaseException = translator.doTranslation(new IllegalArgumentException());

    assertNull(databaseException.getSqlState());
    assertTrue(databaseException.getCause() instanceof IllegalArgumentException);

  }

  @Test
  public void shouldReturnDatabaseExceptionWhenExceptionIsNull() {
    DatabaseException databaseException = translator.doTranslation(null);
    assertNull(databaseException.getSqlState());
  }

  @Test
  public void shouldReturnDatabaseExceptionWhenThrowableIsNotAcceptable() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(containsString("Exception is not acceptable and cannot be translated"));

    DatabaseException databaseException = translator.translate(new IndexOutOfBoundsException());
    assertNull(databaseException.getSqlState());
  }

  @Test
  public void shouldReturnDatabaseExceptionWhenThrowableIsNull() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(containsString("Exception is not acceptable and cannot be translated"));

    DatabaseException databaseException = translator.translate(null);
    assertNull(databaseException.getSqlState());
  }

  @Test
  public void shouldFailFutureWithIndexOutOfBoundsException() {
    Function<Throwable, Future<Object>> throwableFutureFunction = translator.translateOrPassBy();
    Future<Object> apply = throwableFutureFunction.apply(new IndexOutOfBoundsException());

    assertTrue(apply.cause() instanceof IndexOutOfBoundsException);
  }

  @Test
  public void shouldReturnDatabaseExceptionWhenThrowableIsNullo() {
    PgException exception = createPgExceptionFromMap((getUUIDErrorMap()));

    Function<Throwable, Future<Object>> throwableFutureFunction = translator.translateOrPassBy();
    Future<Object> apply = throwableFutureFunction.apply(exception);

    assertTrue(apply.cause() instanceof InvalidUUIDException);
  }

}
