package org.folio.rest.exc;

import static org.folio.common.pf.PartialFunctions.pf;

import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.github.mauricio.async.db.postgresql.exceptions.GenericDatabaseException;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import org.folio.common.pf.PartialFunction;
import org.folio.common.pf.PartialFunctions;
import org.folio.cql2pgjson.exception.CQL2PgJSONException;
import org.folio.rest.persist.cql.CQLQueryValidationException;
import org.folio.rest.tools.utils.ValidationHelper;

public class RestExceptionHandlers {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandlers.class);

  private RestExceptionHandlers() {
  }

  @SuppressWarnings("squid:CommentedOutCodeLine")
  public static PartialFunction<Throwable, Response> badRequestHandler() {
    // predicate can be written also as:
    //    t -> t instanceof BadRequestException || t instanceof CQL2PgJSONException
    //
    // the below is to show how predicates that potentially have complex logic can be combined
    return pf(instanceOf(BadRequestException.class)
                .or(instanceOf(GenericDatabaseException.class).and(invalidUUID()))
                .or(instanceOf(CQLQueryValidationException.class))
                .or(instanceOf(CQL2PgJSONException.class)),
              RestExceptionHandlers::toBadRequest);
  }

  public static PartialFunction<Throwable, Response> notFoundHandler() {
    return pf(NotFoundException.class::isInstance, RestExceptionHandlers::toNotFound);
  }

  public static PartialFunction<Throwable, Response> generalHandler() {
    return pf(t -> true, RestExceptionHandlers::toGeneral);
  }

  public static PartialFunction<Throwable, Response> logged(PartialFunction<Throwable, Response> pf) {
    return PartialFunctions.logged(pf, t -> LOGGER.error("Execution failed with: " + t.getMessage(), t));
  }

  /**
   * In case of Completable Futures the errors are usually wrapped inside of CompletionException,
   * opposite to Vert.x Futures where the original exceptions simply stored inside the Future as is.
   *
   * So to get to the cause this function examines the type of exception and extract the cause from CompletionException
   * if it's not null. Otherwise the given exception returned as cause.
   *
   * This function is supposed to be used before any handlers (via compose) to normalize the exception.
   *
   * @return the cause of CompletionException
   */
  public static Function<Throwable, Throwable> completionCause() {
    return t -> (t instanceof CompletionException) && t.getCause() != null
              ? t.getCause()
              : t;
  }

  private static Response status(int status, String msg) {
    return Response.status(status)
      .type(MediaType.TEXT_PLAIN)
      .entity(StringUtils.defaultString(msg))
      .build();
  }

  private static Response toBadRequest(Throwable t) {
    return status(HttpStatus.SC_BAD_REQUEST, t.getMessage());
  }

  private static Response toNotFound(Throwable t) {
    return status(HttpStatus.SC_NOT_FOUND, t.getMessage());
  }

  private static Response toGeneral(Throwable t) {
    Future<Response> validationFuture = Future.future();
    ValidationHelper.handleError(t, validationFuture);

    if (validationFuture.succeeded()) {
      return validationFuture.result();
    } else {
      return status(HttpStatus.SC_INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }
  }

  private static Predicate<Throwable> instanceOf(Class<? extends Throwable> cl) {
    return new InstanceOfPredicate<>(cl);
  }

  private static Predicate<Throwable> invalidUUID() {
    return t -> ValidationHelper.isInvalidUUID(t.getMessage());
  }

  private static class InstanceOfPredicate<T, E> implements Predicate<E> {

    private Class<T> excClass;

    InstanceOfPredicate(Class<T> excClass) {
      this.excClass = excClass;
    }

    @Override
    public boolean test(E e) {
      return excClass.isInstance(e);
    }
  }

}


