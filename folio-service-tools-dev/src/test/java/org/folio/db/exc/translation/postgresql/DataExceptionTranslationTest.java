package org.folio.db.exc.translation.postgresql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

import static org.folio.db.ErrorFactory.getCheckViolationErrorMap;
import static org.folio.db.ErrorFactory.getDataLengthMismatch;
import static org.folio.db.ErrorFactory.getErrorMapWithSqlStateNull;
import static org.folio.rest.persist.PgExceptionUtil.createPgExceptionFromMap;

import io.vertx.pgclient.PgException;
import org.folio.test.extensions.TestStartLoggingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.folio.common.pf.PartialFunction;
import org.folio.db.exc.DataException;
import org.folio.db.exc.DatabaseException;

class DataExceptionTranslationTest {

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();

  @Test
  void shouldReturnDatabaseExceptionWithDataExceptionCode() {
    PgException exception = createPgExceptionFromMap((getDataLengthMismatch()));
    final PartialFunction<PgException, DatabaseException> asPartial = DataExceptionTranslation.asPartial();
    final DatabaseException apply = asPartial.apply(exception);

    assertThat(apply.getSqlState(), equalTo(PSQLState.STRING_DATA_RIGHT_TRUNCATION.getCode()));
  }

  @Test
  void shouldReturnTrueWhenExceptionIsDataException() {
    PgException exception = createPgExceptionFromMap((getDataLengthMismatch()));
    final boolean isDataException = new DataExceptionTranslation.TPredicate().test(exception);

    assertTrue(isDataException);
  }

  @Test
  void shouldReturnFalseWhenExceptionIsNotDataException() {
    PgException exception = createPgExceptionFromMap((getCheckViolationErrorMap()));
    final boolean isDataException = new DataExceptionTranslation.TPredicate().test(exception);

    assertFalse(isDataException);
  }

  @Test
  void shouldReturnFalseWhenExceptionIsNull() {
    PgException exception = createPgExceptionFromMap((getErrorMapWithSqlStateNull()));
    final boolean isDataException = new DataExceptionTranslation.TPredicate().test(exception);

    assertFalse(isDataException);
  }

  @Test
  void shouldReturnDataExceptionWithSqlStateWhenExceptionIsDataException() {
    PgException exception = createPgExceptionFromMap((getDataLengthMismatch()));
    final DataException resultException = new DataExceptionTranslation.TFunction().apply(exception);

    assertThat(resultException.getSqlState(), equalTo(PSQLState.STRING_DATA_RIGHT_TRUNCATION.getCode()));
  }

  @Test
  void shouldReturnDataExceptionWithNoFieldsWhenExceptionIsNull() {
    PgException exception = createPgExceptionFromMap((getErrorMapWithSqlStateNull()));
    final DataException resultException = new DataExceptionTranslation.TFunction().apply(exception);

    assertNull(resultException.getSqlState());
    assertNull(resultException.getMessage());
  }
}
