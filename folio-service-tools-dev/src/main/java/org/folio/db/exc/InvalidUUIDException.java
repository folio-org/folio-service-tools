package org.folio.db.exc;

public class InvalidUUIDException extends DataException {

  private String invalidValue;


  public InvalidUUIDException(String message, String sqlState, String invalidValue) {
    this(message, null, sqlState, invalidValue);
  }

  public InvalidUUIDException(String message, Throwable cause, String sqlState, String invalidValue) {
    super(message, cause, sqlState);
    this.invalidValue = invalidValue;
  }

  public String getInvalidValue() {
    return invalidValue;
  }
}
