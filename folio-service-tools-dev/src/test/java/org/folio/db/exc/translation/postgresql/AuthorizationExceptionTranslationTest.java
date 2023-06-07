package org.folio.db.exc.translation.postgresql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.folio.db.ErrorFactory.getDataLengthMismatch;
import static org.folio.db.ErrorFactory.getErrorMapWithSqlStateNull;
import static org.folio.db.ErrorFactory.getInvalidPasswordErrorMap;
import static org.folio.rest.persist.PgExceptionUtil.createPgExceptionFromMap;

import io.vertx.pgclient.PgException;
import org.folio.test.extensions.TestStartLoggingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.folio.db.exc.AuthorizationException;
import org.folio.db.exc.DatabaseException;


class AuthorizationExceptionTranslationTest {

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();

  @Test
  void shouldReturnDatabaseExceptionWithInvalidAuthorizationCode() {
    PgException exception = createPgExceptionFromMap((getInvalidPasswordErrorMap()));
    DatabaseException resultException = AuthorizationExceptionTranslation.asPartial().apply(exception);

    assertThat(resultException.getSqlState(), equalTo(PSQLState.INVALID_PASSWORD.getCode()));
  }

  @Test
  void shouldReturnTrueWhenExceptionIsAuthorizationException() {
    PgException exception = createPgExceptionFromMap((getInvalidPasswordErrorMap()));
    boolean isAuthException = new AuthorizationExceptionTranslation.TPredicate().test(exception);

    assertTrue(isAuthException);
  }

  @Test
  void shouldReturnFalseWhenExceptionIsNotAuthorizationException() {
    PgException exception = createPgExceptionFromMap((getDataLengthMismatch()));
    boolean isAuthException = new AuthorizationExceptionTranslation.TPredicate().test(exception);

    assertFalse(isAuthException);
  }

  @Test
  void shouldReturnFalseWhenExceptionIsNull() {
    PgException exception = createPgExceptionFromMap((getErrorMapWithSqlStateNull()));
    boolean isAuthException = new AuthorizationExceptionTranslation.TPredicate().test(exception);

    assertFalse(isAuthException);
  }

  @Test
  void shouldReturnAuthorizationExceptionWithSqlStateWhenExceptionIsInvalidAuthorization() {
    PgException exception = createPgExceptionFromMap(getInvalidPasswordErrorMap());

    AuthorizationException resultException = new AuthorizationExceptionTranslation.TFunction().apply(exception);

    assertThat(resultException.getSqlState(), equalTo(PSQLState.INVALID_PASSWORD.getCode()));
    assertThat(resultException.getCause(), is(exception));
    assertThat(resultException.getMessage(), equalTo(exception.getErrorMessage()));
  }
}
