package org.folio.common.pf;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import static org.folio.common.pf.TestData.fPositiveIntMsg;
import static org.folio.common.pf.TestData.intsOfEveryKind;
import static org.folio.common.pf.TestData.positiveInt;

import java.util.Optional;

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
public class LiftedTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private PartialFunction<Integer, String> pf = new PartialFunctionImpl<>(positiveInt, fPositiveIntMsg);

  private Lifted<Integer, String> lifted = new Lifted<>(pf);


  @DataPoints
  public static Integer[] testData() {
    return intsOfEveryKind();
  }

  @Test
  public void notCreateWithNPEIfPartialFuncIsNull() {
    thrown.expect(NullPointerException.class);

    new Lifted<>(null);
  }

  @Theory
  public void appliedToValueIfPartialFuncDefined(Integer i) {
    assumeThat(pf.isDefinedAt(i), is(true));

    assertThat(lifted.apply(i), is(Optional.ofNullable(pf.apply(i))));
  }

  @Theory
  public void appliedToEmptyIfPartialFuncNotDefined(Integer i) {
    assumeThat(pf.isDefinedAt(i), is(false));

    assertThat(lifted.apply(i), is(Optional.empty()));
  }
}
