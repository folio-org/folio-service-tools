package org.folio.rest.validate;

import static org.apache.commons.lang3.EnumUtils.getEnumList;
import static org.apache.commons.lang3.EnumUtils.isValidEnum;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.apache.commons.lang3.Validate;

public class ValidationMethods {

  private ValidationMethods() {
  }

  /**
   * Returns test method which verifies if passed String value can be converted to an enum item of {@code enumClass} class.
   * If validation failed, the method throws {@code java.lang.IllegalArgumentException}, otherwise finishes silently.
   * <p>
   * Method ignores the case of String value. To take it into account use {@link #validateEnum(Class, boolean)}.
   *
   * @param enumClass enum class
   * @param <E> enum
   * @return test method
   */
  public static <E extends Enum<E>> Consumer<String> validateEnum(final Class<E> enumClass) {
    return validateEnum(enumClass, true);
  }

  /**
   * Returns test method which verifies if passed String value can be converted to an enum item of {@code enumClass} class.
   * If validation failed, the method throws {@code java.lang.IllegalArgumentException}, otherwise finishes silently.
   *
   * @param enumClass enum class
   * @param ignoreCase should the method ignore the case of String value or not
   * @param <E> enum
   * @return test method
   */
  public static <E extends Enum<E>> Consumer<String> validateEnum(final Class<E> enumClass, boolean ignoreCase) {
    return enumName -> {
      String notEmpty = defaultString(enumName);
      String tested = ignoreCase ? notEmpty.toUpperCase() : notEmpty;

      isTrue(isValidEnum(enumClass, tested),
        "%s is incorrect: %s. Possible values: %s",
        enumClass.getSimpleName(), enumName, join(getEnumList(enumClass), ", "));
    };
  }

  public static <T> Consumer<T> validateEquals(T expected) {
    return validateEquals(expected, (exp, actual) -> String.format(
        "The validated object is not equal to the expected one: actual = %s, expected = %s", actual, exp));
  }

  public static <T> Consumer<T> validateEquals(T expected, BiFunction<T, T, String> errMessageProvider) {
    return actual -> Validate.isTrue(Objects.equals(expected, actual), errMessageProvider.apply(expected, actual));
  }

  public static <T> Consumer<T> validateEquals(T expected, String message, Object... values) {
    return actual -> Validate.isTrue(Objects.equals(expected, actual), message, values);
  }
}
