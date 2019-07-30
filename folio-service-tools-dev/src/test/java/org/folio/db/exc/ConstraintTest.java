package org.folio.db.exc;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;

import org.folio.test.junit.TestStartLoggingRule;

public class ConstraintTest {

  private static final String CONS_NAME = "foo_constraint";
  private static final String CONS_TABLE = "foo_table";
  private static final String CONS_COL_1 = "foo_col1";
  private static final String CONS_COL_2 = "foo_col2";

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();
  @Rule
  public ExpectedException thrown = ExpectedException.none();


  @Test
  public void constructsPrimaryKeyConstraint() {
    Constraint cons = Constraint.primaryKey(CONS_NAME, CONS_TABLE, CONS_COL_1, CONS_COL_2);

    assertThat(cons.getType(), is(Constraint.Type.PRIMARY_KEY));
    assertThat(cons.getName(), is(CONS_NAME));
    assertThat(cons.getTable(), is(CONS_TABLE));
    assertThat(cons.getColumns(), containsInAnyOrder(CONS_COL_1, CONS_COL_2));
  }

  @Test
  public void constructsForeignKeyConstraint() {
    Constraint cons = Constraint.foreignKey(CONS_NAME, CONS_TABLE, CONS_COL_1);

    assertThat(cons.getType(), is(Constraint.Type.FOREIGN_KEY));
    assertThat(cons.getName(), is(CONS_NAME));
    assertThat(cons.getTable(), is(CONS_TABLE));
    assertThat(cons.getColumns(), containsInAnyOrder(CONS_COL_1));
  }

  @Test
  public void constructsUniqueConstraint() {
    Constraint cons = Constraint.unique(CONS_NAME, CONS_TABLE, CONS_COL_2);

    assertThat(cons.getType(), is(Constraint.Type.UNIQUE));
    assertThat(cons.getName(), is(CONS_NAME));
    assertThat(cons.getTable(), is(CONS_TABLE));
    assertThat(cons.getColumns(), containsInAnyOrder(CONS_COL_2));
  }

  @Test
  public void constructsCheckConstraint() {
    Constraint cons = Constraint.check(CONS_NAME, CONS_TABLE);

    assertThat(cons.getType(), is(Constraint.Type.CHECK));
    assertThat(cons.getName(), is(CONS_NAME));
    assertThat(cons.getTable(), is(CONS_TABLE));
    assertThat(cons.getColumns(), empty());
  }

  @Test
  public void constructsNotNullConstraint() {
    Constraint cons = Constraint.notNull(CONS_NAME, CONS_TABLE, CONS_COL_1);

    assertThat(cons.getType(), is(Constraint.Type.NOT_NULL));
    assertThat(cons.getName(), is(CONS_NAME));
    assertThat(cons.getTable(), is(CONS_TABLE));
    assertThat(cons.getColumns(), containsInAnyOrder(CONS_COL_1));
  }

  @Test
  public void constructsOtherConstraint() {
    Constraint cons = Constraint.other(CONS_NAME, CONS_TABLE);

    assertThat(cons.getType(), is(Constraint.Type.OTHER));
    assertThat(cons.getName(), is(CONS_NAME));
    assertThat(cons.getTable(), is(CONS_TABLE));
    assertThat(cons.getColumns(), empty());
  }

  @Test
  public void filtersOutNullColumns() {
    Constraint cons = Constraint.primaryKey(CONS_NAME, CONS_TABLE, null, CONS_COL_2, null);

    assertThat(cons.getColumns(), containsInAnyOrder(CONS_COL_2));
  }

  @Test
  public void failedIfTableNameIsNull() {
    thrown.expect(NullPointerException.class);

    Constraint.other(CONS_NAME, null);
  }

  @Test
  public void failedIfTableNameIsBlank() {
    thrown.expect(IllegalArgumentException.class);

    Constraint.other(CONS_NAME, "  \t");
  }

  @Test
  public void basicEquals() {
    Constraint cons = Constraint.primaryKey(CONS_NAME, CONS_TABLE, CONS_COL_1, CONS_COL_2);

    boolean result = cons.equals(Constraint.primaryKey(CONS_NAME, CONS_TABLE, CONS_COL_1, CONS_COL_2));
    assertThat(result, is(true));

    result = cons.equals(Constraint.primaryKey(CONS_NAME, CONS_TABLE, CONS_COL_1));
    assertThat(result, is(false));

    result = cons.equals(Constraint.foreignKey(CONS_NAME, CONS_TABLE, CONS_COL_1, CONS_COL_2));
    assertThat(result, is(false));
  }

  @Test
  public void basicToString() {
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
