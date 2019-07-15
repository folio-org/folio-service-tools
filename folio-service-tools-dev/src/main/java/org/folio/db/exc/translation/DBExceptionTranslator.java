package org.folio.db.exc.translation;

import java.util.function.Function;

import io.vertx.core.Future;

import org.folio.db.exc.DatabaseException;

public abstract class DBExceptionTranslator {

  public abstract boolean acceptable(Throwable exc);

  public final DatabaseException translate(Throwable exc) {
    if (!acceptable(exc)) {
      throw new IllegalArgumentException("Exception is not acceptable and cannot be translated: " + exc);
    }

    return doTranslation(exc);
  }

  protected abstract DatabaseException doTranslation(Throwable exc);

  public final <T> Function<Throwable, Future<T>> translateOrPassBy() {
    return th -> {
      // translate exception if possible, otherwise use the original one
      Throwable translated = acceptable(th) ? translate(th) : th;
      
      return Future.failedFuture(translated);
    };
  }

}
