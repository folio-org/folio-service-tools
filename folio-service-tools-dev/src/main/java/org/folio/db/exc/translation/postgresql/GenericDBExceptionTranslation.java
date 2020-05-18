package org.folio.db.exc.translation.postgresql;

import java.util.function.Function;
import java.util.function.Predicate;

import io.vertx.pgclient.PgException;

import org.folio.common.pf.PartialFunction;
import org.folio.common.pf.PartialFunctions;
import org.folio.db.exc.DatabaseException;

class GenericDBExceptionTranslation {

  private GenericDBExceptionTranslation() {
  }

  static PartialFunction<PgException, DatabaseException> asPartial() {
    return PartialFunctions.pf(new TPredicate(), new TFunction());
  }

  static class TPredicate implements Predicate<PgException> {

    @Override
    public boolean test(PgException exc) {
      return true;
    }
  }

  static class TFunction implements Function<PgException, DatabaseException> {

    @Override
    public DatabaseException apply(PgException e) {
      PgExceptionAdapter em = new PgExceptionAdapter(e);

      String sqlState = em.getSQLState().orElse(null);
      String msg = em.getMessage().orElse(null);

      return new DatabaseException(msg, e, sqlState);
    }
  }
}
