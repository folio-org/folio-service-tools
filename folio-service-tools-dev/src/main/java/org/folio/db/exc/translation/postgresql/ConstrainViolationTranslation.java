package org.folio.db.exc.translation.postgresql;

import static org.apache.commons.lang3.StringUtils.defaultString;

import static org.folio.db.exc.translation.postgresql.TranslationUtils.exceptionWithSQLStateClass;

import java.util.function.Function;
import java.util.function.Predicate;

import com.github.mauricio.async.db.postgresql.exceptions.GenericDatabaseException;

import org.folio.common.pf.PartialFunction;
import org.folio.common.pf.PartialFunctions;
import org.folio.db.exc.Constraint;
import org.folio.db.exc.ConstraintViolationException;
import org.folio.db.exc.DatabaseException;

class ConstrainViolationTranslation {

  private ConstrainViolationTranslation() {
  }

  static PartialFunction<GenericDatabaseException, DatabaseException> asPartial() {
    return PartialFunctions.pf(new TPredicate(), new TFunction());
  }

  static class TPredicate implements Predicate<GenericDatabaseException> {

    @Override
    public boolean test(GenericDatabaseException exc) {
      return exceptionWithSQLStateClass(exc, PSQLState.INTEGRITY_CONSTRAINT_VIOLATION);
    }
  }

  static class TFunction implements Function<GenericDatabaseException, ConstraintViolationException> {

    private static final String PK_PREFIX = "pk_";

    @Override
    @SuppressWarnings("squid:S3655")
    public ConstraintViolationException apply(GenericDatabaseException exc) {
      ErrorMessageAdapter em = new ErrorMessageAdapter(exc);

      PSQLState sqlState = em.getPSQLState().get(); // predicate should test the message already, it's save to call get()
      String msg = em.getMessage().orElse(null);
      String details = em.getDetailedMessage().orElse(null);

      String table = em.getTable().orElse(null);
      String constName = em.getName().orElse(null);
      // for some types of exception (FK, PK, UNIQUE violations) the columns can be extracted from Detail msg
      // but for now only take it from the column field
      String column = em.getColumn().orElse(null);

      Constraint constraint = createConstraint(sqlState, constName, table, column);

      return new ConstraintViolationException(msg, exc, sqlState.getCode(), details, constraint);
    }

    private Constraint createConstraint(PSQLState sqlState, String constName, String table, String column) {
      Constraint constraint;
      switch (sqlState) {
        case NOT_NULL_VIOLATION:
          constraint = Constraint.notNull(constName, table, column);
          break;
        case FOREIGN_KEY_VIOLATION:
          constraint = Constraint.foreignKey(constName, table);
          break;
        case UNIQUE_VIOLATION:
          // !!!!! Note:
          // There is no difference in postgresql-async lib between "Primary Key" Constraint and "Unique" Constraint
          // although in PostgreSQL they are defined with different keywords:
          //    - PRIMARY KEY
          //    - UNIQUE.
          // So to differentiate them somehow the name of PK constraint should start from "pk_"
          constraint = defaultString(constName).toLowerCase().startsWith(PK_PREFIX)
                          ? Constraint.primaryKey(constName, table)
                          : Constraint.unique(constName, table);
          break;
        case CHECK_VIOLATION:
          constraint = Constraint.check(constName, table);
          break;
        // the rest goes as OTHER constraint type for now
        case INTEGRITY_CONSTRAINT_VIOLATION:
        case RESTRICT_VIOLATION:
        case EXCLUSION_VIOLATION:
          constraint = Constraint.other(constName, table);
          break;
        default:
          throw new IllegalArgumentException("No constraint defined for SQLState: " + sqlState.getCode());
      }
      return constraint;
    }
  }

}
