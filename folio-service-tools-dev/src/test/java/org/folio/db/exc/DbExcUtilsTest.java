package org.folio.db.exc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.folio.test.extensions.TestStartLoggingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;


class DbExcUtilsTest {

  private static final String CONS_NAME = "foo_constraint";
  private static final String CONS_TABLE = "foo_table";
  private static final String CONS_COL_1 = "foo_col1";

  private static final Exception SOME_EXCEPTION = new Exception("FAILURE");
  private static final ConstraintViolationException UNIQUE_EXCEPTION = new ConstraintViolationException(
      "UNIQUE", "23505", Constraint.unique(CONS_NAME, CONS_TABLE, CONS_COL_1));
  private static final ConstraintViolationException PK_EXCEPTION = new ConstraintViolationException(
      "PK", "23505", Constraint.primaryKey(CONS_NAME, CONS_TABLE, CONS_COL_1));
  private static final ConstraintViolationException FK_EXCEPTION = new ConstraintViolationException(
      "FK", "23503", Constraint.foreignKey(CONS_NAME, CONS_TABLE, CONS_COL_1));
  private static final ConstraintViolationException NN_EXCEPTION = new ConstraintViolationException(
      "NOTNULL", "23502", Constraint.notNull(CONS_NAME, CONS_TABLE, CONS_COL_1));
  private static final ConstraintViolationException CHECK_EXCEPTION = new ConstraintViolationException(
      "CHECK", "23514", Constraint.check(CONS_NAME, CONS_TABLE));

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();

  @Test
  void testUniqueViolation() {
    assertTrue(DbExcUtils.isUniqueViolation(UNIQUE_EXCEPTION));

    assertFalse(DbExcUtils.isUniqueViolation(SOME_EXCEPTION));
    assertFalse(DbExcUtils.isUniqueViolation(PK_EXCEPTION));
  }

  @Test
  void testPKViolation() {
    assertTrue(DbExcUtils.isPKViolation(PK_EXCEPTION));

    assertFalse(DbExcUtils.isPKViolation(SOME_EXCEPTION));
    assertFalse(DbExcUtils.isPKViolation(UNIQUE_EXCEPTION));
  }

  @Test
  void testFKViolation() {
    assertTrue(DbExcUtils.isFKViolation(FK_EXCEPTION));

    assertFalse(DbExcUtils.isFKViolation(SOME_EXCEPTION));
    assertFalse(DbExcUtils.isFKViolation(PK_EXCEPTION));
  }

  @Test
  void testNotNullViolation() {
    assertTrue(DbExcUtils.isNotNullViolation(NN_EXCEPTION));

    assertFalse(DbExcUtils.isNotNullViolation(SOME_EXCEPTION));
    assertFalse(DbExcUtils.isNotNullViolation(PK_EXCEPTION));
  }

  @Test
  void testCheckViolation() {
    assertTrue(DbExcUtils.isCheckViolation(CHECK_EXCEPTION));

    assertFalse(DbExcUtils.isCheckViolation(SOME_EXCEPTION));
    assertFalse(DbExcUtils.isCheckViolation(PK_EXCEPTION));
  }

}
