package org.folio.common.pf;

import java.util.Objects;

final class OrElse<T, R> implements PartialFunction<T, R> {

  private PartialFunction<T, R> f1;
  private PartialFunction<T, R> f2;


  OrElse(PartialFunction<T, R> f1, PartialFunction<T, R> f2) {
    Objects.requireNonNull(f1, "Partial function f1 is null");
    Objects.requireNonNull(f2, "Partial function f2 is null");

    this.f1 = f1;
    this.f2 = f2;
  }

  @Override
  public boolean isDefinedAt(T t) {
    return f1.isDefinedAt(t) || f2.isDefinedAt(t);
  }

  @Override
  public R applySuccessfully(T t) {
    return f1.isDefinedAt(t) ? f1.apply(t) : f2.apply(t);
  }

}
