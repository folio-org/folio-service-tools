package org.folio.common.pf;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import java.util.function.Function;
import java.util.function.Predicate;

import org.jeasy.random.api.Randomizer;
import org.jeasy.random.randomizers.range.IntegerRangeRandomizer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import org.folio.test.junit.TestStartLoggingRule;

@RunWith(Theories.class)
public class PartialFunctionImplTest {

  private static final Randomizer<Integer> negativeRandom = new IntegerRangeRandomizer(Integer.MIN_VALUE, -1);
  private static final Randomizer<Integer> positiveRandom = new IntegerRangeRandomizer(1, Integer.MAX_VALUE);

  private static final Predicate<Integer> PREDICATE = Predicates.positiveInt();
  private static final Function<Integer, String> FUNC = integer -> integer.toString();

  private static final String OTHERWISE_RESULT = "OTHERWISE";
  private static final Function<Integer, String> OTHERWISE = integer -> OTHERWISE_RESULT;

  @Rule
  public TestRule startLogger = new TestStartLoggingRule();
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private PartialFunction<Integer, String> pf = new PartialFunctionImpl<>(PREDICATE, FUNC);


  @DataPoint
  public static Integer zero = 0;

  @DataPoint("negative")
  public static Integer negative() {
    return negativeRandom.getRandomValue();
  }

  @DataPoint("positive")
  public static Integer positive() {
    return positiveRandom.getRandomValue();
  }

  @Test
  public void notCreateWithNPEIfPredicateIsNull() {
    thrown.expect(NullPointerException.class);

    new PartialFunctionImpl<>(null, FUNC);
  }

  @Test
  public void notCreateWithNPEIfFunctionIsNull() {
    thrown.expect(NullPointerException.class);

    new PartialFunctionImpl<>(PREDICATE, null);
  }

  @Theory
  public void isDefinedAtResultEqualToPredicateResult(Integer i) {
    boolean expected = PREDICATE.test(i);
    assertThat(pf.isDefinedAt(i), is(expected));
  }

  @Theory
  public void applySuccessfullyResultEqualToFunctionResult(Integer i) {
    assumeThat(pf.isDefinedAt(i), is(true));

    String expected = FUNC.apply(i);
    assertThat(pf.applySuccessfully(i), is(expected));
  }

  @Theory
  public void appliedIfPartialFunctionDefined(Integer i) {
    assumeThat(pf.isDefinedAt(i), is(true));

    assumeThat(pf.apply(i), is(FUNC.apply(i)));
  }

  @Theory
  public void notAppliedIfPartialFunctionNotDefined(Integer i) {
    assumeThat(pf.isDefinedAt(i), is(false));

    thrown.expect(NotDefinedException.class);
    thrown.expectMessage(containsString(i.toString()));

    pf.apply(i);
  }

  @Theory
  public void applyOrElseDoesntAcceptNullOtherwise(Integer i) {
    thrown.expect(NullPointerException.class);

    pf.applyOrElse(i, null);
  }

  @Theory
  public void applyOrElseEqualToApplyIfPartialFunctionDefined(Integer i) {
    assumeThat(pf.isDefinedAt(i), is(true));

    assertThat(pf.applyOrElse(i, OTHERWISE), is(pf.apply(i)));
  }

  @Theory
  public void applyOrElseFallbackToOtherwiseIfPartialFunctionDefined(Integer i) {
    assumeThat(pf.isDefinedAt(i), is(false));

    assertThat(pf.applyOrElse(i, OTHERWISE), is(OTHERWISE_RESULT));
  }
}
