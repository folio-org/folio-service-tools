package org.folio.rest.exc;

import static org.folio.rest.ResponseHelper.statusWithText;

import jakarta.ws.rs.core.Response;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.http.HttpStatus;
import java.util.Optional;

import org.folio.rest.tools.messages.MessageConsts;
import org.folio.rest.tools.messages.Messages;
import org.folio.rest.tools.utils.ValidationHelper;

public class RestExceptionResponses {

  private static final String CONTENT_TYPE = "Content-Type";

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
    Promise<javax.ws.rs.core.Response> promise = Promise.promise();

    try {
      ValidationHelper.handleError(t, promise);

      Future<javax.ws.rs.core.Response> future = promise.future();
      if (future.succeeded()) {
        return asJakartaResponse(future.result());
      } else {
        return statusWithText(HttpStatus.SC_INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
      }
    } catch (LinkageError ex) {
      return plainInternalServerErrorResponse();
    }
  }

  private static Response plainInternalServerErrorResponse() {
    Response.ResponseBuilder responseBuilder =
      Response.status(Response.Status.INTERNAL_SERVER_ERROR).header(CONTENT_TYPE, "text/plain");
    String entity = Messages.getInstance().getMessage("en", MessageConsts.InternalServerError);
    responseBuilder.entity(entity);
    return responseBuilder.build();
  }

  private static Response asJakartaResponse(javax.ws.rs.core.Response javaxResponse) {
    Response.ResponseBuilder builder = Response.status(javaxResponse.getStatus());
    Optional.ofNullable(javaxResponse.getHeaderString(CONTENT_TYPE))
      .ifPresent(contentTypeHeader -> builder.header(CONTENT_TYPE, contentTypeHeader));
    builder.entity(javaxResponse.getEntity());
    return builder.build();
  }
}
