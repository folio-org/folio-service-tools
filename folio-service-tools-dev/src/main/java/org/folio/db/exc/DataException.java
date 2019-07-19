package org.folio.db.exc;

public class DataException extends DatabaseException {

  public DataException(String message) {
    super(message);
  }

  public DataException(String message, String sqlState) {
    super(message, sqlState);
  }

  public DataException(String message, Throwable cause) {
    super(message, cause);
  }

  public DataException(String message, Throwable cause, String sqlState) {
    super(message, cause, sqlState);
  }
}
