package org.folio.db.exc.translation.postgresql;

import io.vertx.pgclient.PgException;

import org.folio.common.pf.PartialFunction;
import org.folio.db.exc.DatabaseException;
import org.folio.db.exc.translation.DBExceptionTranslator;

public class PostgreSQLExceptionTranslator extends DBExceptionTranslator {

  private PartialFunction<PgException, DatabaseException> fTranslation;

  public PostgreSQLExceptionTranslator() {
    fTranslation = InvalidUUIDTranslation.asPartial()
      .orElse(DataExceptionTranslation.asPartial())
      .orElse(ConstrainViolationTranslation.asPartial())
      .orElse(AuthorizationExceptionTranslation.asPartial())
      .orElse(GenericDBExceptionTranslation.asPartial());
  }

  @Override
  public boolean acceptable(Throwable exc) {
    return (exc instanceof PgException);
  }

  @Override
  protected DatabaseException doTranslation(Throwable exc) {
    return (exc instanceof PgException)
      ? fTranslation.apply((PgException) exc) // operate with GenericDatabaseException at the moment
      : new DatabaseException(exc); // the rest is just wrapped into DatabaseException
  }

}
