package org.folio.common.pf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.folio.common.pf.TestData.fNegate;
import static org.folio.common.pf.TestData.fPositiveIntMsg;
import static org.folio.common.pf.TestData.intsOfEveryKind;
import static org.folio.common.pf.TestData.positiveInt;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.function.Function;

import org.folio.test.extensions.TestStartLoggingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ComposeTest {

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();

  private PartialFunction<Integer, String> pf = new PartialFunctionImpl<>(positiveInt, fPositiveIntMsg);
  private Function<Integer, Integer> before = fNegate;

  private Compose<Integer, Integer, String> compose = new Compose<>(pf, before);

  public static Integer[] testData() {
    return intsOfEveryKind();
  }

  @Test
  void notCreateWithNPEIfPartialFuncIsNull() {
    assertThrows(NullPointerException.class, () -> new Compose<>(null, before));
  }

  @Test
  void notCreateWithNPEIfBeforeFuncIsNull() {
    assertThrows(NullPointerException.class, () -> new Compose<>(pf, null));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void isDefinedIfPartialFuncDefinedForBeforeFuncResult(Integer i) {
    boolean expected = pf.isDefinedAt(before.apply(i));

    assertThat(compose.isDefinedAt(i), is(expected));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void applyResultEqualToPartialFunctionAppliedToBeforeFunc(Integer i) {
    assumeTrue(compose.isDefinedAt(i));

    assertThat(compose.apply(i), is(pf.apply(before.apply(i))));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void notAppliedIfPartialFuncNotDefinedForBeforeFuncResult(Integer i) {
    assumeFalse(pf.isDefinedAt(before.apply(i)));

    NotDefinedException thrown = assertThrows(NotDefinedException.class, () -> compose.apply(i));
    assertThat(thrown.getMessage(), containsString(i.toString()));
  }
}
