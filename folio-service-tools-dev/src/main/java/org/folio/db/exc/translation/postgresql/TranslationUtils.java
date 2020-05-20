package org.folio.db.exc.translation.postgresql;

import io.vertx.pgclient.PgException;

class TranslationUtils {

  private TranslationUtils() {
  }

  static boolean exceptionWithSQLStateClass(PgException exc, PSQLState expectedClass) {
    PgExceptionAdapter em = new PgExceptionAdapter(exc);

    return em.getPSQLState()
      .map(state -> state.belongToClassOf(expectedClass))
      .orElse(false);
  }

  static boolean exceptionWithSQLState(PgException exc, PSQLState expectedState) {
    PgExceptionAdapter em = new PgExceptionAdapter(exc);

    return em.getPSQLState()
      .map(state -> state == expectedState)
      .orElse(false);
  }
}
