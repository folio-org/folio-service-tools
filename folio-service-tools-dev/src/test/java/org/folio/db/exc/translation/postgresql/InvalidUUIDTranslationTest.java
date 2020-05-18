package org.folio.db.exc.translation.postgresql;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.folio.db.ErrorFactory.getErrorMapWithSqlStateNull;
import static org.folio.db.ErrorFactory.getIntegrityViolationErrorMap;
import static org.folio.db.ErrorFactory.getUUIDErrorMap;
import static org.folio.rest.persist.PgExceptionUtil.createPgExceptionFromMap;

import io.vertx.pgclient.PgException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import org.folio.common.pf.PartialFunction;
import org.folio.db.exc.DatabaseException;
import org.folio.db.exc.InvalidUUIDException;
import org.folio.test.junit.TestStartLoggingRule;

public class InvalidUUIDTranslationTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();

  @Test
  public void shouldReturnDatabaseExceptionWithUUUIDViolationCode() {
    PgException exception = createPgExceptionFromMap((getUUIDErrorMap()));
    PartialFunction<PgException, DatabaseException> partial = InvalidUUIDTranslation.asPartial();
    DatabaseException databaseException = partial.apply(exception);

    assertThat(databaseException.getSqlState(), equalTo(PSQLState.INVALID_TEXT_REPRESENTATION.getCode()));
  }

  @Test
  public void shouldReturnTrueWhenExceptionIsUUIDViolation() {
    PgException exception = createPgExceptionFromMap((getUUIDErrorMap()));
    InvalidUUIDTranslation.TPredicate predicate = new InvalidUUIDTranslation.TPredicate();
    boolean isUUIDException = predicate.test(exception);

    assertTrue(isUUIDException);
  }

  @Test
  public void shouldReturnFalseWhenExceptionIsNotUUIDViolation() {
    PgException exception = createPgExceptionFromMap((getIntegrityViolationErrorMap()));
    InvalidUUIDTranslation.TPredicate predicate = new InvalidUUIDTranslation.TPredicate();
    boolean isUUIDException = predicate.test(exception);

    assertFalse(isUUIDException);
  }

  @Test
  public void shouldReturnFalseWhenExceptionIsNull() {
    PgException exception = createPgExceptionFromMap((getErrorMapWithSqlStateNull()));
    InvalidUUIDTranslation.TPredicate predicate = new InvalidUUIDTranslation.TPredicate();
    boolean isUUIDException = predicate.test(exception);

    assertFalse(isUUIDException);
  }

  @Test
  public void shouldReturnUUIDExceptionWithNoFieldsWhenExceptionIsNull() {
    PgException exception = createPgExceptionFromMap((getUUIDErrorMap()));
    InvalidUUIDTranslation.TFunction function = new InvalidUUIDTranslation.TFunction();
    InvalidUUIDException apply = function.apply(exception);

    assertThat(apply.getSqlState(), equalTo(PSQLState.INVALID_TEXT_REPRESENTATION.getCode()));
    assertThat(apply.getInvalidValue(), equalTo("INVALID"));

  }
}
