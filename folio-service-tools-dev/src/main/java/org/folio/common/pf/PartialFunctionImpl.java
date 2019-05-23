package org.folio.common.pf;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

class PartialFunctionImpl<T, R> implements PartialFunction<T, R> {

  private Predicate<? super T> isDefinedAt;
  private Function<? super T, ? extends R> function;


  PartialFunctionImpl(Predicate<? super T> isDefinedAt, Function<? super T, ? extends R> function) {
    Objects.requireNonNull(isDefinedAt, "Predicate is null");
    Objects.requireNonNull(function, "Function is null");

    this.function = function;
    this.isDefinedAt = isDefinedAt;
  }

  @Override
  public boolean isDefinedAt(T t) {
    return isDefinedAt.test(t);
  }

  @Override
  public R applySuccessfully(T t) {
    return function.apply(t);
  }
}
