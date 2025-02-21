package org.folio.db.exc;

public class DatabaseException extends RuntimeException {

  private final String sqlState;

  public DatabaseException(String message) {
   this(message, null, null);
  }

  public DatabaseException(String message, Throwable cause) {
    this(message, cause, null);
  }

  public DatabaseException(Throwable cause) {
    super(cause);
    sqlState = null;
  }

  public DatabaseException(String message, String sqlState) {
    this(message, null, sqlState);
  }

  public DatabaseException(String message, Throwable cause, String sqlState) {
    super(message, cause);
    this.sqlState = sqlState;
  }

  public String getSqlState() {
    return sqlState;
  }
}
