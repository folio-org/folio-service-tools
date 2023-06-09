package org.folio.common.pf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.folio.common.pf.TestData.fPositiveIntMsg;
import static org.folio.common.pf.TestData.intsOfEveryKind;
import static org.folio.common.pf.TestData.positiveInt;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.function.Function;
import java.util.function.Predicate;

import org.folio.test.extensions.TestStartLoggingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.folio.common.log.LogHandler;

class LoggedApplicationTest {

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();

  private Function<Integer, String> function = fPositiveIntMsg;
  private Predicate<Integer> predicate = positiveInt;
  private PartialFunction<Integer, String> pf = new PartialFunctionImpl<>(predicate, function);

  private MockLogHandler<Integer> handler = new MockLogHandler<>();

  private LoggedApplication<Integer, String> logged = new LoggedApplication<>(pf, handler);


  public static Integer[] testData() {
    return intsOfEveryKind();
  }

  @Test
  void notCreateWithNPEIfPartialFuncIsNull() {
    assertThrows(NullPointerException.class, () -> new LoggedApplication<>(null, handler));
  }

  @Test
  void notCreateWithNPEIfLogHandlerIsNull() {
    assertThrows(NullPointerException.class, () -> new LoggedApplication<>(pf, null));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void isDefinedAtEqualesToPartialFuncOne(Integer i) {
    assertThat(logged.isDefinedAt(i), is(pf.isDefinedAt(i)));
  }

  @ParameterizedTest
  @MethodSource("testData")
  void logHandlerCalledIfPartialFuncApplied(Integer i) {
    assumeTrue(logged.isDefinedAt(i));

    assertThat(logged.apply(i), is(pf.apply(i)));
    assertTrue(handler.logCalled);
  }

  @ParameterizedTest
  @MethodSource("testData")
  void logHandlerCalledIfPartialFuncNotDefined(Integer i) {
    assumeFalse(logged.isDefinedAt(i));

    assertThrows(NotDefinedException.class, () -> logged.apply(i));
    assertTrue(handler.logCalled);
  }

  @ParameterizedTest
  @MethodSource("testData")
  void logHandlerNotCalledDuringApplySuccessfully(Integer i) {
    assumeTrue(logged.isDefinedAt(i)); // just to make sure the function doesn't fail

    logged.applySuccessfully(i);

    assertFalse(handler.logCalled);
  }

  private static class MockLogHandler<T> implements LogHandler<T> {

    boolean logCalled;

    @Override
    public void log(T t) {
      logCalled = true;
    }
  }
}
