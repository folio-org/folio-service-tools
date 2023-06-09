package org.folio.db;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import org.junit.jupiter.api.Test;

import org.folio.rest.persist.helpers.LocalRowSet;

class RowSetUtilsTest {

  @Test
  void testStreamOf() {
    LocalRowSet rowSet = getTestRowSet();

    Stream<Row> actual = RowSetUtils.streamOf(rowSet);

    List<Row> rowList = actual.collect(Collectors.toList());
    assertThat(rowList, hasSize(2));
    assertThat(rowList.get(0).getColumnName(0), equalTo("id"));
    assertThat(rowList.get(0).getColumnName(1), equalTo("name"));
    assertThat(rowList.get(0).getInteger("id"), equalTo(1));
    assertThat(rowList.get(0).getString("name"), equalTo("test name1"));
  }

  @Test
  void testMapRowSet() {
    LocalRowSet rowSet = getTestRowSet();

    List<Integer> ids = RowSetUtils.mapItems(rowSet, row -> row.getInteger("id"));
    assertThat(ids, hasSize(2));
    assertThat(ids, hasItems(1, 2));
  }

  @Test
  void testMapFirstItemInRowSet() {
    LocalRowSet rowSet = getTestRowSet();

    Integer id = RowSetUtils.mapFirstItem(rowSet, row -> row.getInteger("id"));
    assertThat(id, equalTo(1));
  }

  @Test
  void testNullRow() {
    LocalRowSet rowSet = new LocalRowSet(0);

    Row item = RowSetUtils.firstItem(rowSet);

    assertTrue(item.getColumnName(0).isEmpty());
    assertThat(item.getColumnIndex("id"), equalTo(-1));
    assertThat(item.getValue(0), nullValue());
    assertThat(item.getValue("id"), nullValue());
    assertThat(item.addValue(1), nullValue());
    assertThat(item.size(), equalTo(0));
    assertThat(item.types(), empty());

    item.clear();

    assertThat(item.size(), equalTo(0));
  }

  @Test
  void testMapFirstItemInEmptyRowSet() {
    LocalRowSet emptyRowSet = new LocalRowSet(0);

    Integer id = RowSetUtils.mapFirstItem(emptyRowSet, row -> row.getInteger("id"));
    assertThat(id, nullValue());
  }

  @Test
  void testIsEmptyOfEmptyRowSet() {
    LocalRowSet emptyRowSet = new LocalRowSet(0);

    boolean isEmpty = RowSetUtils.isEmpty(emptyRowSet);
    assertTrue(isEmpty);
  }

  @Test
  void testIsEmptyOfNonEmptyRowSet() {
    LocalRowSet emptyRowSet = getTestRowSet();

    boolean isEmpty = RowSetUtils.isEmpty(emptyRowSet);
    assertFalse(isEmpty);
  }

  @Test
  void testMapFromNonNullUUID() {
    UUID uuid = UUID.randomUUID();

    String stringUUID = RowSetUtils.fromUUID(uuid);
    assertThat(stringUUID, notNullValue());
  }

  @Test
  void testMapFromNullDate() {
    OffsetDateTime offsetDateTime = RowSetUtils.fromDate(null);
    assertThat(offsetDateTime, nullValue());
  }

  @Test
  void testMapFromNonNullDate() {
    Date date = Date.from(Instant.now());

    OffsetDateTime offsetDateTime = RowSetUtils.fromDate(date);
    assertThat(offsetDateTime, notNullValue());
  }

  @Test
  void testMapFromNonNullOffsetDateTime() {
    OffsetDateTime offsetDateTime = OffsetDateTime.now();

    Date date = RowSetUtils.toDate(offsetDateTime);
    assertThat(date, notNullValue());
  }

  class Holder {
    public String a;
    public String b;
  }

  @Test
  void testToJsonObject() {
    assertThat(RowSetUtils.toJsonObject(null), nullValue());

    Holder holder = new Holder();
    holder.a = "foo";
    holder.b = "bar";
    assertThat(RowSetUtils.toJsonObject(holder), equalTo(new JsonObject().put("a", "foo").put("b", "bar")));
  }

  @Test
  void testMapFromNullUUID() {
    String stringUUID = RowSetUtils.fromUUID(null);
    assertThat(stringUUID, nullValue());
  }

  @Test
  void testMapToUUID() {
    UUID expected = UUID.randomUUID();

    UUID actual = RowSetUtils.toUUID(expected.toString());
    assertThat(actual, equalTo(expected));
  }

  private LocalRowSet getTestRowSet() {
    Row row1 = mock(Row.class);
    Row row2 = mock(Row.class);
    when(row1.getInteger("id")).thenReturn(1);
    when(row2.getInteger("id")).thenReturn(2);
    when(row1.getString("name")).thenReturn("test name1");
    when(row2.getString("name")).thenReturn("test name2");
    when(row1.getColumnName(0)).thenReturn("id");
    when(row1.getColumnName(1)).thenReturn("name");
    return new LocalRowSet(2).withRows(Arrays.asList(row1, row2)).withColumns(List.of("id", "name"));
  }
}
