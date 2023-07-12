package org.folio.rest.exc;

import static org.folio.rest.ResponseHelper.statusWithText;

import javax.ws.rs.core.Response;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.http.HttpStatus;
import org.folio.rest.tools.utils.ValidationHelper;

public class RestExceptionResponses {

  private RestExceptionResponses() {
  }

  public static Response toBadRequest(Throwable t) {
    return statusWithText(HttpStatus.SC_BAD_REQUEST, t.getMessage());
  }

  public static Response toNotFound(Throwable t) {
    return statusWithText(HttpStatus.SC_NOT_FOUND, t.getMessage());
  }

  @SuppressWarnings("squid:S1172")
  public static Response toUnauthorized(Throwable t) {
    return statusWithText(HttpStatus.SC_UNAUTHORIZED, "Unauthorized");
  }

  public static Response toUnprocessable(Throwable t) {
    return statusWithText(HttpStatus.SC_UNPROCESSABLE_ENTITY, t.getMessage());
  }

  public static Response toGeneral(Throwable t) {
    Promise<Response> promise = Promise.promise();

    ValidationHelper.handleError(t, promise);

    Future<Response> future = promise.future();
    if (future.succeeded()) {
      return future.result();
    } else {
      return statusWithText(HttpStatus.SC_INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }
  }
}
