package org.folio.db;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.folio.rest.jaxrs.model.ResultInfo;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.cql.CQLWrapper;
import org.folio.rest.persist.interfaces.Results;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class CqlQueryTest {

  @ParameterizedTest
  @ValueSource(booleans = { true, false })
  void allRecords(boolean hasResultInfo, VertxTestContext vtc) {
    var fooBarBaz = List.of("foo", "bar", "baz");
    var results = hasResultInfo ? results(fooBarBaz, 8) : results(fooBarBaz);
    var postgresClient = mock(PostgresClient.class);
    when(postgresClient.get(any(), eq(String.class), any(CQLWrapper.class), eq(false)))
      .thenReturn(results);
    var cqlQuery = new CqlQuery<String>(postgresClient, "t", String.class);
    cqlQuery.get("some cql", 0, Integer.MAX_VALUE)
    .onComplete(vtc.succeeding(result -> {
      assertThat(result.getResultInfo().getTotalRecords(), is(3));
      assertThat(result.getResultInfo().getTotalRecordsEstimated(), is(false));
      assertThat(result.getResults(), is(fooBarBaz));
      verify(postgresClient).get(any(), eq(String.class), any(CQLWrapper.class), eq(false));
      verifyNoMoreInteractions(postgresClient);
      vtc.completeNow();
    }));
  }

  @ParameterizedTest
  @CsvSource({
    "0, 1",
    "1, 0",
    "0, 2147483646",  // 0, MAX_VALUE-1
  })
  void notAllRecords(int offset, int limit, VertxTestContext vtc) {
    var fooBarBaz = List.of("foo", "bar", "baz");
    var postgresClient = mock(PostgresClient.class);
    when(postgresClient.get(any(), eq(String.class), any(CQLWrapper.class), eq(true)))
    .thenReturn(results(fooBarBaz, 7));
    var cqlQuery = new CqlQuery<String>(postgresClient, "t", String.class);
    cqlQuery.get("some cql", offset, limit)
    .onComplete(vtc.succeeding(result -> {
      assertThat(result.getResultInfo().getTotalRecords(), is(7));
      assertThat(result.getResultInfo().getTotalRecordsEstimated(), is(true));
      assertThat(result.getResults(), is(fooBarBaz));
      verify(postgresClient).get(any(), eq(String.class), any(CQLWrapper.class), eq(true));
      vtc.completeNow();
    }));
  }

  private static Future<Results<String>> results(List<String> list) {
    var results = new Results<String>();
    results.setResults(list);
    return Future.succeededFuture(results);
  }

  private static Future<Results<String>> results(List<String> list, int totalRecordsEstimated) {
    var results = new Results<String>();
    results.setResults(list);
    results.setResultInfo(new ResultInfo()
        .withTotalRecords(totalRecordsEstimated)
        .withTotalRecordsEstimated(true));
    return Future.succeededFuture(results);
  }
}
