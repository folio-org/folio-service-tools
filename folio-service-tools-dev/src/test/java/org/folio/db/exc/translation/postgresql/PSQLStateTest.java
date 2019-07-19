package org.folio.db.exc.translation.postgresql;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeasy.random.api.Randomizer;
import org.jeasy.random.randomizers.collection.SetRandomizer;
import org.jeasy.random.randomizers.range.IntegerRangeRandomizer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import org.folio.test.junit.TestStartLoggingRule;

@RunWith(Theories.class)
public class PSQLStateTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @DataPoints("invalid-codes")
  public static String[] invalidCodes = TestData.generateInvalidCodes();

  @DataPoints("valid-codes")
  public static String[] validCodes = TestData.generateValidCodes();

  @Test
  public void getCodeReturnsAlphaNumStringOf5Chars() {
    for (PSQLState state : PSQLState.values()) {
      String code = state.getCode();

      assertThat(code, not(isEmptyOrNullString()));
      assertThat(code.length(), is(5));
      assertThat(StringUtils.isAlphanumeric(code), is(true));
    }
  }

  @Test
  public void getCodeClassReturnsFirst2CharsOfCode() {
    for (PSQLState state : PSQLState.values()) {
      assertThat(state.getCodeClass(), allOf(
        not(isEmptyOrNullString()), 
        is(state.getCodeClass().substring(0, 2))
      ));
    }
  }

  @Theory
  public void containsReturnsFalseForInvalidCode(@FromDataPoints("invalid-codes") String code) {
    boolean contains = PSQLState.contains(code);

    assertThat(contains, is(false));
  }

  @Theory
  public void containsReturnsTrueForValidCode(@FromDataPoints("valid-codes") String code) {
    boolean contains = PSQLState.contains(code);

    assertThat(contains, is(true));
  }

  @Theory
  public void containsWorksWithMixedCase(@FromDataPoints("valid-codes") String code) {
    String mixed = mixCase(code);

    boolean contains = PSQLState.contains(mixed);

    assertThat(contains, is(true));
  }

  @Test
  public void containsReturnsFalseForNull() {
    boolean contains = PSQLState.contains(null);

    assertThat(contains, is(false));
  }

  @Theory
  public void enumOfReturnsEnumForValidCode(@FromDataPoints("valid-codes") String code) {
    PSQLState state = PSQLState.enumOf(code);

    assertThat(state, notNullValue());
  }

  @Theory
  public void enumOfWorksWithMixedCase(@FromDataPoints("valid-codes") String code) {
    String mixed = mixCase(code);

    PSQLState state = PSQLState.enumOf(mixed);

    assertThat(state, notNullValue());
  }

  @Theory
  public void enumOfFailsForInvalidCode(@FromDataPoints("invalid-codes") String code) {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(code);

    PSQLState.enumOf(code);
  }

  @Test
  public void enumOfFailsForNull() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(String.valueOf((Object) null));

    PSQLState.enumOf(null);
  }

  @Test
  public void belongToClassReturnsTrueIfSameClass() {
    PSQLState classState = PSQLState.INTEGRITY_CONSTRAINT_VIOLATION;

    boolean result = classState.belongToClassOf(PSQLState.UNIQUE_VIOLATION);

    assertThat(result, is(true));
  }

  @Test
  public void belongToClassReturnsFalseIfDiffClass() {
    PSQLState classState = PSQLState.INTEGRITY_CONSTRAINT_VIOLATION;

    boolean result = classState.belongToClassOf(PSQLState.ACTIVE_SQL_TRANSACTION);

    assertThat(result, is(false));
  }

  @Test
  public void belongToClassOfCodeReturnsTrueIfSameClass() {
    PSQLState classState = PSQLState.INTEGRITY_CONSTRAINT_VIOLATION;

    boolean result = classState.belongToClassOf(PSQLState.UNIQUE_VIOLATION.getCode());

    assertThat(result, is(true));
  }

  @Test
  public void belongToClassOfCodeReturnsFalseIfDiffClass() {
    PSQLState classState = PSQLState.INTEGRITY_CONSTRAINT_VIOLATION;

    boolean result = classState.belongToClassOf(PSQLState.ACTIVE_SQL_TRANSACTION.getCode());

    assertThat(result, is(false));
  }

  @Test
  public void belongToClassOfCodeReturnsFalseIfNullCode() {
    PSQLState classState = PSQLState.INTEGRITY_CONSTRAINT_VIOLATION;

    boolean result = classState.belongToClassOf((String) null);

    assertThat(result, is(false));
  }

  @Test
  public void belongToClassOfCodeWorksWithMixedCase() {
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
