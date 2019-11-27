package org.folio.db.exc.translation.postgresql;

import static org.folio.db.exc.translation.postgresql.TranslationUtils.exceptionWithSQLStateClass;

import java.util.function.Function;
import java.util.function.Predicate;

import com.github.jasync.sql.db.postgresql.exceptions.GenericDatabaseException;

import org.folio.common.pf.PartialFunction;
import org.folio.common.pf.PartialFunctions;
import org.folio.db.exc.AuthorizationException;
import org.folio.db.exc.DatabaseException;

class AuthorizationExceptionTranslation {

  private AuthorizationExceptionTranslation() {
  }

  static PartialFunction<GenericDatabaseException, DatabaseException> asPartial() {
    return PartialFunctions.pf(new TPredicate(), new TFunction());
  }

  static class TPredicate implements Predicate<GenericDatabaseException> {

    @Override
    public boolean test(GenericDatabaseException exc) {
      return exceptionWithSQLStateClass(exc, PSQLState.INVALID_AUTHORIZATION_SPECIFICATION);
    }
  }

  static class TFunction implements Function<GenericDatabaseException, AuthorizationException> {

    @Override
    public AuthorizationException apply(GenericDatabaseException exc) {
      ErrorMessageAdapter em = new ErrorMessageAdapter(exc);

      String sqlState = em.getSQLState().orElse(null);
      String msg = em.getMessage().orElse(null);

      return new AuthorizationException(msg, exc, sqlState);
    }
  }
}
