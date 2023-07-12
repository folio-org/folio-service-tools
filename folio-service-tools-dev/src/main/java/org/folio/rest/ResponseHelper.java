package org.folio.rest;

import java.util.function.Function;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;

import org.folio.common.pf.PartialFunction;

public class ResponseHelper {

  private ResponseHelper() {
  }

  public static Response statusWithText(int status, String msg) {
    return Response.status(status)
      .type(MediaType.TEXT_PLAIN)
      .entity(StringUtils.defaultString(msg))
      .build();
  }

  public static Response statusWithJson(int status, Object json) {
    return Response.status(status)
      .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON)
      .entity(json)
      .build();
  }

  public static <T> void respond(Future<T> result, Function<T, Response> mapper,
                                 Handler<AsyncResult<Response>> asyncResultHandler,
                                 PartialFunction<Throwable, Response> exceptionHandler) {
    result.map(mapper)
      .otherwise(exceptionHandler)
      .onComplete(asyncResultHandler);
  }
}
