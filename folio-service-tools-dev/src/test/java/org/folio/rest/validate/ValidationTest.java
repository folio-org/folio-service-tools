package org.folio.rest.validate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import io.vertx.core.Future;
import java.util.Objects;
import java.util.function.Consumer;
import org.apache.commons.lang3.Validate;
import org.folio.test.extensions.TestStartLoggingExtension;
import org.jeasy.random.api.Randomizer;
import org.jeasy.random.randomizers.text.StringRandomizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ValidationTest {

  private static final Randomizer<String> stringRandomizer = new StringRandomizer();

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();
  private Validation validation;

  @BeforeEach
  void setUp() {
    validation = Validation.instance();
  }

  @Test
  void addTestsFailedWithNPEIfTestMethodNull() {
    assertThrows(NullPointerException.class, () -> validation.addTest("test", null));
  }

  @Test
  void addTestsAcceptsNullTestValue() {
    try {
      validation.addTest(null, new AcceptAll<>());
    } catch (NullPointerException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void validateReturnsCompletedFutureIfAllTestsPass() {
    String testValue = stringRandomizer.getRandomValue();

    validation
      .addTest(testValue, Objects::requireNonNull)
      .addTest(testValue, new TestEqual<>(testValue));

    Future<Void> result = validation.validate();

    assertThat(result, notNullValue());
    assertThat(result.isComplete(), equalTo(true));
  }

  @Test
  void validateReturnsCompletedFutureIfNoTests() {
    Future<Void> result = validation.validate();

    assertThat(result, notNullValue());
    assertThat(result.isComplete(), equalTo(true));
  }

  @Test
  void validateReturnsFailedFutureIfTestThrowsNPE() {
    String testValue = null;

    validation
      .addTest(testValue, Objects::requireNonNull)
      .addTest(testValue, new TestEqual<>(testValue));

    Future<Void> result = validation.validate();

    assertThat(result, notNullValue());
    assertThat(result.failed(), equalTo(true));
    assertThat(result.cause(), is(instanceOf(NullPointerException.class)));
  }

  @Test
  void validateReturnsFailedFutureIfTestThrowsIllArgExc() {
    String testValue = stringRandomizer.getRandomValue();

    validation
      .addTest(testValue, Objects::requireNonNull)
      .addTest(testValue,
        new TestEqual<>(stringRandomizer.getRandomValue())); // should fail with IllegalArgumentException

    Future<Void> result = validation.validate();

    assertThat(result, notNullValue());
    assertThat(result.failed(), equalTo(true));
    assertThat(result.cause(), is(instanceOf(IllegalArgumentException.class)));
  }

  private static class AcceptAll<T> implements Consumer<T> {
    @Override
    public void accept(T t) {
      // do nothing
    }
  }

  private record TestEqual<T>(T expected) implements Consumer<T> {

    @Override
    public void accept(T t) {
      Validate.isTrue(Objects.equals(expected, t),
        "Tested value is not equal to expected value: tested = %s, expected = %s",
        t, expected);
    }
  }
}
