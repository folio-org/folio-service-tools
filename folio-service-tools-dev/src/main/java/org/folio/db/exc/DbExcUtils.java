package org.folio.db.exc;

import java.util.Objects;

public final class DbExcUtils {

  private DbExcUtils() {
  }

  public static boolean isUniqueViolation(Throwable t) {
    return Objects.equals(getConstraintType(t), Constraint.Type.UNIQUE);
  }

  public static boolean isPKViolation(Throwable t) {
    return Objects.equals(getConstraintType(t), Constraint.Type.PRIMARY_KEY);
  }

  public static boolean isFKViolation(Throwable t) {
    return Objects.equals(getConstraintType(t), Constraint.Type.FOREIGN_KEY);
  }

  public static boolean isNotNullViolation(Throwable t) {
    return Objects.equals(getConstraintType(t), Constraint.Type.NOT_NULL);
  }

  public static boolean isCheckViolation(Throwable t) {
    return Objects.equals(getConstraintType(t), Constraint.Type.CHECK);
  }

  private static Constraint.Type getConstraintType(Throwable t) {
    return (t instanceof ConstraintViolationException)
      ? ((ConstraintViolationException) t).getConstraintType()
      : null;
  }

}
