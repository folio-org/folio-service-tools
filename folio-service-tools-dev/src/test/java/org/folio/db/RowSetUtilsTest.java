package org.folio.db;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.pgclient.impl.RowImpl;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.RowDesc;
import org.junit.Test;

import org.folio.rest.persist.helpers.LocalRowSet;

public class RowSetUtilsTest {

  @Test
  public void testStreamOf() {
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
  public void testMapRowSet() {
    LocalRowSet rowSet = getTestRowSet();

    List<Integer> ids = RowSetUtils.mapItems(rowSet, row -> row.getInteger("id"));
    assertThat(ids, hasSize(2));
    assertThat(ids, hasItems(1, 2));
  }

  @Test
  public void testMapFirstItemInRowSet() {
    LocalRowSet rowSet = getTestRowSet();

    Integer id = RowSetUtils.mapFirstItem(rowSet, row -> row.getInteger("id"));
    assertThat(id, equalTo(1));
  }

  @Test
  public void testNullRow() {
    LocalRowSet rowSet = new LocalRowSet(0);

    Row item = RowSetUtils.firstItem(rowSet);

    assertTrue(item.getColumnName(0).isEmpty());
    assertThat(item.getColumnIndex("id"), equalTo(-1));
    assertThat(item.getValue(0), nullValue());
    assertThat(item.getValue("id"), nullValue());
    assertThat(item.addValue(1), nullValue());
    assertThat(item.size(), equalTo(0));
  }

  @Test
  public void testMapFirstItemInEmptyRowSet() {
    LocalRowSet emptyRowSet = new LocalRowSet(0);

    Integer id = RowSetUtils.mapFirstItem(emptyRowSet, row -> row.getInteger("id"));
    assertThat(id, nullValue());
  }

  @Test
  public void testIsEmptyOfEmptyRowSet() {
    LocalRowSet emptyRowSet = new LocalRowSet(0);

    boolean isEmpty = RowSetUtils.isEmpty(emptyRowSet);
    assertTrue(isEmpty);
  }

  @Test
  public void testIsEmptyOfNonEmptyRowSet() {
    LocalRowSet emptyRowSet = getTestRowSet();

    boolean isEmpty = RowSetUtils.isEmpty(emptyRowSet);
    assertFalse(isEmpty);
  }

  @Test
  public void testMapFromNonNullUUID() {
    UUID uuid = UUID.randomUUID();

    String stringUUID = RowSetUtils.fromUUID(uuid);
    assertThat(stringUUID, notNullValue());
  }

  @Test
  public void testMapFromNullDate() {
    OffsetDateTime offsetDateTime = RowSetUtils.fromDate(null);
    assertThat(offsetDateTime, nullValue());
  }

  @Test
  public void testMapFromNonNullDate() {
    Date date = Date.from(Instant.now());

    OffsetDateTime offsetDateTime = RowSetUtils.fromDate(date);
    assertThat(offsetDateTime, notNullValue());
  }

  @Test
  public void testMapFromNonNullOffsetDateTime() {
    OffsetDateTime offsetDateTime = OffsetDateTime.now();

    Date date = RowSetUtils.toDate(offsetDateTime);
    assertThat(date, notNullValue());
  }

  @Test
  public void testMapFromNullUUID() {
    String stringUUID = RowSetUtils.fromUUID(null);
    assertThat(stringUUID, nullValue());
  }

  @Test
  public void testMapToUUID() {
    UUID expected = UUID.randomUUID();

    UUID actual = RowSetUtils.toUUID(expected.toString());
    assertThat(actual, equalTo(expected));
  }

  private LocalRowSet getTestRowSet() {
    RowImpl row1 = new RowImpl(new RowDesc(Arrays.asList("id", "name")));
    row1.addInteger(1).addString("test name1");
    RowImpl row2 = new RowImpl(new RowDesc(Arrays.asList("id", "name")));
    row2.addInteger(2).addString("test name2");
    return new LocalRowSet(2).withRows(Arrays.asList(row1, row2));
  }
}
