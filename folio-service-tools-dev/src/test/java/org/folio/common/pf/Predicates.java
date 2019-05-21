package org.folio.common.pf;

import java.util.function.Predicate;

public final class Predicates {

  private Predicates() {
  }

  public static Predicate<Integer> positiveInt() {
    return i -> i > 0;
  }

}
