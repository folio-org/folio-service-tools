package org.folio.db.exc.translation.postgresql;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.folio.db.ErrorFactory.getDataLengthMismatch;
import static org.folio.db.ErrorFactory.getErrorMapWithSqlStateNull;
import static org.folio.db.ErrorFactory.getInvalidPasswordErrorMap;
import static org.folio.rest.persist.PgExceptionUtil.createPgExceptionFromMap;

import io.vertx.pgclient.PgException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import org.folio.db.exc.AuthorizationException;
import org.folio.db.exc.DatabaseException;
import org.folio.test.junit.TestStartLoggingRule;


public class AuthorizationExceptionTranslationTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();

  @Test
  public void shouldReturnDatabaseExceptionWithInvalidAuthorizationCode() {
    PgException exception = createPgExceptionFromMap((getInvalidPasswordErrorMap()));
    DatabaseException resultException = AuthorizationExceptionTranslation.asPartial().apply(exception);

    assertThat(resultException.getSqlState(), equalTo(PSQLState.INVALID_PASSWORD.getCode()));
  }

  @Test
  public void shouldReturnTrueWhenExceptionIsAuthorizationException() {
    PgException exception = createPgExceptionFromMap((getInvalidPasswordErrorMap()));
    boolean isAuthException = new AuthorizationExceptionTranslation.TPredicate().test(exception);

    assertTrue(isAuthException);
  }

  @Test
  public void shouldReturnFalseWhenExceptionIsNotAuthorizationException() {
    PgException exception = createPgExceptionFromMap((getDataLengthMismatch()));
    boolean isAuthException = new AuthorizationExceptionTranslation.TPredicate().test(exception);

    assertFalse(isAuthException);
  }

  @Test
  public void shouldReturnFalseWhenExceptionIsNull() {
    PgException exception = createPgExceptionFromMap((getErrorMapWithSqlStateNull()));
    boolean isAuthException = new AuthorizationExceptionTranslation.TPredicate().test(exception);

    assertFalse(isAuthException);
  }

  @Test
  public void shouldReturnAuthorizationExceptionWithSqlStateWhenExceptionIsInvalidAuthorization() {
    PgException exception = createPgExceptionFromMap(getInvalidPasswordErrorMap());

    AuthorizationException resultException = new AuthorizationExceptionTranslation.TFunction().apply(exception);

    assertThat(resultException.getSqlState(), equalTo(PSQLState.INVALID_PASSWORD.getCode()));
    assertThat(resultException.getCause(), is(exception));
    assertThat(resultException.getMessage(), equalTo(exception.getMessage()));
  }
}
