package org.folio.db.exc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import org.folio.test.extensions.TestStartLoggingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ConstraintTest {

  private static final String CONS_NAME = "foo_constraint";
  private static final String CONS_TABLE = "foo_table";
  private static final String CONS_COL_1 = "foo_col1";
  private static final String CONS_COL_2 = "foo_col2";

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();

  @Test
  void constructsPrimaryKeyConstraint() {
    Constraint cons = Constraint.primaryKey(CONS_NAME, CONS_TABLE, CONS_COL_1, CONS_COL_2);

    assertThat(cons.getType(), is(Constraint.Type.PRIMARY_KEY));
    assertThat(cons.getName(), is(CONS_NAME));
    assertThat(cons.getTable(), is(CONS_TABLE));
    assertThat(cons.getColumns(), containsInAnyOrder(CONS_COL_1, CONS_COL_2));
  }

  @Test
  void constructsForeignKeyConstraint() {
    Constraint cons = Constraint.foreignKey(CONS_NAME, CONS_TABLE, CONS_COL_1);

    assertThat(cons.getType(), is(Constraint.Type.FOREIGN_KEY));
    assertThat(cons.getName(), is(CONS_NAME));
    assertThat(cons.getTable(), is(CONS_TABLE));
    assertThat(cons.getColumns(), containsInAnyOrder(CONS_COL_1));
  }

  @Test
  void constructsUniqueConstraint() {
    Constraint cons = Constraint.unique(CONS_NAME, CONS_TABLE, CONS_COL_2);

    assertThat(cons.getType(), is(Constraint.Type.UNIQUE));
    assertThat(cons.getName(), is(CONS_NAME));
    assertThat(cons.getTable(), is(CONS_TABLE));
    assertThat(cons.getColumns(), containsInAnyOrder(CONS_COL_2));
  }

  @Test
  void constructsCheckConstraint() {
    Constraint cons = Constraint.check(CONS_NAME, CONS_TABLE);

    assertThat(cons.getType(), is(Constraint.Type.CHECK));
    assertThat(cons.getName(), is(CONS_NAME));
    assertThat(cons.getTable(), is(CONS_TABLE));
    assertThat(cons.getColumns(), empty());
  }

  @Test
  void constructsNotNullConstraint() {
    Constraint cons = Constraint.notNull(CONS_NAME, CONS_TABLE, CONS_COL_1);

    assertThat(cons.getType(), is(Constraint.Type.NOT_NULL));
    assertThat(cons.getName(), is(CONS_NAME));
    assertThat(cons.getTable(), is(CONS_TABLE));
    assertThat(cons.getColumns(), containsInAnyOrder(CONS_COL_1));
  }

  @Test
  void constructsOtherConstraint() {
    Constraint cons = Constraint.other(CONS_NAME, CONS_TABLE);

    assertThat(cons.getType(), is(Constraint.Type.OTHER));
    assertThat(cons.getName(), is(CONS_NAME));
    assertThat(cons.getTable(), is(CONS_TABLE));
    assertThat(cons.getColumns(), empty());
  }

  @Test
  void filtersOutNullColumns() {
    Constraint cons = Constraint.primaryKey(CONS_NAME, CONS_TABLE, null, CONS_COL_2, null);

    assertThat(cons.getColumns(), containsInAnyOrder(CONS_COL_2));
  }

  @Test
  void basicEquals() {
    Constraint cons = Constraint.primaryKey(CONS_NAME, CONS_TABLE, CONS_COL_1, CONS_COL_2);

    boolean result = cons.equals(Constraint.primaryKey(CONS_NAME, CONS_TABLE, CONS_COL_1, CONS_COL_2));
    assertThat(result, is(true));

    result = cons.equals(Constraint.primaryKey(CONS_NAME, CONS_TABLE, CONS_COL_1));
    assertThat(result, is(false));

    result = cons.equals(Constraint.foreignKey(CONS_NAME, CONS_TABLE, CONS_COL_1, CONS_COL_2));
    assertThat(result, is(false));
  }

  @Test
  void basicToString() {
    Constraint cons = Constraint.primaryKey(CONS_NAME, CONS_TABLE, CONS_COL_1, CONS_COL_2);

    String str = cons.toString();

    assertThat(str, allOf(
      containsString(Constraint.Type.PRIMARY_KEY.toString()),
      containsString(CONS_NAME),
      containsString(CONS_TABLE),
      containsString(CONS_COL_1),
      containsString(CONS_COL_2)
    ));
  }

}
