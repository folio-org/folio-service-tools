package org.folio.common.pf;

import static org.folio.common.pf.TestData.fNegate;
import static org.folio.common.pf.TestData.fPositiveIntMsg;
import static org.folio.common.pf.TestData.intsOfEveryKind;
import static org.folio.common.pf.TestData.positiveInt;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;
import java.util.function.Function;
import org.folio.test.junit.TestStartLoggingRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class ComposeTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private PartialFunction<Integer, String> pf = new PartialFunctionImpl<>(positiveInt, fPositiveIntMsg);
  private Function<Integer, Integer> before = fNegate;

  private Compose<Integer, Integer, String> compose = new Compose<>(pf, before);

  @DataPoints
  public static Integer[] testData() {
    return intsOfEveryKind();
  }

  @Test
  public void notCreateWithNPEIfPartialFuncIsNull() {
    thrown.expect(NullPointerException.class);

    new Compose<>(null, before);
  }

  @Test
  public void notCreateWithNPEIfBeforeFuncIsNull() {
    thrown.expect(NullPointerException.class);

    new Compose<>(pf, null);
  }

  @Theory
  public void isDefinedIfPartialFuncDefinedForBeforeFuncResult(Integer i) {
    boolean expected = pf.isDefinedAt(before.apply(i));

    assertThat(compose.isDefinedAt(i), is(expected));
  }

  @Theory
  public void applyResultEqualToPartialFunctionAppliedToBeforeFunc(Integer i) {
    assumeThat(compose.isDefinedAt(i), is(true));

    assertThat(compose.apply(i), is(pf.apply(before.apply(i))));
  }

  @Theory
  public void notAppliedIfPartialFuncNotDefinedForBeforeFuncResult(Integer i) {
    assumeThat(pf.isDefinedAt(before.apply(i)), is(false));

    thrown.expect(NotDefinedException.class);
    thrown.expectMessage(containsString(i.toString()));

    compose.apply(i);
  }
}
