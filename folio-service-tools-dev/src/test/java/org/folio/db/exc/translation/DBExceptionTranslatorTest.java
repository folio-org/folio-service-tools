package org.folio.db.exc.translation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import io.vertx.core.Future;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;

import org.folio.db.exc.DatabaseException;
import org.folio.test.junit.TestStartLoggingRule;

public class DBExceptionTranslatorTest {

  private static final Throwable INCOMING_EXC = new Exception("Incoming");
  private static final DatabaseException TRANSLATED_EXC = new DatabaseException("Translated");

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private TestTranslator translator;

  @Before
  public void setUp() {
    translator = new TestTranslator(true, TRANSLATED_EXC);
  }

  @Test
  public void translateFailsIfNotAcceptable() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Exception is not acceptable and cannot be translated");

    translator.setAcceptable(false);

    translator.translate(INCOMING_EXC);
  }

  @Test
  public void translateProcessesExceptionIfAcceptable() {
    DatabaseException exc = translator.translate(INCOMING_EXC);

    assertThat(exc, sameInstance(TRANSLATED_EXC));
  }

  @Test
  public void translateOrPassByReturnsFutureWithTranslatedIfAcceptable() {
    Future<Object> future = translator.translateOrPassBy().apply(INCOMING_EXC);

    assertThat(future.failed(), is(true));
    assertThat(future.cause(), sameInstance(TRANSLATED_EXC));
  }

  @Test
  public void translateOrPassByReturnsFutureWithIncomingIfNotAcceptable() {
    translator.setAcceptable(false);

    Future<Object> future = translator.translateOrPassBy().apply(INCOMING_EXC);

    assertThat(future.failed(), is(true));
    assertThat(future.cause(), sameInstance(INCOMING_EXC));
  }

  private static class TestTranslator extends DBExceptionTranslator {

    private boolean acceptable;
    private DatabaseException translated;

    TestTranslator(boolean acceptable, DatabaseException translated) {
      this.acceptable = acceptable;
      this.translated = translated;
    }

    void setAcceptable(boolean acceptable) {
      this.acceptable = acceptable;
    }

    void setTranslated(DatabaseException translated) {
      this.translated = translated;
    }

    @Override
    public boolean acceptable(Throwable exc) {
      return acceptable;
    }

    @Override
    protected DatabaseException doTranslation(Throwable exc) {
      return translated;
    }
  }
}
