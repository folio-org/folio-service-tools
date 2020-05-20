package org.folio.db;


import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
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

    List<Integer> ids = RowSetUtils.map(rowSet, row -> row.getInteger("id"));
    assertThat(ids, hasSize(2));
    assertThat(ids, hasItems(1, 2));
  }

  private LocalRowSet getTestRowSet() {
    RowImpl row1 = new RowImpl(new RowDesc(Arrays.asList("id", "name")));
    row1.addInteger(1).addString("test name1");
    RowImpl row2 = new RowImpl(new RowDesc(Arrays.asList("id", "name")));
    row2.addInteger(2).addString("test name2");
    return new LocalRowSet(2).withRows(Arrays.asList(row1, row2));
  }
}
