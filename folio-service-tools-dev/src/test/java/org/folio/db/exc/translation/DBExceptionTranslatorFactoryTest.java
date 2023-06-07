package org.folio.db.exc.translation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.folio.test.extensions.TestStartLoggingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class DBExceptionTranslatorFactoryTest {

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();

  @Test
  void instanceReturnsNotNullFactory() {
    DBExceptionTranslatorFactory factory = DBExceptionTranslatorFactory.instance();

    assertThat(factory, notNullValue());
  }

  @Test
  void createFailsIfNameIsNull() {
    DBExceptionTranslatorFactory factory = DBExceptionTranslatorFactory.instance();
    assertThrows(NullPointerException.class, () -> factory.create(null));
  }

  @Test
  void createFailsIfNameIsUnknown() {
    String name = "SomeName";
    DBExceptionTranslatorFactory factory = DBExceptionTranslatorFactory.instance();

    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> factory.create(name));
    assertThat(thrown.getMessage(), containsString(name));
  }

  @Test
  void createReturnsKnownTranslator() {
    DBExceptionTranslator trans = DBExceptionTranslatorFactory.instance().create("postgresql");

    assertThat(trans, notNullValue());
  }
}
