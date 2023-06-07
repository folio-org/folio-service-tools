package org.folio.common.pf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.folio.common.pf.TestData.fNegativeIntMsg;
import static org.folio.common.pf.TestData.fPositiveIntMsg;
import static org.folio.common.pf.TestData.intsOfEveryKind;
import static org.folio.common.pf.TestData.negativeInt;
import static org.folio.common.pf.TestData.positiveInt;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.folio.test.extensions.TestStartLoggingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class OrElseTest {

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();

  private PartialFunction<Integer, String> f1 = new PartialFunctionImpl<>(positiveInt, fPositiveIntMsg);
  private PartialFunction<Integer, String> f2 = new PartialFunctionImpl<>(negativeInt, fNegativeIntMsg);

  private OrElse<Integer, String> orElse = new OrElse<>(f1, f2);

  public static Integer[] testData() {
    return intsOfEveryKind();
  }

  @Test
  void notCreateWithNPEIfF1IsNull() {
    assertThrows(NullPointerException.class, () -> new OrElse<>(null, f2));
  }

  @Test
  void notCreateWithNPEIfF2IsNull() {
    assertThrows(NullPointerException.class, () -> new OrElse<>(f1, null));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void isDefinedIfAtLeastOnePartialFunctionDefined(Integer i) {
    boolean expected = f1.isDefinedAt(i) || f2.isDefinedAt(i);
    assertThat(orElse.isDefinedAt(i), is(expected));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void applyResultEqualToF1IfF1Defined(Integer i) {
    assumeTrue(f1.isDefinedAt(i));

    assertThat(orElse.apply(i), is(f1.apply(i)));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void applyResultEqualToF2IfF2DefinedAndF1NotDefined(Integer i) {
    assumeFalse(f1.isDefinedAt(i));
    assumeTrue(f2.isDefinedAt(i));

    assertThat(orElse.apply(i), is(f2.apply(i)));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void notAppliedIfBothPFsNotDefined(Integer i) {
    assumeFalse(f1.isDefinedAt(i));
    assumeFalse(f2.isDefinedAt(i));

    NotDefinedException thrown = assertThrows(NotDefinedException.class, () -> orElse.apply(i));
    assertThat(thrown.getMessage(), containsString(i.toString()));
  }
}
