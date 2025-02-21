package org.folio.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.junit.jupiter.api.Test;

class FutureUtilsTest {

  private static final String RESULT_VALUE = "Completed";
  private static final IllegalArgumentException EXCEPTION_VALUE = new IllegalArgumentException();

  @Test
  void shouldCompleteVertxFutureWhenCompletableFutureSucceeds() {
    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
    Future<Object> vertxFuture = FutureUtils.mapCompletableFuture(completableFuture);
    completableFuture.complete(RESULT_VALUE);
    assertTrue(vertxFuture.succeeded());
    vertxFuture.onComplete(result ->
      assertEquals(RESULT_VALUE, result.result()));
  }

  @Test
  void shouldFailVertxFutureWhenCompletableFutureFails() {
    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
    Future<Object> vertxFuture = FutureUtils.mapCompletableFuture(completableFuture);
    completableFuture.completeExceptionally(EXCEPTION_VALUE);
    assertTrue(vertxFuture.failed());
    vertxFuture.onComplete(result -> {
      assertInstanceOf(CompletionException.class, result.cause());
      assertEquals(EXCEPTION_VALUE, result.cause().getCause());
    });
  }

  @Test
  void shouldNotCompleteVertxFutureWhenCompletableFutureNotCompleted() {
    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
    Future<Object> vertxFuture = FutureUtils.mapCompletableFuture(completableFuture);
    assertFalse(vertxFuture.isComplete());
  }

  @Test
  void shouldCompleteCompletableFutureWhenVertxFutureSucceeds() {
    Future<Object> vertxFuture = Promise.promise().future();
    CompletableFuture<Object> completableFuture = FutureUtils.mapVertxFuture(vertxFuture);
    completableFuture.complete(RESULT_VALUE);
    assertEquals(RESULT_VALUE, completableFuture.getNow(null));
  }

  @Test
  void shouldFailCompletableFutureWhenVertxFutureFails() {
    Future<Object> vertxFuture = Promise.promise().future();
    CompletableFuture<Object> completableFuture = FutureUtils.mapVertxFuture(vertxFuture);
    completableFuture.completeExceptionally(EXCEPTION_VALUE);
    assertTrue(completableFuture.isCompletedExceptionally());
    completableFuture.exceptionally(ex -> {
      assertEquals(EXCEPTION_VALUE, ex.getCause());
      return null;
    });
  }

  @Test
  void shouldNotCompleteCompletableFutureWhenVertxFutureNotCompleted() {
    Future<Object> vertxFuture = Promise.promise().future();
    CompletableFuture<Object> completableFuture = FutureUtils.mapVertxFuture(vertxFuture);
    assertFalse(completableFuture.isDone());
  }
}
