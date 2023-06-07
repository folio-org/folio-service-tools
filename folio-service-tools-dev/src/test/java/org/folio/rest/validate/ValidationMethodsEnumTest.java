package org.folio.rest.validate;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.function.Consumer;

import org.folio.test.extensions.TestStartLoggingExtension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ValidationMethodsEnumTest {

  private enum RGBColors {
    RED, GREEN, BLUE
  }

  public static String[] upperCaseNames() {
    return Arrays.stream(RGBColors.values()).map(Enum::name).toArray(String[]::new);
  }

  public static String[] caseInsensitiveReds() {
    return new String[] {"red", "Red", "rEd"};
  }

  public static String[] invalid() {
    return new String[] {null, "WHITE", "RED ", "blu"};
  }

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();


  @ParameterizedTest
  @MethodSource("caseInsensitiveReds")
  void validateEnumAcceptsNamesInAnyCase(String enumName) {
    Consumer<String> method = ValidationMethods.validateEnum(RGBColors.class);

    method.accept(enumName);
  }

  @ParameterizedTest
  @MethodSource("invalid")
  void validateEnumFailsIfNameIsInvalid(String enumName) {
    Consumer<String> method = ValidationMethods.validateEnum(RGBColors.class);

    assertThrows(IllegalArgumentException.class, () -> method.accept(enumName));
  }

  @ParameterizedTest
  @MethodSource("caseInsensitiveReds")
  void caseSensitiveValidateEnumFailsIfCaseIsInvalid(String enumName) {
    Consumer<String> method = ValidationMethods.validateEnum(RGBColors.class, false);

    assertThrows(IllegalArgumentException.class, () -> method.accept(enumName));
  }

  @ParameterizedTest
  @MethodSource("upperCaseNames")
  void caseSensitiveValidateEnumAcceptNamesInUpperCase(String enumName) {
    Consumer<String> method = ValidationMethods.validateEnum(RGBColors.class, false);

    method.accept(enumName);
  }

}
