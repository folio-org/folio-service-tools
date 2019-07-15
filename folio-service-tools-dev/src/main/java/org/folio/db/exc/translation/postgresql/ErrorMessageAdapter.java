package org.folio.db.exc.translation.postgresql;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Optional;

import com.github.mauricio.async.db.postgresql.exceptions.GenericDatabaseException;
import com.github.mauricio.async.db.postgresql.messages.backend.ErrorMessage;
import com.github.mauricio.async.db.postgresql.messages.backend.InformationMessage;
import scala.collection.immutable.Map;

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
    this(dbe.errorMessage());
  }

  Optional<String> getSQLState() {
    return getField(InformationMessage.SQLState());
  }

  Optional<PSQLState> getPSQLState() {
    String sqlState = getField(InformationMessage.SQLState()).orElse(EMPTY);

    return PSQLState.contains(sqlState) ? Optional.of(PSQLState.enumOf(sqlState)) : Optional.empty();
  }

  Optional<String> getMessage() {
    return getField(InformationMessage.Message());
  }

  Optional<String> getDetailedMessage() {
    return getField(InformationMessage.Detail());
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
    Map<Object, String> errFields = errorMessage.fields();

    return errFields.get(name).fold(Optional::empty, Optional::of);
  }
}
