package org.folio.db;

import io.vertx.core.json.JsonArray;
import io.vertx.sqlclient.Tuple;
import java.util.Arrays;
import org.folio.cql2pgjson.CQL2PgJSON;
import org.folio.cql2pgjson.exception.FieldException;
import org.folio.rest.persist.Criteria.Limit;
import org.folio.rest.persist.Criteria.Offset;
import org.folio.rest.persist.cql.CQLWrapper;

public final class DbUtils {

  private DbUtils() {
  }

  public static CQLWrapper getCQLWrapper(String tableName, String query, int limit, int offset) throws FieldException {
    return getCQLWrapper(tableName, query)
      .setLimit(new Limit(limit))
      .setOffset(new Offset(offset));
  }

  public static CQLWrapper getCQLWrapper(String tableName, String query) throws FieldException {
    CQL2PgJSON cql2pgJson = new CQL2PgJSON(tableName + ".jsonb");
    return new CQLWrapper(cql2pgJson, query);
  }

  public static Tuple createParams(Iterable<?> queryParameters) {
    Tuple parameters = Tuple.tuple();
    queryParameters.forEach(parameters::addValue);
    return parameters;
  }

  public static Tuple createParams(Object... queryParameters) {
    return createParams(Arrays.asList(queryParameters));
  }

  public static JsonArray createParamsAsJsonArray(Iterable<?> queryParameters) {
    JsonArray parameters = new JsonArray();

    for (Object p : queryParameters) {
      if (p != null) {
        parameters.add(p);
      } else {
        parameters.addNull();
      }
    }

    return parameters;
  }

  public static JsonArray createParamsAsJsonArray(Object... queryParameters) {
    return createParamsAsJsonArray(Arrays.asList(queryParameters));
  }

}
