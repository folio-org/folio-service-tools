package org.folio.common.pf;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assume.assumeThat;

import static org.folio.common.pf.TestData.fOtherwise;
import static org.folio.common.pf.TestData.intsOfEveryKind;

import org.junit.Rule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import org.folio.test.junit.TestStartLoggingRule;

@RunWith(Theories.class)
public class EmptyTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Empty<Integer, String> empty = new Empty<>();


  @DataPoints
  public static Integer[] testData() {
    return intsOfEveryKind();
  }

  @Theory
  public void isDefinedAtIsAlwaysFalse(Integer i) {
    assertFalse(empty.isDefinedAt(i));
  }

  @Theory
  public void applyAlwaysFailed(Integer i) {
    thrown.expect(NotDefinedException.class);
    thrown.expectMessage(containsString(i.toString()));

    empty.apply(i);
  }

  @Theory
  public void applyOrElseAlwaysReturnsOtherwiseResult(Integer i) {
    assumeThat(empty.applyOrElse(i, fOtherwise), is(fOtherwise.apply(i)));
  }
}
