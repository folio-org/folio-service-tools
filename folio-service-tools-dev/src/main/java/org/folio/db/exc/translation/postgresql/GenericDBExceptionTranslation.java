package org.folio.db.exc.translation.postgresql;

import java.util.function.Function;
import java.util.function.Predicate;

import com.github.jasync.sql.db.postgresql.exceptions.GenericDatabaseException;

import org.folio.common.pf.PartialFunction;
import org.folio.common.pf.PartialFunctions;
import org.folio.db.exc.DatabaseException;

class GenericDBExceptionTranslation {

  private GenericDBExceptionTranslation() {
  }

  static PartialFunction<GenericDatabaseException, DatabaseException> asPartial() {
    return PartialFunctions.pf(new TPredicate(), new TFunction());
  }

  static class TPredicate implements Predicate<GenericDatabaseException> {
    @Override
    public boolean test(GenericDatabaseException exc) {
      return true;
    }
  }

  static class TFunction implements Function<GenericDatabaseException, DatabaseException> {

    @Override
    public DatabaseException apply(GenericDatabaseException e) {
      ErrorMessageAdapter em = new ErrorMessageAdapter(e);

      String sqlState = em.getSQLState().orElse(null);
      String msg = em.getMessage().orElse(null);

      return new DatabaseException(msg, e, sqlState);
    }
  }
}
