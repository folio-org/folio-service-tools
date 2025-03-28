package org.folio.db.exc.translation.postgresql;

import static org.apache.commons.lang3.StringUtils.defaultString;

import static org.folio.db.exc.translation.postgresql.TranslationUtils.exceptionWithSQLStateClass;

import java.util.function.Function;
import java.util.function.Predicate;

import io.vertx.pgclient.PgException;
import org.apache.commons.collections4.IterableGet;
import org.apache.commons.lang3.ArrayUtils;

import org.folio.common.pf.PartialFunction;
import org.folio.common.pf.PartialFunctions;
import org.folio.db.exc.Constraint;
import org.folio.db.exc.ConstraintViolationException;
import org.folio.db.exc.DatabaseException;

class ConstrainViolationTranslation {

  private ConstrainViolationTranslation() {
  }

  static PartialFunction<PgException, DatabaseException> asPartial() {
    return PartialFunctions.pf(new TPredicate(), new TFunction());
  }

  static class TPredicate implements Predicate<PgException> {

    @Override
    public boolean test(PgException exc) {
      return exceptionWithSQLStateClass(exc, PSQLState.INTEGRITY_CONSTRAINT_VIOLATION);
    }
  }

  static class TFunction implements Function<PgException, ConstraintViolationException> {

    private static final String PK_PREFIX = "pk_";

    @Override
    @SuppressWarnings("squid:S3655")
    public ConstraintViolationException apply(PgException exc) {
      PgExceptionAdapter em = new PgExceptionAdapter(exc);

      PSQLState sqlState = em.getPSQLState().get(); // predicate should test the message already, it's save to call get()
      String msg = em.getMessage().orElse(null);
      String details = em.getDetailedMessage().orElse(null);

      String table = em.getTable().orElse(null);
      String constName = em.getName().orElse(null);

      IterableGet<String, String> invalidValues = new InvalidValueParser(em).parse();
      // for some types of exception (FK, PK, UNIQUE violations) the columns can be extracted from Detail msg
      String[] columns = em.getColumn()
        .map(ArrayUtils::toArray)
        .orElse(invalidValues.keySet().toArray(new String[0]));

      Constraint constraint = createConstraint(sqlState, constName, table, columns);

      ConstraintViolationException cve = new ConstraintViolationException(msg, exc, sqlState.getCode(),
        details, constraint);

      invalidValues.entrySet().forEach(entry -> cve.addInvalidValue(entry.getKey(), entry.getValue()));

      return cve;
    }

    private Constraint createConstraint(PSQLState sqlState, String constName, String table, String[] columns) {
      return switch (sqlState) {
        case NOT_NULL_VIOLATION -> {
          // for this type of constraint single column is expected
          String column = columns.length == 1 ? columns[0] : null;
          yield Constraint.notNull(constName, table, column);
        }
        case FOREIGN_KEY_VIOLATION -> Constraint.foreignKey(constName, table, columns);
        case UNIQUE_VIOLATION ->
          // !!!!! Note:
          // There is no difference in postgresql-async lib between "Primary Key" Constraint and "Unique" Constraint
          // although in PostgreSQL they are defined with different keywords:
          //    - PRIMARY KEY
          //    - UNIQUE.
          // So to differentiate them somehow the name of PK constraint should start with "pk_"
          defaultString(constName).toLowerCase().startsWith(PK_PREFIX)
          ? Constraint.primaryKey(constName, table, columns)
          : Constraint.unique(constName, table, columns);
        case CHECK_VIOLATION -> Constraint.check(constName, table);
        // the rest goes as OTHER constraint type for now
        case INTEGRITY_CONSTRAINT_VIOLATION, RESTRICT_VIOLATION, EXCLUSION_VIOLATION ->
          Constraint.other(constName, table);
        default -> throw new IllegalArgumentException("No constraint defined for SQLState: " + sqlState.getCode());
      };
    }
  }

}
