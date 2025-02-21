package org.folio.db.exc;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.Validate;

public class ConstraintViolationException extends DatabaseException {

  private final String detailedMessage;
  private final Constraint constraint;
  private final Map<String, String> invalidValues;

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
    this.invalidValues = new HashMap<>();
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

  public void addInvalidValue(String fieldName, String value) {
    Validate.notBlank(fieldName);
    invalidValues.put(fieldName, value);
  }

  public Map<String, String> getInvalidValues() {
    return new HashMap<>(invalidValues);
  }

}
