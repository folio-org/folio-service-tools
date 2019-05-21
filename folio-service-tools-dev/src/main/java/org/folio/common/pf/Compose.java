package org.folio.common.pf;

import java.util.Objects;
import java.util.function.Function;

public class Compose<V, T, R> implements PartialFunction<V, R> {

  private PartialFunction<T, R> pf;
  private Function<? super V, ? extends T> before;


  Compose(PartialFunction<T, R> pf, Function<? super V, ? extends T> before) {
    Objects.requireNonNull(pf, "Partial function is null");
    Objects.requireNonNull(before, "Before is null");

    this.pf = pf;
    this.before = before;
  }

  @Override
  public boolean isDefinedAt(V v) {
    T b = before.apply(v); // have to apply 'before' function to apply its result to isDefinedAt() of partial function
    return pf.isDefinedAt(b);
  }

  @Override
  public R applySuccessfully(V v) {
    return pf.apply(before.apply(v));
  }

}
