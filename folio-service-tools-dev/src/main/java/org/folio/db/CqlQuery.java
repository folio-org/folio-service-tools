package org.folio.db;

import static org.folio.db.DbUtils.getCQLWrapper;

import io.vertx.core.Future;
import org.folio.cql2pgjson.exception.FieldException;
import org.folio.rest.jaxrs.model.ResultInfo;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.cql.CQLWrapper;
import org.folio.rest.persist.interfaces.Results;

public class CqlQuery<T> {

  private final PostgresClient pg;
  private final String table;
  private final Class<T> clazz;

  public CqlQuery(PostgresClient pg, String table, Class<T> clazz) {
    this.pg = pg;
    this.table = table;
    this.clazz = clazz;
  }

  public Future<Results<T>> get(String cqlQuery, int offset, int limit) {
    CQLWrapper cql;
    try {
      cql = getCQLWrapper(table, cqlQuery, limit, offset);
    } catch (FieldException e) {
      return Future.failedFuture(e);
    }

    if (offset == 0 && limit == Integer.MAX_VALUE) {
      // if all records get loaded we can avoid the additional totalRecords query
      // and take the number from the result set
      return pg.get(table, clazz, cql, false)
          .map(results -> {
            if (results.getResultInfo() == null) {
              results.setResultInfo(new ResultInfo());
            }
            results.getResultInfo()
                .withTotalRecords(results.getResults().size())
                .withTotalRecordsEstimated(false);
            return results;
          });
    }

    return pg.get(table, clazz, cql, true);
  }
}
