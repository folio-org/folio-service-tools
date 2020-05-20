package org.folio.db.exc.translation.postgresql;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertx.pgclient.PgException;

import org.folio.rest.persist.PgExceptionUtil;

class PgExceptionAdapter {

  private static final char FIELD_SCHEMA = 's';

  private static final Pattern TABLE_PATTERN = Pattern.compile("(table|relation)\\s+\"(.*)\"\\s+");
  private static final Pattern CONSTRAINT_PATTERN = Pattern.compile("(constraint)\\s+\"(.+)\"");
  private static final Pattern COLUMN_PATTERN = Pattern.compile("(column)\\s+\"(.+)\"");

  private final PgException dbe;

  PgExceptionAdapter(PgException dbe) {
    this.dbe = dbe;
  }

  Optional<String> getSQLState() {
    return getField(InformationMessageConstants.SQL_STATE);
  }

  Optional<PSQLState> getPSQLState() {
    String sqlState = getField(InformationMessageConstants.SQL_STATE).orElse(EMPTY);

    return PSQLState.contains(sqlState) ? Optional.of(PSQLState.enumOf(sqlState)) : Optional.empty();
  }

  Optional<String> getMessage() {
    return getField(InformationMessageConstants.MESSAGE);
  }

  Optional<String> getDetailedMessage() {
    return getField(InformationMessageConstants.DETAIL);
  }

  Optional<String> getSchema() {
    return getField(FIELD_SCHEMA);
  }

  Optional<String> getTable() {
    return extractFromException(TABLE_PATTERN);
  }

  Optional<String> getName() {
    return extractFromException(CONSTRAINT_PATTERN);
  }

  Optional<String> getColumn() {
    return extractFromException(COLUMN_PATTERN);
  }

  private Optional<String> extractFromException(Pattern pattern) {
    Optional<String> s = extractFromMessage(pattern, getMessage());
    return s.isPresent() ? s : extractFromMessage(pattern, getDetailedMessage());
  }

  private Optional<String> extractFromMessage(Pattern pattern, Optional<String> message) {
    if (message.isPresent()) {
      Matcher matcher = pattern.matcher(message.get());
      if (matcher.find()) {
        return Optional.ofNullable(matcher.group(2));
      }
    }
    return Optional.empty();
  }

  private Optional<String> getField(char name) {
    return Optional.ofNullable(PgExceptionUtil.get(dbe, name));
  }
}
