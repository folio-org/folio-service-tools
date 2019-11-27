package org.folio.db.exc.translation.postgresql;

import com.github.jasync.sql.db.postgresql.exceptions.GenericDatabaseException;

class TranslationUtils {

  private TranslationUtils() {
  }

  static boolean exceptionWithSQLStateClass(GenericDatabaseException exc, PSQLState expectedClass) {
    ErrorMessageAdapter em = new ErrorMessageAdapter(exc);

    return em.getPSQLState()
      .map(state -> state.belongToClassOf(expectedClass))
      .orElse(false);
  }

  static boolean exceptionWithSQLState(GenericDatabaseException exc, PSQLState expectedState) {
    ErrorMessageAdapter em = new ErrorMessageAdapter(exc);

    return em.getPSQLState()
      .map(state -> state == expectedState)
      .orElse(false);
  }
}
