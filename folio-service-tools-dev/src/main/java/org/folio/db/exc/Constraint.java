package org.folio.db.exc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Constraint {

  public enum Type {
    PRIMARY_KEY,
    FOREIGN_KEY,
    UNIQUE,
    CHECK,
    NOT_NULL,
    OTHER // for not need / not processed types at the moment
  }

  private Type type;
  private String name;
  private String table;
  private List<String> columns;
  

  public static Constraint primaryKey(String name, String table, String... columns) {
    return new Constraint(Type.PRIMARY_KEY, name, table, columns);
  }

  public static Constraint foreingKey(String name, String table, String... columns) {
    return new Constraint(Type.FOREIGN_KEY, name, table, columns);
  }

  public static Constraint unique(String name, String table, String... columns) {
    return new Constraint(Type.UNIQUE, name, table, columns);
  }

  public static Constraint check(String name, String table) {
    return new Constraint(Type.CHECK, name, table);
  }

  public static Constraint notNull(String name, String table, String column) {
    return new Constraint(Type.NOT_NULL, name, table, column);
  }

  public static Constraint other(String name, String table) {
    return new Constraint(Type.OTHER, name, table);
  }

  private Constraint(Type type, String name, String table, String... columns) {
    Validate.notNull(type, "Constraint type is null");
    //Validate.notBlank(name, "Constraint name is empty");

    String[] cols = ArrayUtils.nullToEmpty(columns);
    Validate.noNullElements(cols);

    this.type = type;
    this.name = name;
    this.table = table;
    this.columns = Arrays.asList(cols);
  }

  public Type getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public String getTable() {
    return table;
  }

  public List<String> getColumns() {
    return Collections.unmodifiableList(columns);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    Constraint that = (Constraint) o;

    return type == that.type &&
      Objects.equals(name, that.name) &&
      Objects.equals(table, that.table) &&
      Objects.equals(columns, that.columns);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, name, table, columns);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
      .append("type", type)
      .append("name", name)
      .append("table", table)
      .append("columns", columns)
      .toString();
  }
}
