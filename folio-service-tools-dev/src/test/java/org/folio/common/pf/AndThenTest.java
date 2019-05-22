package org.folio.common.pf;

import static org.folio.common.pf.TestData.fNegate;
import static org.folio.common.pf.TestData.fNegativeIntMsg;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import static org.folio.common.pf.TestData.intsOfEveryKind;
import static org.folio.common.pf.TestData.positiveInt;

import java.util.function.Function;
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
public class AndThenTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Function<Integer, Integer> function = fNegate;
  private PartialFunction<Integer, Integer> pf = new PartialFunctionImpl<>(positiveInt, function);
  private Function<Integer, String> after = fNegativeIntMsg;

  private AndThen<Integer, Integer, String> andThen = new AndThen<>(pf, after);

  @DataPoints
  public static Integer[] testData() {
    return intsOfEveryKind();
  }

  @Test
  public void notCreateWithNPEIfPartialFuncIsNull() {
    thrown.expect(NullPointerException.class);

    new AndThen<>(null, function);
  }

  @Test
  public void notCreateWithNPEIfAfterFuncIsNull() {
    thrown.expect(NullPointerException.class);

    new AndThen<>(pf, null);
  }

  @Theory
  public void isDefinedIfPartialFuncDefined(Integer i) {
    boolean expected = pf.isDefinedAt(i);
    assertThat(andThen.isDefinedAt(i), is(expected));
  }

  @Theory
  public void applyResultEqualToAfterFuncAppliedToPartialFunc(Integer i) {
    assumeThat(andThen.isDefinedAt(i), is(true));

    assertThat(andThen.apply(i), is(after.apply(function.apply(i))));
  }

  @Theory
  public void notAppliedIfPartialFuncNotDefined(Integer i) {
    assumeThat(pf.isDefinedAt(i), is(false));

    thrown.expect(NotDefinedException.class);
    thrown.expectMessage(containsString(i.toString()));

    andThen.apply(i);
  }
}
