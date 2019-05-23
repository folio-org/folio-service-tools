package org.folio.common.pf;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

import static org.folio.common.pf.TestData.fPositiveIntMsg;
import static org.folio.common.pf.TestData.intsOfEveryKind;
import static org.folio.common.pf.TestData.positiveInt;

import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import org.folio.common.log.LogHandler;
import org.folio.test.junit.TestStartLoggingRule;

@RunWith(Theories.class)
public class LoggedApplicationTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Function<Integer, String> function = fPositiveIntMsg;
  private Predicate<Integer> predicate = positiveInt;
  private PartialFunction<Integer, String> pf = new PartialFunctionImpl<>(predicate, function);

  private MockLogHandler<Integer> handler = new MockLogHandler<>();

  private LoggedApplication<Integer, String> logged = new LoggedApplication<>(pf, handler);


  @DataPoints
  public static Integer[] testData() {
    return intsOfEveryKind();
  }

  @Test
  public void notCreateWithNPEIfPartialFuncIsNull() {
    thrown.expect(NullPointerException.class);

    new LoggedApplication<>(null, handler);
  }

  @Test
  public void notCreateWithNPEIfLogHandlerIsNull() {
    thrown.expect(NullPointerException.class);

    new LoggedApplication<>(pf, null);
  }

  @Theory
  public void isDefinedAtEqualesToPartialFuncOne(Integer i) {
    assertThat(logged.isDefinedAt(i), is(pf.isDefinedAt(i)));
  }

  @Theory
  public void logHandlerCalledIfPartialFuncApplied(Integer i) {
    assumeThat(logged.isDefinedAt(i), is(true));

    String result = logged.apply(i);

    assertThat(result, is(pf.apply(i)));
    assertTrue(handler.logCalled);
  }

  @Theory
  public void logHandlerCalledIfPartialFuncNotDefined(Integer i) {
    assumeThat(logged.isDefinedAt(i), is(false));

    try {
      logged.apply(i);
      fail();
    } catch (NotDefinedException e) {
      assertTrue(handler.logCalled);
    }
  }

  @Theory
  public void logHandlerNotCalledDuringApplySuccessfully(Integer i) {
    assumeThat(logged.isDefinedAt(i), is(true)); // just to make sure the function doesn't fail

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
