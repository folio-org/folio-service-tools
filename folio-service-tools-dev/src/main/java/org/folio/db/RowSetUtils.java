package org.folio.db;

import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

public final class RowSetUtils {

  private RowSetUtils() {
  }

  public static Stream<Row> streamOf(RowSet<Row> rowSet) {
    Spliterator<Row> spliterator = Spliterators.spliterator(rowSet.iterator(), rowSet.rowCount(), Spliterator.ORDERED);
    return StreamSupport.stream(spliterator, false);
  }

  public static <T> List<T> mapRowSet(RowSet<Row> rowSet, Function<Row, T> mapper) {
    return streamOf(rowSet)
      .map(mapper)
      .collect(Collectors.toList());
  }
}
