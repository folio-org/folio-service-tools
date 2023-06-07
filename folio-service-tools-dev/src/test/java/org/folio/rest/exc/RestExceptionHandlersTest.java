package org.folio.rest.exc;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

import io.vertx.core.Promise;
import io.vertx.core.Future;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.folio.rest.tools.utils.ValidationHelper;
import org.junit.jupiter.api.Test;

import org.folio.common.pf.PartialFunction;
import org.folio.cql2pgjson.exception.CQL2PgJSONException;
import org.folio.db.exc.Constraint;
import org.folio.db.exc.ConstraintViolationException;
import org.folio.db.exc.DataException;
import org.folio.db.exc.InvalidUUIDException;
import org.folio.db.exc.translation.postgresql.InformationMessageConstants;
import org.folio.rest.persist.PgExceptionUtil;
import org.folio.rest.persist.cql.CQLQueryValidationException;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

class RestExceptionHandlersTest {

  private static void verifyResponse(Response response, int expectedStatus, String expectedMsg) {
    assertThat(response, notNullValue());
    assertThat(response.getStatus(), is(expectedStatus));
    assertThat(response.getMediaType(), is(TEXT_PLAIN_TYPE));
    assertThat(response.getEntity(), is(expectedMsg));
  }

  @Test
  void badReqHandlerCreates400ResponseForBadReqException() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.baseBadRequestHandler();

    Response response = handler.apply(new BadRequestException("BAD"));

    verifyResponse(response, HttpStatus.SC_BAD_REQUEST, "BAD");
  }

  @Test
  void badReqHandlerCreates400ResponseForCQLQueryValidationException() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.baseBadRequestHandler();

    Exception cause = new Exception("INVALID");
    Response response = handler.apply(new CQLQueryValidationException(cause));

    verifyResponse(response, HttpStatus.SC_BAD_REQUEST, cause.toString());
  }

  @Test
  void badReqHandlerCreates400ResponseForInvalidUUID() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.baseBadRequestHandler();

    Response response = handler.apply(PgExceptionUtil.createPgExceptionFromMap(
      Collections.singletonMap(InformationMessageConstants.MESSAGE, "invalid input syntax for type uuid")));

    assertThat(response, notNullValue());
    assertThat(response.getStatus(), is(HttpStatus.SC_BAD_REQUEST));
    assertThat(response.getMediaType(), is(TEXT_PLAIN_TYPE));
    assertThat(response.getEntity().toString(), containsString("invalid input syntax for type uuid"));
  }

  @Test
  void badReqHandlerCreates400ResponseForCQL2PgJSONException() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.baseBadRequestHandler();

    Response response = handler.apply(new CQL2PgJSONException("EXC"));

    verifyResponse(response, HttpStatus.SC_BAD_REQUEST, "EXC");
  }

  @Test
  void badReqHandlerCreates400ResponseForInvalidUUIDExc() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.baseBadRequestHandler();

    Response response = handler.apply(new InvalidUUIDException("UUID", "22P02", "11111111"));

    verifyResponse(response, HttpStatus.SC_BAD_REQUEST, "UUID");
  }

  @Test
  void notFoundHandlerCreates404ResponseForNotFoundException() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.baseNotFoundHandler();

    Response response = handler.apply(new NotFoundException("NOTFOUND"));

    verifyResponse(response, HttpStatus.SC_NOT_FOUND, "NOTFOUND");
  }

  @Test
  void unauthorizedHandlerCreates401ResponseForNotAuthorizedException() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.baseUnauthorizedHandler();

    Response response = handler.apply(new NotAuthorizedException("NOTAUTH"));

    verifyResponse(response, HttpStatus.SC_UNAUTHORIZED, "Unauthorized"); // message from the exception is ignored
  }

  @Test
  void unauthorizedHandlerCreates401ResponseForAuthorizationException() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.baseUnauthorizedHandler();

    Response response = handler.apply(new NotAuthorizedException("NOTAUTH"));

    verifyResponse(response, HttpStatus.SC_UNAUTHORIZED, "Unauthorized"); // message from the exception is ignored
  }

  @Test
  void unprocessableHandlerCreates422ResponseForConstraintViolationException() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.baseUnprocessableHandler();

    Response response = handler.apply(new ConstraintViolationException("PK", "23505",
      Constraint.primaryKey("pk_foo", "foo_table")));

    verifyResponse(response, HttpStatus.SC_UNPROCESSABLE_ENTITY, "PK");
  }

  @Test
  void unprocessableHandlerCreates422ResponseForDataException() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.baseUnprocessableHandler();

    Response response = handler.apply(new DataException("DATA"));

    verifyResponse(response, HttpStatus.SC_UNPROCESSABLE_ENTITY, "DATA");
  }

  @Test
  void generalHandlerCreates500ResponseWhenRunWithJavaVersionLessThan17() {
    Promise<javax.ws.rs.core.Response> promise = mock(Promise.class);
    javax.ws.rs.core.Response javaxResponse = mock(javax.ws.rs.core.Response.class);
    Future<javax.ws.rs.core.Response> future = mock(Future.class);
    when(javaxResponse.getStatus()).thenReturn(SC_INTERNAL_SERVER_ERROR);
    when(javaxResponse.getHeaderString("Content-Type")).thenReturn("text/plain");
    when(javaxResponse.getEntity()).thenReturn("Internal Error");

    try (MockedStatic<Promise> mockPromise = Mockito.mockStatic(Promise.class)) {
      try (MockedStatic<ValidationHelper> mockedValidation = Mockito.mockStatic(ValidationHelper.class)) {
        mockedValidation.when(() -> ValidationHelper.handleError(any(), any())).thenAnswer((Answer<Void>) invocation -> null);
        mockPromise.when(Promise::promise).thenReturn(promise);
        when(promise.future()).thenReturn(future);
        when(future.succeeded()).thenReturn(true);
        when(future.result()).thenReturn(javaxResponse);

        PartialFunction<Throwable, Response> handler = RestExceptionHandlers.generalHandler();
        Response response = handler.apply(new Exception("GENERAL"));

        assertThat(response, notNullValue());
        assertThat(response.getStatus(), is(SC_INTERNAL_SERVER_ERROR));
        assertThat(response.getMediaType(), is(TEXT_PLAIN_TYPE));
      }
    }
  }

  @Test
  void generalHandlerCreates500ResponseWhenRunWithJavaVersion17OrAbove() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.generalHandler();

    Response response = handler.apply(new Exception("GENERAL"));

    assertThat(response, notNullValue());
    assertThat(response.getStatus(), is(SC_INTERNAL_SERVER_ERROR));
    assertThat(response.getMediaType(), is(TEXT_PLAIN_TYPE));
  }

  @Test
  void completionCauseUnwrapsCompletionException() {
    Function<Throwable, Throwable> completionCause = RestExceptionHandlers.completionCause();

    Exception cause = new Exception();
    Throwable actual = completionCause.apply(new CompletionException(cause));

    assertThat(actual, is(cause));
  }

  @Test
  void completionCauseReturnsCompletionExceptionIfNoCause() {
    Function<Throwable, Throwable> completionCause = RestExceptionHandlers.completionCause();

    CompletionException completionException = new CompletionException(null);
    Throwable actual = completionCause.apply(completionException);

    assertThat(actual, is(completionException));
  }

  @Test
  void completionCauseReturnsExceptionIfNotCompletionException() {
    Function<Throwable, Throwable> completionCause = RestExceptionHandlers.completionCause();

    Exception exception = new Exception(new Exception("cause"));
    Throwable actual = completionCause.apply(exception);

    assertThat(actual, is(exception));
  }
}
