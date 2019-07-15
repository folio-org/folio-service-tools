package org.folio.db.exc;

public class ConstraintViolationException extends DatabaseException {

  private String detailedMessage;
  private Constraint constraint;


  public ConstraintViolationException(String message, String sqlState, Constraint constraint) {
    this(message, sqlState, null, constraint);
  }

  public ConstraintViolationException(String message, String sqlState, String detailedMessage, Constraint constraint) {
    this(message, null, sqlState, detailedMessage, constraint);
  }

  public ConstraintViolationException(String message, Throwable cause, String sqlState, Constraint constraint) {
    this(message, cause, sqlState, null, constraint);
  }

  public ConstraintViolationException(String message, Throwable cause, String sqlState, String detailedMessage,
                                      Constraint constraint) {
    super(message, cause, sqlState);
    this.detailedMessage = detailedMessage;
    this.constraint = constraint;
  }

  public String getDetailedMessage() {
    return detailedMessage;
  }

  public Constraint getConstraint() {
    return constraint;
  }

  public Constraint.Type getConstraintType() {
    return constraint.getType();
  }
}
