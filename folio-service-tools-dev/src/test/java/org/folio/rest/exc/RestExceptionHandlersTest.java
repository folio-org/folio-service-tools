package org.folio.rest.exc;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CompletionException;
import java.util.function.Function;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import com.github.mauricio.async.db.postgresql.exceptions.GenericDatabaseException;
import com.github.mauricio.async.db.postgresql.messages.backend.ErrorMessage;
import com.github.mauricio.async.db.postgresql.messages.backend.InformationMessage;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.z3950.zing.cql.cql2pgjson.CQL2PgJSONException;
import scala.collection.immutable.Map;

import org.folio.common.pf.PartialFunction;
import org.folio.rest.persist.cql.CQLQueryValidationException;

public class RestExceptionHandlersTest {

  @Test
  public void badReqHandlerCreates400ResponseForBadReqException() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.badRequestHandler();

    Response response = handler.apply(new BadRequestException("BAD"));

    verifyResponse(response, HttpStatus.SC_BAD_REQUEST, "BAD");
  }

  @Test
  public void badReqHandlerCreates400ResponseForCQLQueryValidationException() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.badRequestHandler();

    Exception cause = new Exception("INVALID");
    Response response = handler.apply(new CQLQueryValidationException(cause));

    verifyResponse(response, HttpStatus.SC_BAD_REQUEST, cause.toString());
  }

  @Test
  public void badReqHandlerCreates400ResponseForCQL2PgJSONException() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.badRequestHandler();

    Response response = handler.apply(new CQL2PgJSONException("EXC"));

    verifyResponse(response, HttpStatus.SC_BAD_REQUEST, "EXC");
  }

  @Test
  public void badReqHandlerCreates400ResponseForInvalidUUID() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.badRequestHandler();

    Response response = handler.apply(new GenericDatabaseException(
      new ErrorMessage(new Map.Map1<>(InformationMessage.Message(), "invalid input syntax for type uuid"))));

    assertThat(response, notNullValue());
    assertThat(response.getStatus(), is(HttpStatus.SC_BAD_REQUEST));
    assertThat(response.getMediaType(), is(TEXT_PLAIN_TYPE));
    assertThat(response.getEntity().toString(), containsString("invalid input syntax for type uuid"));
  }

  @Test
  public void notFoundHandlerCreates404ResponseForNotFoundException() {
    PartialFunction<Throwable, Response> handler = RestExceptionHandlers.notFoundHandler();

    Response response = handler.apply(new NotFoundException("NOTFOUND"));

    verifyResponse(response, HttpStatus.SC_NOT_FOUND, "NOTFOUND");
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
