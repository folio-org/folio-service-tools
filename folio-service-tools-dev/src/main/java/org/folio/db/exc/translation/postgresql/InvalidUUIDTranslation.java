package org.folio.db.exc.translation.postgresql;

import static org.folio.db.exc.translation.postgresql.TranslationUtils.exceptionWithSQLState;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertx.pgclient.PgException;
import org.apache.commons.lang3.StringUtils;

import org.folio.common.pf.PartialFunction;
import org.folio.common.pf.PartialFunctions;
import org.folio.db.exc.DatabaseException;
import org.folio.db.exc.InvalidUUIDException;

class InvalidUUIDTranslation {

  private static final Pattern INVALID_UUID_MSG_PATTERN =
    Pattern.compile("invalid input syntax for type uuid:\\s*\"(.*)\"");

  private InvalidUUIDTranslation() {
  }

  static PartialFunction<PgException, DatabaseException> asPartial() {
    return PartialFunctions.pf(new TPredicate(), new TFunction());
  }

  static class TPredicate implements Predicate<PgException> {

    @Override
    public boolean test(PgException exc) {
      return exceptionWithSQLState(exc, PSQLState.INVALID_TEXT_REPRESENTATION) &&
        hasInvalidUUIDMessage(exc);
    }

    private boolean hasInvalidUUIDMessage(PgException exc) {
      PgExceptionAdapter em = new PgExceptionAdapter(exc);

      String msg = em.getMessage().orElse(StringUtils.EMPTY);

      return INVALID_UUID_MSG_PATTERN.matcher(msg).matches();
    }
  }

  static class TFunction implements Function<PgException, InvalidUUIDException> {

    @Override
    @SuppressWarnings("squid:S3655")
    public InvalidUUIDException apply(PgException exc) {
      PgExceptionAdapter em = new PgExceptionAdapter(exc);

      String msg = em.getMessage().get(); // predicate should test the message already, it's save to call get()
      String sqlState = em.getSQLState().get(); // ... same for the state
      String uuid = extractInvalidUUID(msg);

      return new InvalidUUIDException(msg, exc, sqlState, uuid);
    }

    private String extractInvalidUUID(String msg) {
      Matcher matcher = INVALID_UUID_MSG_PATTERN.matcher(msg);
      return matcher.matches()
        ? matcher.group(1)
        : StringUtils.EMPTY; // do not throw any exception for now
    }

  }

}
