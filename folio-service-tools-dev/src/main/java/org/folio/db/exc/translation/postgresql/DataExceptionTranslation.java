package org.folio.db.exc.translation.postgresql;

import static org.folio.db.exc.translation.postgresql.TranslationUtils.exceptionWithSQLStateClass;

import java.util.function.Function;
import java.util.function.Predicate;

import io.vertx.pgclient.PgException;

import org.folio.common.pf.PartialFunction;
import org.folio.common.pf.PartialFunctions;
import org.folio.db.exc.DataException;
import org.folio.db.exc.DatabaseException;

class DataExceptionTranslation {

  private DataExceptionTranslation() {
  }

  static PartialFunction<PgException, DatabaseException> asPartial() {
    return PartialFunctions.pf(new TPredicate(), new TFunction());
  }

  static class TPredicate implements Predicate<PgException> {

    @Override
    public boolean test(PgException exc) {
      return exceptionWithSQLStateClass(exc, PSQLState.DATA_EXCEPTION);
    }
  }

  static class TFunction implements Function<PgException, DataException> {

    @Override
    public DataException apply(PgException exc) {
      PgExceptionAdapter em = new PgExceptionAdapter(exc);

      String sqlState = em.getSQLState().orElse(null);
      String msg = em.getMessage().orElse(null);

      return new DataException(msg, exc, sqlState);
    }
  }
}
