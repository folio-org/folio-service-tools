package org.folio.db.exc.translation;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;

import org.folio.test.junit.TestStartLoggingRule;

public class DBExceptionTranslatorFactoryTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void instanceReturnsNotNullFactory() {
    DBExceptionTranslatorFactory factory = DBExceptionTranslatorFactory.instance();

    assertThat(factory, Matchers.notNullValue());
  }

  @Test
  public void createFailsIfNameIsNull() {
    thrown.expect(NullPointerException.class);

    DBExceptionTranslatorFactory.instance().create(null);
  }

  @Test
  public void createFailsIfNameIsUnknown() {
    String name = "SomeName";

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(name);

    DBExceptionTranslatorFactory.instance().create(name);
  }

  @Test
  public void createReturnsKnownTranslator() {
    DBExceptionTranslator trans = DBExceptionTranslatorFactory.instance().create("postgresql");

    assertThat(trans, Matchers.notNullValue());
  }
}
