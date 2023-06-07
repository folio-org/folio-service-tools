package org.folio.common.pf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.folio.common.pf.TestData.fOtherwise;
import static org.folio.common.pf.TestData.intsOfEveryKind;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.folio.test.extensions.TestStartLoggingExtension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class EmptyTest {

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();

  private final Empty<Integer, String> empty = new Empty<>();

  public static Integer[] testData() {
    return intsOfEveryKind();
  }

  @ParameterizedTest
  @MethodSource("testData")
  void isDefinedAtIsAlwaysFalse(Integer i) {
    assertFalse(empty.isDefinedAt(i));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void applyAlwaysFailed(Integer i) {
    NotDefinedException thrown = assertThrows(NotDefinedException.class, () -> empty.apply(i));
    assertThat(thrown.getMessage(), containsString(i.toString()));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void applyOrElseAlwaysReturnsOtherwiseResult(Integer i) {
    assertThat(empty.applyOrElse(i, fOtherwise), is(fOtherwise.apply(i)));
  }
}
