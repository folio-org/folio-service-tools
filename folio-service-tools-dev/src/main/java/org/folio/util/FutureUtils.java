package org.folio.util;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;

import io.vertx.core.Future;
import org.apache.commons.lang3.reflect.ConstructorUtils;

public final class FutureUtils {

  private FutureUtils() {
  }

  public static  <T> Future<T> wrapExceptions(Future<T> future, Class<? extends Throwable> wrapperExcClass) {
    Future<T> result = Future.future();

    future.setHandler(ar -> {
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

    return result;
  }

  public static <T> CompletableFuture<T> mapVertxFuture(Future<T> future) {
    CompletableFuture<T> completableFuture = new CompletableFuture<>();

    future
      .map(completableFuture::complete)
      .otherwise(completableFuture::completeExceptionally);

    return completableFuture;
  }

  public static <T> Future<T> mapCompletableFuture(CompletableFuture<T> completableFuture) {
    Future<T> future = Future.future();

    completableFuture
      .thenAccept(future::complete)
      .exceptionally(cause -> {
        future.fail(cause);
        return null;
      });

    return future;
  }
}
