package org.folio.common.pf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeThat;

import static org.folio.common.pf.TestData.fNegativeIntMsg;
import static org.folio.common.pf.TestData.fPositiveIntMsg;
import static org.folio.common.pf.TestData.intsOfEveryKind;
import static org.folio.common.pf.TestData.negativeInt;
import static org.folio.common.pf.TestData.positiveInt;

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
public class OrElseTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private PartialFunction<Integer, String> f1 = new PartialFunctionImpl<>(positiveInt, fPositiveIntMsg);
  private PartialFunction<Integer, String> f2 = new PartialFunctionImpl<>(negativeInt, fNegativeIntMsg);

  private OrElse<Integer, String> orElse = new OrElse<>(f1, f2);

  @DataPoints
  public static Integer[] testData() {
    return intsOfEveryKind();
  }

  @Test
  public void notCreateWithNPEIfF1IsNull() {
    thrown.expect(NullPointerException.class);

    new OrElse<>(null, f2);
  }

  @Test
  public void notCreateWithNPEIfF2IsNull() {
    thrown.expect(NullPointerException.class);

    new OrElse<>(f1, null);
  }

  @Theory
  public void isDefinedIfAtLeastOnePartialFunctionDefined(Integer i) {
    boolean expected = f1.isDefinedAt(i) || f2.isDefinedAt(i);
    assertThat(orElse.isDefinedAt(i), is(expected));
  }

  @Theory
  public void applyResultEqualToF1IfF1Defined(Integer i) {
    assumeThat(f1.isDefinedAt(i), is(true));

    assertThat(orElse.apply(i), is(f1.apply(i)));
  }

  @Theory
  public void applyResultEqualToF2IfF2DefinedAndF1NotDefined(Integer i) {
    assumeThat(f1.isDefinedAt(i), is(false));
    assumeThat(f2.isDefinedAt(i), is(true));

    assertThat(orElse.apply(i), is(f2.apply(i)));
  }

  @Theory
  public void notAppliedIfBothPFsNotDefined(Integer i) {
    assumeThat(f1.isDefinedAt(i), is(false));
    assumeThat(f2.isDefinedAt(i), is(false));

    thrown.expect(NotDefinedException.class);
    thrown.expectMessage(containsString(i.toString()));

    orElse.apply(i);
  }
}
