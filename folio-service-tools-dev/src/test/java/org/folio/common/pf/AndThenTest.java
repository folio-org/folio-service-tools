package org.folio.common.pf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import static org.folio.common.pf.TestData.fNegate;
import static org.folio.common.pf.TestData.fNegativeIntMsg;
import static org.folio.common.pf.TestData.intsOfEveryKind;
import static org.folio.common.pf.TestData.positiveInt;

import java.util.function.Function;

import org.folio.test.extensions.TestStartLoggingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;

import org.junit.jupiter.params.provider.MethodSource;

class AndThenTest {

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();

  private Function<Integer, Integer> function = fNegate;
  private PartialFunction<Integer, Integer> pf = new PartialFunctionImpl<>(positiveInt, function);
  private Function<Integer, String> after = fNegativeIntMsg;

  private AndThen<Integer, Integer, String> andThen = new AndThen<>(pf, after);

  public static Integer[] testData() {
    return intsOfEveryKind();
  }

  @Test
  void notCreateWithNPEIfPartialFuncIsNull() {
    assertThrows(NullPointerException.class, () -> new AndThen<>(null, function));
  }

  @Test
  void notCreateWithNPEIfAfterFuncIsNull() {
    assertThrows(NullPointerException.class, () -> new AndThen<>(pf, null));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void isDefinedIfPartialFuncDefined(Integer i) {
    boolean expected = pf.isDefinedAt(i);
    assertThat(andThen.isDefinedAt(i), is(expected));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void applyResultEqualToAfterFuncAppliedToPartialFunc(Integer i) {
    assumeTrue(andThen.isDefinedAt(i));

    assertThat(andThen.apply(i), is(after.apply(function.apply(i))));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void notAppliedIfPartialFuncNotDefined(Integer i) {
    assumeFalse(pf.isDefinedAt(i));

    NotDefinedException thrown = assertThrows(NotDefinedException.class, () -> andThen.apply(i));
    assertThat(thrown.getMessage(), containsString(i.toString()));
  }
}
