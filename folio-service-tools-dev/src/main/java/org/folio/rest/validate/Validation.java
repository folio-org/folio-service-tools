package org.folio.rest.validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.commons.lang3.tuple.Pair;

@SuppressWarnings("java:S3740")
public class Validation {

  private final List<Pair<Object, Consumer>> tests = new ArrayList<>();


  private Validation() {
  }

  public static Validation instance() {
    return new Validation();
  }

  public <T> Validation addTest(T testValue, Consumer<T> testMethod) {
    Objects.requireNonNull(testMethod);

    tests.add(Pair.of(testValue, testMethod));

    return this;
  }

  @SuppressWarnings("unchecked")
  public Future<Void> validate() {
    Promise<Void> result = Promise.promise();

    try {
      for (Pair<Object, Consumer> test : tests) {
        Object testValue = test.getLeft();
        Consumer testMethod = test.getRight();

        testMethod.accept(testValue);
      }

      result.complete();
    } catch (IllegalArgumentException | NullPointerException e) {
      result.fail(e);
    }

    return result.future();
  }

}
