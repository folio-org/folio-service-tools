package org.folio.rest.exc;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import com.github.jasync.sql.db.postgresql.exceptions.GenericDatabaseException;
import com.github.jasync.sql.db.postgresql.messages.backend.ErrorMessage;
import org.apache.http.HttpStatus;
import org.folio.db.exc.translation.postgresql.InformationMessageConstants;
import org.junit.Test;

import org.folio.common.pf.PartialFunction;
import org.folio.cql2pgjson.exception.CQL2PgJSONException;
import org.folio.db.exc.Constraint;
import org.folio.db.exc.ConstraintViolationException;
import org.folio.db.exc.DataException;
import org.folio.db.exc.InvalidUUIDException;
import org.folio.rest.persist.cql.CQLQueryValidationException;

public class RestExceptionHandlersTest {

  @Test
  public void badReqHandlerCreates400ResponseForBadReqException() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.baseBadRequestHandler();

    Response response = handler.apply(new BadRequestException("BAD"));

    verifyResponse(response, HttpStatus.SC_BAD_REQUEST, "BAD");
  }

  @Test
  public void badReqHandlerCreates400ResponseForCQLQueryValidationException() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.baseBadRequestHandler();

    Exception cause = new Exception("INVALID");
    Response response = handler.apply(new CQLQueryValidationException(cause));

    verifyResponse(response, HttpStatus.SC_BAD_REQUEST, cause.toString());
  }

  @Test
  public void badReqHandlerCreates400ResponseForCQL2PgJSONException() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.baseBadRequestHandler();

    Response response = handler.apply(new CQL2PgJSONException("EXC"));

    verifyResponse(response, HttpStatus.SC_BAD_REQUEST, "EXC");
  }

  @Test
  public void badReqHandlerCreates400ResponseForInvalidUUID() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.baseBadRequestHandler();

    Response response = handler.apply(new GenericDatabaseException(
      new ErrorMessage(Collections.singletonMap(InformationMessageConstants.MESSAGE, "invalid input syntax for type uuid"))));

    assertThat(response, notNullValue());
    assertThat(response.getStatus(), is(HttpStatus.SC_BAD_REQUEST));
    assertThat(response.getMediaType(), is(TEXT_PLAIN_TYPE));
    assertThat(response.getEntity().toString(), containsString("invalid input syntax for type uuid"));
  }

  @Test
  public void badReqHandlerCreates400ResponseForInvalidUUIDExc() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.baseBadRequestHandler();

    Response response = handler.apply(new InvalidUUIDException("UUID", "22P02", "11111111"));

    verifyResponse(response, HttpStatus.SC_BAD_REQUEST, "UUID");
  }

  @Test
  public void notFoundHandlerCreates404ResponseForNotFoundException() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.baseNotFoundHandler();

    Response response = handler.apply(new NotFoundException("NOTFOUND"));

    verifyResponse(response, HttpStatus.SC_NOT_FOUND, "NOTFOUND");
  }

  @Test
  public void unauthorizedHandlerCreates401ResponseForNotAuthorizedException() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.baseUnauthorizedHandler();

    Response response = handler.apply(new NotAuthorizedException("NOTAUTH"));

    verifyResponse(response, HttpStatus.SC_UNAUTHORIZED, "Unauthorized"); // message from the exception is ignored
  }

  @Test
  public void unauthorizedHandlerCreates401ResponseForAuthorizationException() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.baseUnauthorizedHandler();

    Response response = handler.apply(new NotAuthorizedException("NOTAUTH"));

    verifyResponse(response, HttpStatus.SC_UNAUTHORIZED, "Unauthorized"); // message from the exception is ignored
  }

  @Test
  public void unprocessableHandlerCreates422ResponseForConstraintViolationException() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.baseUnprocessableHandler();

    Response response = handler.apply(new ConstraintViolationException("PK", "23505",
      Constraint.primaryKey("pk_foo", "foo_table")));

    verifyResponse(response, HttpStatus.SC_UNPROCESSABLE_ENTITY, "PK");
  }

  @Test
  public void unprocessableHandlerCreates422ResponseForDataException() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.baseUnprocessableHandler();

    Response response = handler.apply(new DataException("DATA"));

    verifyResponse(response, HttpStatus.SC_UNPROCESSABLE_ENTITY, "DATA");
  }

  @Test
  public void generalHandlerCreates500Response() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.generalHandler();

    Response response = handler.apply(new Exception("GENERAL"));

    assertThat(response, notNullValue());
    assertThat(response.getStatus(), is(HttpStatus.SC_INTERNAL_SERVER_ERROR));
    assertThat(response.getMediaType(), is(TEXT_PLAIN_TYPE));
  }

  @Test
  public void completionCauseUnwrapsCompletionException() {
    Function<Throwable, Throwable> completionCause = RestExceptionHandlers.completionCause();

    Exception cause = new Exception();
    Throwable actual = completionCause.apply(new CompletionException(cause));

    assertThat(actual, is(cause));
  }

  @Test
  public void completionCauseReturnsCompletionExceptionIfNoCause() {
    Function<Throwable, Throwable> completionCause = RestExceptionHandlers.completionCause();

    CompletionException completionException = new CompletionException(null);
    Throwable actual = completionCause.apply(completionException);

    assertThat(actual, is(completionException));
  }

  @Test
  public void completionCauseReturnsExceptionIfNotCompletionException() {
    Function<Throwable, Throwable> completionCause = RestExceptionHandlers.completionCause();

    Exception exception = new Exception(new Exception("cause"));
    Throwable actual = completionCause.apply(exception);

    assertThat(actual, is(exception));
  }

  private static void verifyResponse(Response response, int expectedStatus, String expectedMsg) {
    assertThat(response, notNullValue());
    assertThat(response.getStatus(), is(expectedStatus));
    assertThat(response.getMediaType(), is(TEXT_PLAIN_TYPE));
    assertThat(response.getEntity(), is(expectedMsg));
  }
}
