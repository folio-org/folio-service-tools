package org.folio.db.exc.translation.postgresql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.folio.db.ErrorFactory.getErrorMapWithSqlStateNull;
import static org.folio.db.ErrorFactory.getIntegrityViolationErrorMap;
import static org.folio.db.ErrorFactory.getUUIDErrorMap;
import static org.folio.rest.persist.PgExceptionUtil.createPgExceptionFromMap;

import io.vertx.pgclient.PgException;
import org.folio.test.extensions.TestStartLoggingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.folio.common.pf.PartialFunction;
import org.folio.db.exc.DatabaseException;
import org.folio.db.exc.InvalidUUIDException;

class InvalidUUIDTranslationTest {

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();

  @Test
  void shouldReturnDatabaseExceptionWithUUUIDViolationCode() {
    PgException exception = createPgExceptionFromMap((getUUIDErrorMap()));
    PartialFunction<PgException, DatabaseException> partial = InvalidUUIDTranslation.asPartial();
    DatabaseException databaseException = partial.apply(exception);

    assertThat(databaseException.getSqlState(), equalTo(PSQLState.INVALID_TEXT_REPRESENTATION.getCode()));
  }

  @Test
  void shouldReturnTrueWhenExceptionIsUUIDViolation() {
    PgException exception = createPgExceptionFromMap((getUUIDErrorMap()));
    InvalidUUIDTranslation.TPredicate predicate = new InvalidUUIDTranslation.TPredicate();
    boolean isUUIDException = predicate.test(exception);

    assertTrue(isUUIDException);
  }

  @Test
  void shouldReturnFalseWhenExceptionIsNotUUIDViolation() {
    PgException exception = createPgExceptionFromMap((getIntegrityViolationErrorMap()));
    InvalidUUIDTranslation.TPredicate predicate = new InvalidUUIDTranslation.TPredicate();
    boolean isUUIDException = predicate.test(exception);

    assertFalse(isUUIDException);
  }

  @Test
  void shouldReturnFalseWhenExceptionIsNull() {
    PgException exception = createPgExceptionFromMap((getErrorMapWithSqlStateNull()));
    InvalidUUIDTranslation.TPredicate predicate = new InvalidUUIDTranslation.TPredicate();
    boolean isUUIDException = predicate.test(exception);

    assertFalse(isUUIDException);
  }

  @Test
  void shouldReturnUUIDExceptionWithNoFieldsWhenExceptionIsNull() {
    PgException exception = createPgExceptionFromMap((getUUIDErrorMap()));
    InvalidUUIDTranslation.TFunction function = new InvalidUUIDTranslation.TFunction();
    InvalidUUIDException apply = function.apply(exception);

    assertThat(apply.getSqlState(), equalTo(PSQLState.INVALID_TEXT_REPRESENTATION.getCode()));
    assertThat(apply.getInvalidValue(), equalTo("INVALID"));

  }
}
