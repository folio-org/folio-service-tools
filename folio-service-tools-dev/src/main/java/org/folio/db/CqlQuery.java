package org.folio.db;

import static org.folio.db.DbUtils.ALL_FIELDS;
import static org.folio.db.DbUtils.getCQLWrapper;

import io.vertx.core.Future;
import io.vertx.core.Promise;

import org.folio.cql2pgjson.exception.FieldException;
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

    Promise<Results<T>> promise = Promise.promise();
    pg.get(table, clazz, ALL_FIELDS, cql, true, promise);

    return promise.future();
  }
}
