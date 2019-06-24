package org.folio.rest.validate;

import java.util.Arrays;
import java.util.function.Consumer;
import org.folio.test.junit.TestStartLoggingRule;
import org.junit.Rule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class ValidationMethodsEnumTest {

  private enum RGBColors {

    RED, GREEN, BLUE

  }

  @DataPoints("upper-case-names")
  public static String[] ucNames = Arrays.stream(RGBColors.values()).map(Enum::name).toArray(String[]::new);

  @DataPoints("case-insensitive-reds")
  public static String[] ciReds = new String[] {"red", "Red", "rEd"};

  @DataPoints("invalid")
  public static String[] invalid = new String[] {null, "WHITE", "RED ", "blu"};

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();
  @Rule
  public ExpectedException thrown = ExpectedException.none();


  @Theory
  public void validateEnumAcceptsNamesInAnyCase(@FromDataPoints("case-insensitive-reds") String enumName) {
    Consumer<String> method = ValidationMethods.validateEnum(RGBColors.class);

    method.accept(enumName);
  }

  @Theory
  public void validateEnumFailsIfNameIsInvalid(@FromDataPoints("invalid") String enumName) {
    Consumer<String> method = ValidationMethods.validateEnum(RGBColors.class);

    thrown.expect(IllegalArgumentException.class);

    method.accept(enumName);
  }

  @Theory
  public void caseSensitiveValidateEnumFailsIfCaseIsInvalid(@FromDataPoints("case-insensitive-reds") String enumName) {
    Consumer<String> method = ValidationMethods.validateEnum(RGBColors.class, false);

    thrown.expect(IllegalArgumentException.class);

    method.accept(enumName);
  }

  @Theory
  public void caseSensitiveValidateEnumAcceptNamesInUpperCase(@FromDataPoints("upper-case-names") String enumName) {
    Consumer<String> method = ValidationMethods.validateEnum(RGBColors.class, false);

    method.accept(enumName);
  }

}
