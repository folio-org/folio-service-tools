package org.folio.db.exc.translation.postgresql;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.folio.db.ErrorFactory.getDataLengthMismatch;
import static org.folio.db.ErrorFactory.getErrorMapWithSqlStateNull;
import static org.folio.db.ErrorFactory.getInvalidPasswordErrorMap;

import com.github.mauricio.async.db.postgresql.exceptions.GenericDatabaseException;
import com.github.mauricio.async.db.postgresql.messages.backend.ErrorMessage;
import com.github.mauricio.async.db.postgresql.messages.backend.InformationMessage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import org.folio.common.pf.PartialFunction;
import org.folio.db.exc.AuthorizationException;
import org.folio.db.exc.DatabaseException;
import org.folio.test.junit.TestStartLoggingRule;


public class AuthorizationExceptionTranslationTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();

  @Test
  public void shouldReturnDatabaseExceptionWithInvalidAuthorizationCode(){
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getInvalidPasswordErrorMap()));
    final PartialFunction<GenericDatabaseException, DatabaseException> asPartial =
      AuthorizationExceptionTranslation.asPartial();

    final DatabaseException apply = asPartial.apply(exception);

    assertThat(apply.getSqlState(), equalTo(PSQLState.INVALID_PASSWORD.getCode()));
  }

  @Test
  public void shouldReturnTrueWhenExceptionIsAuthorizationException(){
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getInvalidPasswordErrorMap()));
    final boolean isAuthException = new AuthorizationExceptionTranslation.TPredicate().test(exception);

    assertTrue(isAuthException);
  }

  @Test
  public void shouldReturnFalseWhenExceptionIsNotAuthorizationException(){
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getDataLengthMismatch()));
    final boolean isAuthException = new AuthorizationExceptionTranslation.TPredicate().test(exception);

    assertFalse(isAuthException);
  }

  @Test
  public void shouldReturnFalseWhenExceptionIsNull(){
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getErrorMapWithSqlStateNull()));
    final boolean isAuthException = new AuthorizationExceptionTranslation.TPredicate().test(exception);

    assertFalse(isAuthException);
  }

  @Test
  public void shouldReturnAuthorizationExceptionWithSqlStateWhenExceptionIsInvalidAuthorization(){
    ErrorMessage errorMessage = new ErrorMessage(getInvalidPasswordErrorMap());
    GenericDatabaseException exception = new GenericDatabaseException(errorMessage);

    final AuthorizationException resultException = new AuthorizationExceptionTranslation.TFunction().apply(exception);

    assertThat(resultException.getSqlState(), equalTo(PSQLState.INVALID_PASSWORD.getCode()));
    assertThat(resultException.getCause(), is(exception));
    assertThat(resultException.getMessage(), equalTo(errorMessage.fields().get(InformationMessage.Message()).get()));
  }
}
