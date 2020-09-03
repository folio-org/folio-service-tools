package org.folio.common.pf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeThat;

import static org.folio.common.pf.TestData.fOtherwise;
import static org.folio.common.pf.TestData.fPositiveIntMsg;
import static org.folio.common.pf.TestData.intsOfEveryKind;
import static org.folio.common.pf.TestData.otherwiseResult;
import static org.folio.common.pf.TestData.positiveInt;

import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import org.folio.test.junit.TestStartLoggingRule;

@RunWith(Theories.class)
public class PartialFunctionImplTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Function<Integer, String> function = fPositiveIntMsg;
  private Predicate<Integer> predicate = positiveInt;
  private PartialFunction<Integer, String> pf = new PartialFunctionImpl<>(predicate, function);


  @DataPoints
  public static Integer[] testData() {
    return intsOfEveryKind();
  }

  @Test
  public void notCreateWithNPEIfPredicateIsNull() {
    thrown.expect(NullPointerException.class);

    new PartialFunctionImpl<>(null, function);
  }

  @Test
  public void notCreateWithNPEIfFunctionIsNull() {
    thrown.expect(NullPointerException.class);

    new PartialFunctionImpl<>(predicate, null);
  }

  @Theory
  public void isDefinedAtResultEqualToPredicateResult(Integer i) {
    boolean expected = predicate.test(i);
    assertThat(pf.isDefinedAt(i), is(expected));
  }

  @Theory
  public void applySuccessfullyResultEqualToFunctionResult(Integer i) {
    assumeThat(pf.isDefinedAt(i), is(true));

    String expected = function.apply(i);
    assertThat(pf.applySuccessfully(i), is(expected));
  }

  @Theory
  public void appliedIfPartialFunctionDefined(Integer i) {
    assumeThat(pf.isDefinedAt(i), is(true));

    assumeThat(pf.apply(i), is(function.apply(i)));
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

    assertThat(pf.applyOrElse(i, fOtherwise), is(pf.apply(i)));
  }

  @Theory
  public void applyOrElseFallbackToOtherwiseIfPartialFunctionDefined(Integer i) {
    assumeThat(pf.isDefinedAt(i), is(false));

    assertThat(pf.applyOrElse(i, fOtherwise), is(otherwiseResult));
  }
}
