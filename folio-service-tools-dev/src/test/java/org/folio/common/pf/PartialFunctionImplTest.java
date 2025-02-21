package org.folio.common.pf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import static org.folio.common.pf.TestData.FUNC_OTHERWISE;
import static org.folio.common.pf.TestData.fPositiveIntMsg;
import static org.folio.common.pf.TestData.intsOfEveryKind;
import static org.folio.common.pf.TestData.OTHERWISE_RESULT;
import static org.folio.common.pf.TestData.positiveInt;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.function.Function;
import java.util.function.Predicate;

import org.folio.test.extensions.TestStartLoggingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class PartialFunctionImplTest {

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();

  private Function<Integer, String> function = fPositiveIntMsg;
  private Predicate<Integer> predicate = positiveInt;
  private PartialFunction<Integer, String> pf = new PartialFunctionImpl<>(predicate, function);


  public static Integer[] testData() {
    return intsOfEveryKind();
  }

  @Test
  void notCreateWithNPEIfPredicateIsNull() {
    assertThrows(NullPointerException.class, () -> new PartialFunctionImpl<>(null, function));
  }

  @Test
  void notCreateWithNPEIfFunctionIsNull() {
    assertThrows(NullPointerException.class, () -> new PartialFunctionImpl<>(predicate, null));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void isDefinedAtResultEqualToPredicateResult(Integer i) {
    boolean expected = predicate.test(i);
    assertThat(pf.isDefinedAt(i), is(expected));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void applySuccessfullyResultEqualToFunctionResult(Integer i) {
    assumeTrue(pf.isDefinedAt(i));

    String expected = function.apply(i);
    assertThat(pf.applySuccessfully(i), is(expected));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void appliedIfPartialFunctionDefined(Integer i) {
    assumeTrue(pf.isDefinedAt(i));

    assertThat(pf.apply(i), is(function.apply(i)));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void notAppliedIfPartialFunctionNotDefined(Integer i) {
    assumeFalse(pf.isDefinedAt(i));

    NotDefinedException thrown = assertThrows(NotDefinedException.class, () -> pf.apply(i));
    assertThat(thrown.getMessage(), containsString(i.toString()));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void applyOrElseDoesNotAcceptNullOtherwise(Integer i) {
    assertThrows(NullPointerException.class, () -> pf.applyOrElse(i, null));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void applyOrElseEqualToApplyIfPartialFunctionDefined(Integer i) {
    assumeTrue(pf.isDefinedAt(i));

    assertThat(pf.applyOrElse(i, FUNC_OTHERWISE), is(pf.apply(i)));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void applyOrElseFallbackToOtherwiseIfPartialFunctionDefined(Integer i) {
    assumeFalse(pf.isDefinedAt(i));

    assertThat(pf.applyOrElse(i, FUNC_OTHERWISE), is(OTHERWISE_RESULT));
  }
}
