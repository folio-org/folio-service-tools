package org.folio.db.exc;

public class AuthorizationException extends DatabaseException {

  public AuthorizationException(String message) {
    super(message);
  }

  public AuthorizationException(String message, Throwable cause) {
    super(message, cause);
  }

  public AuthorizationException(String message, String sqlState) {
    super(message, sqlState);
  }

  public AuthorizationException(String message, Throwable cause, String sqlState) {
    super(message, cause, sqlState);
  }
}
