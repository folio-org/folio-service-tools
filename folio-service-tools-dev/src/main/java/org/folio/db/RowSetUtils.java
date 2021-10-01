package org.folio.db;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.apache.commons.lang3.StringUtils;

public final class RowSetUtils {

  private RowSetUtils() {
  }

  public static Stream<Row> streamOf(RowSet<Row> rowSet) {
    Objects.requireNonNull(rowSet);

    Spliterator<Row> spliterator = Spliterators.spliterator(rowSet.iterator(), rowSet.rowCount(), Spliterator.ORDERED);
    return StreamSupport.stream(spliterator, false);
  }

  public static <T> List<T> mapItems(RowSet<Row> rowSet, Function<Row, T> mapper) {
    Objects.requireNonNull(rowSet);

    return streamOf(rowSet)
      .map(mapper)
      .collect(Collectors.toList());
  }

  public static Row firstItem(RowSet<Row> rowSet) {
    Objects.requireNonNull(rowSet);
    try {
      return rowSet.iterator().next();
    } catch (NoSuchElementException e) {
      return NullRow.INSTANCE;
    }
  }

  public static <T> T mapFirstItem(RowSet<Row> rowSet, Function<Row, T> mapper) {
    Objects.requireNonNull(mapper);

    Row firstItem = firstItem(rowSet);
    return firstItem == NullRow.INSTANCE ? null : mapper.apply(firstItem);
  }

  public static boolean isEmpty(RowSet<Row> rowSet) {
    return rowSet.rowCount() == 0;
  }

  public static String fromUUID(UUID uuid) {
    return uuid == null ? null : uuid.toString();
  }

  public static UUID toUUID(String uuid) {
    return StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid);
  }

  public static Date toDate(OffsetDateTime date) {
    return date == null ? null : Date.from(date.toInstant());
  }

  public static OffsetDateTime fromDate(Date date) {
    return date == null ? null : OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
  }

  public static <T> JsonObject toJsonObject(T object) {
    return JsonObject.mapFrom(object);
  }

  private static class NullRow implements Row {

    static final NullRow INSTANCE = new NullRow();

    private NullRow() {}

    @Override
    public String getColumnName(int pos) {
      return StringUtils.EMPTY;
    }

    @Override
    public int getColumnIndex(String name) {
      return -1;
    }

    @Override
    public Object getValue(String column) {
      return null;
    }

    @Override
    public Object getValue(int pos) {
      return null;
    }

    @Override
    public Tuple addValue(Object value) {
      return null;
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public void clear() {
      // nothing to do
    }

    @Override
    public List<Class<?>> types() {
      return Collections.emptyList();
    }
  }
}
