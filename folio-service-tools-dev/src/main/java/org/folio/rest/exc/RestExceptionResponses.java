package org.folio.rest.exc;

import static org.folio.rest.ResponseHelper.statusWithText;

import javax.ws.rs.core.Response;

import io.vertx.core.Future;
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

  public static Response toGeneral(Throwable t) {
    Future<Response> validationFuture = Future.future();
    ValidationHelper.handleError(t, validationFuture);

    if (validationFuture.succeeded()) {
      return validationFuture.result();
    } else {
      return statusWithText(HttpStatus.SC_INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }
  }
}
