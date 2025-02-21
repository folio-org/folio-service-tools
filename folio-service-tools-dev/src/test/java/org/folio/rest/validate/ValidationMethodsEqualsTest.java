package org.folio.rest.validate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.folio.test.extensions.TestStartLoggingExtension;
import org.jeasy.random.randomizers.collection.ListRandomizer;
import org.jeasy.random.randomizers.text.StringRandomizer;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ValidationMethodsEqualsTest {

  private static final String NULL = String.valueOf((Object) null);

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();

  @ParameterizedTest
  @MethodSource("generateRandoms")
  void validateEqualsIsSuccessfulIfObjectsEqual(String actual, String expected) {
    assumeTrue(Objects.nonNull(actual) && Objects.nonNull(expected) && actual.equals(expected));

    Consumer<String> method = ValidationMethods.validateEquals(expected);

    method.accept(actual);
  }

  @ParameterizedTest
  @MethodSource("generateRandoms")
  void validateEqualsFailsIfObjectsNotEqual(String actual, String expected) {
    assumeFalse(Objects.equals(actual, expected));

    Consumer<String> method = ValidationMethods.validateEquals(expected);
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> method.accept(actual));

    assertThat(thrown.getMessage(), allOf(
      containsString(Objects.toString(actual, NULL)),
      containsString(Objects.toString(expected, NULL))));
  }

  @ParameterizedTest
  @MethodSource("generateRandoms")
  void validateEqualsWithCustomErrMessage(String actual, String expected) {
    assumeFalse(Objects.equals(actual, expected));

    String msg = String.format("NOT EQUAL: %s, %s", expected, actual);

    Consumer<String> method = ValidationMethods.validateEquals(expected, "NOT EQUAL: %s, %s", expected, actual);
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> method.accept(actual));
    assertThat(thrown.getMessage(), containsString(msg));
  }

  private static Stream<Arguments> generateRandoms() {
    ListRandomizer<String> randomizer = new ListRandomizer<>(new StringRandomizer(), 10);

    List<String> list = randomizer.getRandomValue();
    list.add(null); // include null also

    return list.stream()
      .flatMap(str1 ->
        list.stream().map(str2 -> arguments(str1, str2)));
  }

}
