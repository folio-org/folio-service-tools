package org.folio.rest.exc;

import java.util.function.Predicate;

import org.folio.rest.tools.utils.ValidationHelper;

public class ExceptionPredicates {

  private ExceptionPredicates() {
  }

  public static Predicate<Throwable> instanceOf(Class<? extends Throwable> cl) {
    return new InstanceOfPredicate<>(cl);
  }

  public static Predicate<Throwable> invalidUUID() {
    return t -> ValidationHelper.isInvalidUUID(t.getMessage());
  }

  private static class InstanceOfPredicate<T, E> implements Predicate<E> {

    private Class<T> excClass;

    InstanceOfPredicate(Class<T> excClass) {
      this.excClass = excClass;
    }

    @Override
    public boolean test(E e) {
      return excClass.isInstance(e);
    }
  }
}
