package org.folio.rest.validate;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assume.assumeThat;
import java.util.List;
import java.util.function.Consumer;
import org.folio.test.junit.TestStartLoggingRule;
import org.jeasy.random.randomizers.collection.ListRandomizer;
import org.jeasy.random.randomizers.text.StringRandomizer;
import org.junit.Rule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class ValidationMethodsEqualsTest {

  private static final String NULL = String.valueOf((Object) null);

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @DataPoints
  public static String[] strings = generateRandoms();

  @Theory
  public void validateEqualsIsSuccessfulIfObjectsEqual(String actual, String expected) {
    assumeThat(actual, equalTo(expected));

    Consumer<String> method = ValidationMethods.validateEquals(expected);

    method.accept(actual);
  }

  @Theory
  public void validateEqualsFailsIfObjectsNotEqual(String actual, String expected) {
    assumeThat(actual, not(equalTo(expected)));

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(allOf(
      containsString(defaultString(actual, NULL)),
      containsString(defaultString(expected, NULL))));

    Consumer<String> method = ValidationMethods.validateEquals(expected);

    method.accept(actual);
  }

  @Theory
  public void validateEqualsWithCustomErrMessage(String actual, String expected) {
    assumeThat(actual, not(equalTo(expected)));

    String msg = String.format("NOT EQUAL: %s, %s", expected, actual);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(msg);

    Consumer<String> method = ValidationMethods.validateEquals(expected, "NOT EQUAL: %s, %s", expected, actual);

    method.accept(actual);
  }

  private static String[] generateRandoms() {
    ListRandomizer<String> randomizer = new ListRandomizer<>(new StringRandomizer(), 10);

    List<String> list = randomizer.getRandomValue();
    list.add(null); // include null also 

    return list.toArray(new String[0]);
  }

}
