package org.folio.common.pf;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

class Lifted<T, R> implements Function<T, Optional<R>> {

  private PartialFunction<T, R> pf;

  Lifted(PartialFunction<T, R> pf) {
    Objects.requireNonNull(pf, "Partial function is null");
    this.pf = pf;
  }

  @Override
  public Optional<R> apply(T t) {
    return Optional.ofNullable(pf.applyOrElse(t, t1 -> null));
  }

}
