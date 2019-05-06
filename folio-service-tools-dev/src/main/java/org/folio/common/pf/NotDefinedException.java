package org.folio.common.pf;

public class NotDefinedException extends RuntimeException {

  public NotDefinedException(Object arg) {
    super("Not defined for: " + arg);
  }

}
