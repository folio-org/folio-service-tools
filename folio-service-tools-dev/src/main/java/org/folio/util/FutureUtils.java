package org.folio.util;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.lang3.reflect.ConstructorUtils;

public final class FutureUtils {

  private FutureUtils() {
  }

  public static <T> Future<T> wrapExceptions(Future<T> future, Class<? extends Throwable> wrapperExcClass) {
    Promise<T> result = Promise.promise();

    future.onComplete(ar -> {
      if (ar.succeeded()) {
        result.complete(ar.result());
      } else {

        Throwable exc;

        if (wrapperExcClass.isInstance(ar.cause())) {
          exc = ar.cause();
        } else {
          try {
            exc = ConstructorUtils.invokeConstructor(wrapperExcClass, ar.cause());
          } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                   | InvocationTargetException e) {
            exc = e;
          }
        }

        result.fail(exc);
      }
    });

    return result.future();
  }

  public static <T> CompletableFuture<T> mapVertxFuture(Future<T> future) {
    CompletableFuture<T> completableFuture = new CompletableFuture<>();

    future
      .map(completableFuture::complete)
      .otherwise(completableFuture::completeExceptionally);

    return completableFuture;
  }

  public static <T> Future<T> mapCompletableFuture(CompletableFuture<T> completableFuture) {
    Promise<T> promise = Promise.promise();

    completableFuture
      .thenAccept(promise::complete)
      .exceptionally(cause -> {
        promise.fail(cause);
        return null;
      });

    return promise.future();
  }

  public static <T> CompletableFuture<T> failedFuture(Throwable ex) {
    CompletableFuture<T> f = new CompletableFuture<>();

    f.completeExceptionally(ex);

    return f;
  }

  public static <T, U> CompletableFuture<T> mapResult(Future<U> future, Function<U, T> mapper) {
    CompletableFuture<T> result = new CompletableFuture<>();

    future.map(mapper)
      .map(result::complete)
      .otherwise(result::completeExceptionally);

    return result;
  }

  /**
   * Returns a new CompletableFuture that is completed when all of the given futures complete,
   * returned CompletableFuture contains list of values from futures that have succeeded,
   * If future completes exceptionally then the exception is passed to provided exceptionHandler
   */
  public static <T> CompletableFuture<List<T>> allOfSucceeded(Collection<CompletableFuture<T>> futures,
                                                              Consumer<Throwable> exceptionHandler) {
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
      .handle((o, e) ->
        futures.stream()
          .map(future -> future.whenComplete((result, throwable) -> {
            if (throwable != null) {
              exceptionHandler.accept(throwable);
            }
          }))
          .filter(future -> !future.isCompletedExceptionally())
          .map(CompletableFuture::join)
          .toList()
      );
  }
}
