package org.folio.common.pf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.folio.common.pf.TestData.fPositiveIntMsg;
import static org.folio.common.pf.TestData.intsOfEveryKind;
import static org.folio.common.pf.TestData.positiveInt;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Optional;

import org.folio.test.extensions.TestStartLoggingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class LiftedTest {

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();

  private final PartialFunction<Integer, String> pf = new PartialFunctionImpl<>(positiveInt, fPositiveIntMsg);

  private final Lifted<Integer, String> lifted = new Lifted<>(pf);

  public static Integer[] testData() {
    return intsOfEveryKind();
  }

  @Test
  void notCreateWithNPEIfPartialFuncIsNull() {
    assertThrows(NullPointerException.class, () -> new Lifted<>(null));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void appliedToValueIfPartialFuncDefined(Integer i) {
    assumeTrue(pf.isDefinedAt(i));

    assertThat(lifted.apply(i), is(Optional.ofNullable(pf.apply(i))));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void appliedToEmptyIfPartialFuncNotDefined(Integer i) {
    assumeFalse(pf.isDefinedAt(i));

    assertThat(lifted.apply(i), is(Optional.empty()));
  }
}
