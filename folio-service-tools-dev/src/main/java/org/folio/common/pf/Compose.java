package org.folio.common.pf;

import java.util.function.Function;

public class Compose<V, T, R> implements PartialFunction<V, R> {

  private PartialFunction<T, R> pf;
  private Function<? super V, ? extends T> before;


  Compose(PartialFunction<T, R> pf, Function<? super V, ? extends T> before) {
    this.pf = pf;
    this.before = before;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean isDefinedAt(V v) {
    return (!(before instanceof PartialFunction)) ||
              ((PartialFunction) before).isDefinedAt(v);
  }

  @Override
  public R applySuccessfully(V v) {
    return pf.apply(before.apply(v));
  }

}
