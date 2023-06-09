package org.folio.db;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.folio.db.DbUtils.createParams;
import static org.folio.db.DbUtils.createParamsAsJsonArray;

import java.util.Arrays;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.sqlclient.Tuple;
import org.junit.jupiter.api.Test;

class DbUtilsTest {

  @Test
  void createParamsFailedWithNPEIfParamsNull() {
    assertThrows(NullPointerException.class, () -> createParams((Iterable<?>) null));
  }

  @Test
  void createParamsPopulateJsonArray() {
    List<?> params = Arrays.asList("param1", 0);

    Tuple tuple = createParams(params);

    assertThat(tuple.size(), is(params.size()));
    assertThat(tuple.getString(0), is("param1"));
    assertThat(tuple.getInteger(1), is(0));
  }

  @Test
  void createParamsWorksWithNulls() {
    List<?> params = Arrays.asList("param1", null, 0, null);

    Tuple tuple = createParams(params);

    assertThat(tuple.size(), is(params.size()));
    assertThat(tuple.getString(0), is("param1"));
    assertThat(tuple.getString(1), is(nullValue()));
    assertThat(tuple.getInteger(2), is(0));
    assertThat(tuple.getString(3), is(nullValue()));
  }

  @Test
  void createParamsVarArgsPopulateTuple() {
    Tuple tuple = createParams("param1", 0);

    assertThat(tuple.size(), is(2));
    assertThat(tuple.getString(0), is("param1"));
    assertThat(tuple.getInteger(1), is(0));
  }


  @Test
  void createParamsAsJsonArrayFailedWithNPEIfParamsNull() {
    assertThrows(NullPointerException.class, () -> createParamsAsJsonArray((Iterable<?>) null));
  }

  @Test
  void createParamsAsJsonArrayPopulatesArray() {
    List<?> params = Arrays.asList("param1", 0);

    JsonArray jarray = createParamsAsJsonArray(params);

    assertThat(jarray.size(), is(params.size()));
    assertThat(jarray.getString(0), is("param1"));
    assertThat(jarray.getInteger(1), is(0));
  }

  @Test
  void createParamsAsJsonArrayWorksWithNulls() {
    List<?> params = Arrays.asList("param1", null, 0, null);

    JsonArray jarray = createParamsAsJsonArray(params);

    assertThat(jarray.size(), is(params.size()));
    assertThat(jarray.getString(0), is("param1"));
    assertThat(jarray.hasNull(1), is(true));
    assertThat(jarray.getInteger(2), is(0));
    assertThat(jarray.hasNull(3), is(true));
  }

  @Test
  void createParamsAsJsonArrayVarArgsPopulatesArray() {
    JsonArray jarray = createParamsAsJsonArray("param1", 0);

    assertThat(jarray.size(), is(2));
    assertThat(jarray.getString(0), is("param1"));
    assertThat(jarray.getInteger(1), is(0));
  }
}
