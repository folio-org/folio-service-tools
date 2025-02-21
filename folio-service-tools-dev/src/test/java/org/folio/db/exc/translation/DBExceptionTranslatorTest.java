package org.folio.db.exc.translation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.vertx.core.Future;
import org.folio.test.extensions.TestStartLoggingExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.folio.db.exc.DatabaseException;

class DBExceptionTranslatorTest {

  private static final Throwable INCOMING_EXC = new Exception("Incoming");
  private static final DatabaseException TRANSLATED_EXC = new DatabaseException("Translated");

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();

  private TestTranslator translator;

  @BeforeEach
  void setUp() {
    translator = new TestTranslator(true, TRANSLATED_EXC);
  }

  @Test
  void translateFailsIfNotAcceptable() {
    translator.setAcceptable(false);
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> translator.translate(INCOMING_EXC));
    assertThat(thrown.getMessage(), containsString("Exception is not acceptable and cannot be translated"));
  }

  @Test
  void translateProcessesExceptionIfAcceptable() {
    DatabaseException exc = translator.translate(INCOMING_EXC);

    assertThat(exc, sameInstance(TRANSLATED_EXC));
  }

  @Test
  void translateOrPassByReturnsFutureWithTranslatedIfAcceptable() {
    Future<Object> future = translator.translateOrPassBy().apply(INCOMING_EXC);

    assertThat(future.failed(), is(true));
    assertThat(future.cause(), sameInstance(TRANSLATED_EXC));
  }

  @Test
  void translateOrPassByReturnsFutureWithIncomingIfNotAcceptable() {
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
