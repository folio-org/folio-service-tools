package org.folio.service.exc;

public class InvalidDataException extends IllegalArgumentException {

  public InvalidDataException(String s) {
    super(s);
  }

  public InvalidDataException(String message, Throwable cause) {
    super(message, cause);
  }
}
