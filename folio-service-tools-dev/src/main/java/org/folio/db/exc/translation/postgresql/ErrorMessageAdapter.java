package org.folio.db.exc.translation.postgresql;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Optional;

import com.github.jasync.sql.db.postgresql.exceptions.GenericDatabaseException;
import com.github.jasync.sql.db.postgresql.messages.backend.ErrorMessage;

class ErrorMessageAdapter {

  private static final char FIELD_SCHEMA = 's';
  private static final char FIELD_TABLE = 't';
  private static final char FIELD_NAME = 'n';
  private static final char FIELD_COLUMN = 'c';

  private ErrorMessage errorMessage;


  ErrorMessageAdapter(ErrorMessage errorMessage) {
    this.errorMessage = errorMessage;
  }

  ErrorMessageAdapter(GenericDatabaseException dbe) {
    this(dbe.getErrorMessage());
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
    return getField(FIELD_TABLE);
  }

  Optional<String> getName() {
    return getField(FIELD_NAME);
  }

  Optional<String> getColumn() {
    return getField(FIELD_COLUMN);
  }

  private Optional<String> getField(char name) {
    return Optional.ofNullable(errorMessage.getFields().get(name));
  }
}
