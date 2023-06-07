package org.folio.db.exc.translation.postgresql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.folio.test.extensions.TestStartLoggingExtension;
import org.jeasy.random.api.Randomizer;
import org.jeasy.random.randomizers.collection.SetRandomizer;
import org.jeasy.random.randomizers.range.IntegerRangeRandomizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class PSQLStateTest {

  public static String[] invalidCodes() {
    return TestData.generateInvalidCodes();
  }

  public static String[] validCodes() {
    return TestData.generateValidCodes();
  }
  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();

  @Test
  void getCodeReturnsAlphaNumStringOf5Chars() {
    for (PSQLState state : PSQLState.values()) {
      String code = state.getCode();

      assertThat(code, not(emptyOrNullString()));
      assertThat(code.length(), is(5));
      assertThat(StringUtils.isAlphanumeric(code), is(true));
    }
  }

  @Test
  void getCodeClassReturnsFirst2CharsOfCode() {
    for (PSQLState state : PSQLState.values()) {
      assertThat(state.getCodeClass(), allOf(
        not(emptyOrNullString()),
        is(state.getCodeClass().substring(0, 2))
      ));
    }
  }

  @ParameterizedTest
  @MethodSource("invalidCodes")
  void containsReturnsFalseForInvalidCode(String code) {
    boolean contains = PSQLState.contains(code);

    assertThat(contains, is(false));
  }

  @ParameterizedTest
  @MethodSource("validCodes")
  void containsReturnsTrueForValidCode(String code) {
    boolean contains = PSQLState.contains(code);

    assertThat(contains, is(true));
  }

  @ParameterizedTest
  @MethodSource("validCodes")
  void containsWorksWithMixedCase(String code) {
    String mixed = mixCase(code);

    boolean contains = PSQLState.contains(mixed);

    assertThat(contains, is(true));
  }

  @Test
  void containsReturnsFalseForNull() {
    boolean contains = PSQLState.contains(null);

    assertThat(contains, is(false));
  }

  @ParameterizedTest
  @MethodSource("validCodes")
  void enumOfReturnsEnumForValidCode(String code) {
    PSQLState state = PSQLState.enumOf(code);

    assertThat(state, notNullValue());
  }

  @ParameterizedTest
  @MethodSource("validCodes")
  void enumOfWorksWithMixedCase(String code) {
    String mixed = mixCase(code);

    PSQLState state = PSQLState.enumOf(mixed);

    assertThat(state, notNullValue());
  }

  @ParameterizedTest
  @MethodSource("invalidCodes")
  void enumOfFailsForInvalidCode(String code) {
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> PSQLState.enumOf(code));
    assertThat(thrown.getMessage(), containsString(code));
  }

  @Test
  void enumOfFailsForNull() {
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> PSQLState.enumOf(null));
    assertThat(thrown.getMessage(), containsString(String.valueOf((Object)null)));
  }

  @Test
  void belongToClassReturnsTrueIfSameClass() {
    PSQLState classState = PSQLState.INTEGRITY_CONSTRAINT_VIOLATION;

    boolean result = classState.belongToClassOf(PSQLState.UNIQUE_VIOLATION);

    assertThat(result, is(true));
  }

  @Test
  void belongToClassReturnsFalseIfDiffClass() {
    PSQLState classState = PSQLState.INTEGRITY_CONSTRAINT_VIOLATION;

    boolean result = classState.belongToClassOf(PSQLState.ACTIVE_SQL_TRANSACTION);

    assertThat(result, is(false));
  }

  @Test
  void belongToClassOfCodeReturnsTrueIfSameClass() {
    PSQLState classState = PSQLState.INTEGRITY_CONSTRAINT_VIOLATION;

    boolean result = classState.belongToClassOf(PSQLState.UNIQUE_VIOLATION.getCode());

    assertThat(result, is(true));
  }

  @Test
  void belongToClassOfCodeReturnsFalseIfDiffClass() {
    PSQLState classState = PSQLState.INTEGRITY_CONSTRAINT_VIOLATION;

    boolean result = classState.belongToClassOf(PSQLState.ACTIVE_SQL_TRANSACTION.getCode());

    assertThat(result, is(false));
  }

  @Test
  void belongToClassOfCodeReturnsFalseIfNullCode() {
    PSQLState classState = PSQLState.INTEGRITY_CONSTRAINT_VIOLATION;

    boolean result = classState.belongToClassOf((String) null);

    assertThat(result, is(false));
  }

  @Test
  void belongToClassOfCodeWorksWithMixedCase() {
    PSQLState classState = PSQLState.FDW_ERROR;

    String mixed = mixCase(PSQLState.FDW_REPLY_HANDLE.getCode());
    boolean result = classState.belongToClassOf(mixed);

    assertThat(result, is(true));
  }

  private String mixCase(String code) {
    if (StringUtils.isBlank(code)) {
      return code;
    }

    StringBuilder result = new StringBuilder();

    char[] chars = code.toCharArray();

    for (char c : chars) {
      char updated = RandomUtils.nextBoolean() ? Character.toUpperCase(c) : Character.toLowerCase(c);
      result.append(updated);
    }

    return result.toString();
  }

  private static class TestData {

    private static final int CODE_NUMBER = new IntegerRangeRandomizer(10, 100).getRandomValue();

    static String[] allValidCodes = Arrays.stream(PSQLState.values())
      .map(PSQLState::getCode).toArray(String[]::new);

    static String[] generateValidCodes() {
      SetRandomizer<String> r = new SetRandomizer<>(new ValidCodeRandomizer(), TestData.CODE_NUMBER);

      return r.getRandomValue().toArray(new String[0]);
    }

    static String[] generateInvalidCodes() {
      SetRandomizer<String> r = new SetRandomizer<>(new InvalidCodeRandomizer(), TestData.CODE_NUMBER);

      return r.getRandomValue().toArray(new String[0]);
    }

    private static class InvalidCodeRandomizer implements Randomizer<String> {

      @Override
      public String getRandomValue() {
        String code;

        do {
          code = RandomStringUtils.randomAlphanumeric(5, 6);
        } while (ArrayUtils.contains(allValidCodes, code));

        return code;
      }

    }

    private static class ValidCodeRandomizer implements Randomizer<String> {

      @Override
      public String getRandomValue() {
        int i = RandomUtils.nextInt(0, allValidCodes.length);

        return allValidCodes[i];
      }
    }
  }
}
