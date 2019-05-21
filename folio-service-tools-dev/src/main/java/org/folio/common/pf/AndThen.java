package org.folio.common.pf;

import java.util.Objects;
import java.util.function.Function;

final class AndThen<T, R, V> implements PartialFunction<T, V> {

  private PartialFunction<T, R> pf;
  private Function<? super R, ? extends V> after;


  AndThen(PartialFunction<T, R> pf, Function<? super R, ? extends V> after) {
    Objects.requireNonNull(pf, "Partial function is null");
    Objects.requireNonNull(after, "After is null");

    this.pf = pf;
    this.after = after;
  }

  @Override
  public boolean isDefinedAt(T t) {
    return pf.isDefinedAt(t);
  }

  @Override
  public V applySuccessfully(T t) {
    return after.apply(pf.apply(t));
  }

}
