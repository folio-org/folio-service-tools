package org.folio.common.pf;

import java.util.function.Function;

public class Compose<V, T, R> implements PartialFunction<V, R> {

  public Compose(PartialFunction<T, R> pf, Function<? super V, ? extends T> before) {
  }

  @Override
  public boolean isDefinedAt(V v) {
    // TODO (Dima Tkachenko): create implementation
    return false;
  }

  @Override
  public R applySuccessfully(V v) {
    // TODO (Dima Tkachenko): create implementation
    return null;
  }

}
