package org.folio.db;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import static org.folio.db.DbUtils.createParams;
import static org.folio.db.DbUtils.createParamsAsJsonArray;

import java.util.Arrays;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;

public class DbUtilsTest {

  @Test(expected = NullPointerException.class)
  public void createParamsFailedWithNPEIfParamsNull() {
    createParams((Iterable<?>) null);
  }

  @Test
  public void createParamsPopulateJsonArray() {
    List<?> params = Arrays.asList("param1", 0);

    Tuple tuple = createParams(params);

    assertThat(tuple.size(), is(params.size()));
    assertThat(tuple.getString(0), is("param1"));
    assertThat(tuple.getInteger(1), is(0));
  }

  @Test
  public void createParamsWorksWithNulls() {
    List<?> params = Arrays.asList("param1", null, 0, null);

    Tuple tuple = createParams(params);

    assertThat(tuple.size(), is(params.size()));
    assertThat(tuple.getString(0), is("param1"));
    assertThat(tuple.getString(2), is(nullValue()));
    assertThat(tuple.getInteger(2), is(0));
    assertThat(tuple.getString(3), is(nullValue()));
  }

  @Test
  public void createParamsVarArgsPopulateTuple() {
    Tuple tuple = createParams("param1", 0);

    assertThat(tuple.size(), is(2));
    assertThat(tuple.getString(0), is("param1"));
    assertThat(tuple.getInteger(1), is(0));
  }


  @Test(expected = NullPointerException.class)
  public void createParamsAsJsonArrayFailedWithNPEIfParamsNull() {
    createParamsAsJsonArray((Iterable<?>) null);
  }

  @Test
  public void createParamsAsJsonArrayPopulatesArray() {
    List<?> params = Arrays.asList("param1", 0);

    JsonArray jarray = createParamsAsJsonArray(params);

    assertThat(jarray.size(), is(params.size()));
    assertThat(jarray.getString(0), is("param1"));
    assertThat(jarray.getInteger(1), is(0));
  }

  @Test
  public void createParamsAsJsonArrayWorksWithNulls() {
    List<?> params = Arrays.asList("param1", null, 0, null);

    JsonArray jarray = createParamsAsJsonArray(params);

    assertThat(jarray.size(), is(params.size()));
    assertThat(jarray.getString(0), is("param1"));
    assertThat(jarray.hasNull(1), is(true));
    assertThat(jarray.getInteger(2), is(0));
    assertThat(jarray.hasNull(3), is(true));
  }

  @Test
  public void createParamsAsJsonArrayVarArgsPopulatesArray() {
    JsonArray jarray = createParamsAsJsonArray("param1", 0);

    assertThat(jarray.size(), is(2));
    assertThat(jarray.getString(0), is("param1"));
    assertThat(jarray.getInteger(1), is(0));
  }
}
