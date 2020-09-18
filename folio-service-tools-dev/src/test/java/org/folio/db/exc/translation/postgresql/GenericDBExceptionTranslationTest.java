package org.folio.db.exc.translation.postgresql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import static org.folio.db.ErrorConstants.INVALID_TEXT_REPRESENTATION_ERROR_CODE;
import static org.folio.db.ErrorFactory.getDataLengthMismatch;
import static org.folio.db.ErrorFactory.getErrorMapWithMessage;
import static org.folio.db.ErrorFactory.getErrorMapWithSqlStateNull;
import static org.folio.db.ErrorFactory.getErrorMapWithSqlStateOnly;
import static org.folio.db.ErrorFactory.getUniqueViolationErrorMap;
import static org.folio.rest.persist.PgExceptionUtil.createPgExceptionFromMap;

import io.vertx.pgclient.PgException;
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
  public void shouldReturnDatabaseExceptionWithUniqueViolationCode() {
    PgException exception = createPgExceptionFromMap((getUniqueViolationErrorMap()));
    PartialFunction<PgException, DatabaseException> asPartial = GenericDBExceptionTranslation.asPartial();
    DatabaseException databaseException = asPartial.apply(exception);

    assertThat(databaseException.getSqlState(), equalTo(PSQLState.UNIQUE_VIOLATION.getCode()));
  }

  @Test
  public void shouldReturnTrueWhenExceptionIsGenericException() {
    PgException exception = createPgExceptionFromMap((getDataLengthMismatch()));
    boolean isGenericException = new GenericDBExceptionTranslation.TPredicate().test(exception);
    assertTrue(isGenericException);
  }

  @Test
  public void shouldReturnTrueWhenExceptionIsNotGenericException() {
    PgException exception = createPgExceptionFromMap((getErrorMapWithSqlStateNull()));
    boolean isGenericException = new GenericDBExceptionTranslation.TPredicate().test(exception);
    assertTrue(isGenericException);
  }

  @Test
  public void shouldReturnTrueWhenExceptionIsNull() {
    boolean isGenericException = new GenericDBExceptionTranslation.TPredicate().test(null);
    assertTrue(isGenericException);
  }

  @Test
  public void shouldReturnDatabaseExceptionWithFieldsWhenExceptionIsGenericDatabase() {
    PgException exception = createPgExceptionFromMap((getDataLengthMismatch()));
    DatabaseException databaseException = new GenericDBExceptionTranslation.TFunction().apply(exception);

    assertThat(databaseException.getSqlState(), equalTo(PSQLState.STRING_DATA_RIGHT_TRUNCATION.getCode()));
    assertThat(databaseException.getMessage(), containsString("value too long"));
  }

  @Test
  public void shouldReturnDatabaseExceptionWithSqlStateOnlyWhenExceptionIsGenericDatabase() {
    PgException exception = createPgExceptionFromMap(getErrorMapWithSqlStateOnly(INVALID_TEXT_REPRESENTATION_ERROR_CODE));
    DatabaseException databaseException = new GenericDBExceptionTranslation.TFunction().apply(exception);

    assertThat(databaseException.getSqlState(), equalTo(PSQLState.INVALID_TEXT_REPRESENTATION.getCode()));
    assertNull(databaseException.getMessage());
  }

  @Test
  public void shouldReturnDatabaseExceptionWithMessageOnlyWhenExceptionIsGenericDatabase() {
    String errorMessage = "test error message";
    PgException exception = createPgExceptionFromMap((getErrorMapWithMessage(errorMessage)));
    DatabaseException databaseException = new GenericDBExceptionTranslation.TFunction().apply(exception);

    assertThat(databaseException.getMessage(), equalTo(errorMessage));
    assertNull(databaseException.getSqlState());
  }

}
