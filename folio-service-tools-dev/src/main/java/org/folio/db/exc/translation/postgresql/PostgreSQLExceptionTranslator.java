package org.folio.db.exc.translation.postgresql;

import com.github.jasync.sql.db.postgresql.exceptions.GenericDatabaseException;

import org.folio.common.pf.PartialFunction;
import org.folio.db.exc.DatabaseException;
import org.folio.db.exc.translation.DBExceptionTranslator;

public class PostgreSQLExceptionTranslator extends DBExceptionTranslator {

  private PartialFunction<GenericDatabaseException, DatabaseException> fTranslation;

  public PostgreSQLExceptionTranslator() {
    fTranslation = InvalidUUIDTranslation.asPartial()
                    .orElse(DataExceptionTranslation.asPartial())
                    .orElse(ConstrainViolationTranslation.asPartial())
                    .orElse(AuthorizationExceptionTranslation.asPartial())
                    .orElse(GenericDBExceptionTranslation.asPartial());
  }

  @Override
  public boolean acceptable(Throwable exc) {
    return (exc instanceof com.github.jasync.sql.db.exceptions.DatabaseException);
  }

  @Override
  protected DatabaseException doTranslation(Throwable exc) {
    return (exc instanceof GenericDatabaseException)
              ? fTranslation.apply((GenericDatabaseException) exc) // operate with GenericDatabaseException at the moment
              : new DatabaseException(exc); // the rest is just wrapped into DatabaseException
  }

}
