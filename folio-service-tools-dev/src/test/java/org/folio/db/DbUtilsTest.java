package org.folio.db;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import static org.folio.db.DbUtils.createParams;

import java.util.Arrays;
import java.util.List;

import io.vertx.core.json.JsonArray;
import org.junit.Test;

public class DbUtilsTest {

  @Test(expected = NullPointerException.class)
  public void createParamsFailedWithNPEIfParamsNull() {
    createParams(null);
  }

  @Test
  public void createParamsPopulateJsonArray() {
    List<?> params = Arrays.asList("param1", 0);

    JsonArray jarray = createParams(params);

    assertThat(jarray.size(), is(params.size()));
    assertThat(jarray.getString(0), is("param1"));
    assertThat(jarray.getInteger(1), is(0));
  }

  @Test
  public void createParamsWorksWithNulls() {
    List<?> params = Arrays.asList("param1", null, 0, null);

    JsonArray jarray = createParams(params);

    assertThat(jarray.size(), is(params.size()));
    assertThat(jarray.getString(0), is("param1"));
    assertThat(jarray.hasNull(1), is(true));
    assertThat(jarray.getInteger(2), is(0));
    assertThat(jarray.hasNull(3), is(true));
  }
}
