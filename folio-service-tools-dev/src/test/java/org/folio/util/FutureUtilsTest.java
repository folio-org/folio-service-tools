package org.folio.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.junit.Test;

public class FutureUtilsTest {

  private static final String RESULT_VALUE = "Completed";
  private static final IllegalArgumentException EXCEPTION_VALUE = new IllegalArgumentException();

  @Test
  public void shouldCompleteVertxFutureWhenCompletableFutureSucceeds() {
    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
    Future<Object> vertxFuture = FutureUtils.mapCompletableFuture(completableFuture);
    completableFuture.complete(RESULT_VALUE);
    assertTrue(vertxFuture.succeeded());
    vertxFuture.setHandler(result ->
      assertEquals(RESULT_VALUE, result.result()));
  }

  @Test
  public void shouldFailVertxFutureWhenCompletableFutureFails() {
    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
    Future<Object> vertxFuture = FutureUtils.mapCompletableFuture(completableFuture);
    completableFuture.completeExceptionally(EXCEPTION_VALUE);
    assertTrue(vertxFuture.failed());
    vertxFuture.setHandler(result -> {
      assertTrue(result.cause() instanceof CompletionException);
      assertEquals(EXCEPTION_VALUE, result.cause().getCause());
    });
  }

  @Test
  public void shouldNotCompleteVertxFutureWhenCompletableFutureNotCompleted() {
    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
    Future<Object> vertxFuture = FutureUtils.mapCompletableFuture(completableFuture);
    assertFalse(vertxFuture.isComplete());
  }

  @Test
  public void shouldCompleteCompletableFutureWhenVertxFutureSucceeds() {
    Future<Object> vertxFuture = Promise.promise().future();
    CompletableFuture<Object> completableFuture = FutureUtils.mapVertxFuture(vertxFuture);
    completableFuture.complete(RESULT_VALUE);
    assertEquals(RESULT_VALUE, completableFuture.getNow(null));
  }

  @Test
  public void shouldFailCompletableFutureWhenVertxFutureFails() {
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
  public void shouldNotCompleteCompletableFutureWhenVertxFutureNotCompleted() {
    Future<Object> vertxFuture = Promise.promise().future();
    CompletableFuture<Object> completableFuture = FutureUtils.mapVertxFuture(vertxFuture);
    assertFalse(completableFuture.isDone());
  }
}
