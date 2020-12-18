package org.folio.rest.exc;

import static org.folio.common.pf.PartialFunctions.pf;
import static org.folio.rest.exc.ExceptionPredicates.instanceOf;
import static org.folio.rest.exc.ExceptionPredicates.invalidUUID;

import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import io.vertx.pgclient.PgException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.folio.common.pf.PartialFunction;
import org.folio.common.pf.PartialFunctions;
import org.folio.cql2pgjson.exception.CQL2PgJSONException;
import org.folio.db.exc.AuthorizationException;
import org.folio.db.exc.ConstraintViolationException;
import org.folio.db.exc.DataException;
import org.folio.db.exc.InvalidUUIDException;
import org.folio.rest.persist.cql.CQLQueryValidationException;

public class RestExceptionHandlers {

  private static final Logger LOGGER = LogManager.getLogger(RestExceptionHandlers.class);

  private RestExceptionHandlers() {
  }

  @SuppressWarnings("squid:CommentedOutCodeLine")
  public static PartialFunction<Throwable, Response> baseBadRequestHandler() {
    // predicate can be written also as:
    //    t -> t instanceof BadRequestException || t instanceof CQL2PgJSONException
    //
    // the below is to show how predicates that potentially have complex logic can be combined
    return badRequestHandler(instanceOf(BadRequestException.class)
      .or(instanceOf(InvalidUUIDException.class)) // if DB exception translation applied OR ...
      .or(instanceOf(PgException.class).and(invalidUUID())) // ... if not applied
      .or(instanceOf(CQLQueryValidationException.class))
      .or(instanceOf(CQL2PgJSONException.class)));
  }

  public static PartialFunction<Throwable, Response> badRequestHandler(Predicate<Throwable> predicate) {
    return pf(predicate, RestExceptionResponses::toBadRequest);
  }

  public static PartialFunction<Throwable, Response> baseNotFoundHandler() {
    return notFoundHandler(NotFoundException.class::isInstance);
  }

  public static PartialFunction<Throwable, Response> notFoundHandler(Predicate<Throwable> predicate) {
    return pf(predicate, RestExceptionResponses::toNotFound);
  }

  public static PartialFunction<Throwable, Response> baseUnauthorizedHandler() {
    return unauthorizedHandler(instanceOf(NotAuthorizedException.class)
      .or(instanceOf(AuthorizationException.class)));
  }

  public static PartialFunction<Throwable, Response> unauthorizedHandler(Predicate<Throwable> predicate) {
    return pf(predicate, RestExceptionResponses::toUnauthorized);
  }

  public static PartialFunction<Throwable, Response> baseUnprocessableHandler() {
    return unprocessableHandler(instanceOf(ConstraintViolationException.class)
      .or(instanceOf(DataException.class)));
  }

  public static PartialFunction<Throwable, Response> unprocessableHandler(Predicate<Throwable> predicate) {
    return pf(predicate, RestExceptionResponses::toUnprocessable);
  }

  public static PartialFunction<Throwable, Response> generalHandler() {
    return pf(t -> true, RestExceptionResponses::toGeneral);
  }

  public static PartialFunction<Throwable, Response> logged(PartialFunction<Throwable, Response> pf) {
    return PartialFunctions.logged(pf, t -> LOGGER.error("Execution failed with: " + t.getMessage(), t));
  }

  /**
   * In case of Completable Futures the errors are usually wrapped inside of CompletionException,
   * opposite to Vert.x Futures where the original exceptions simply stored inside the Future as is.
   * <p>
   * So to get to the cause this function examines the type of exception and extract the cause from CompletionException
   * if it's not null. Otherwise the given exception returned as cause.
   * <p>
   * This function is supposed to be used before any handlers (via compose) to normalize the exception.
   *
   * @return the cause of CompletionException
   */
  public static Function<Throwable, Throwable> completionCause() {
    return t -> (t instanceof CompletionException) && t.getCause() != null
      ? t.getCause()
      : t;
  }

}


