package org.folio.db.exc.translation.postgresql;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.github.mauricio.async.db.postgresql.messages.backend.ErrorMessage;
import org.apache.commons.collections4.IterableGet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import org.folio.db.ErrorFactory;
import org.folio.test.junit.TestStartLoggingRule;

public class InvalidValueParserTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();


  @Test
  public void parsesToSingleValue() {
    ErrorMessageAdapter em = new ErrorMessageAdapter(new ErrorMessage(
      ErrorFactory.getErrorMapWithDetailOnly("Key (name)=(John) already exists")));

    IterableGet<String, String> values = new InvalidValueParser(em).parse();

    assertThat(values.size(), is(1));
    assertThat(values.containsKey("name"), is(true));
    assertThat(values.containsValue("John"), is(true));
  }

  @Test
  public void parsesToSeveralValues() {
    ErrorMessageAdapter em = new ErrorMessageAdapter(new ErrorMessage(ErrorFactory.getErrorMapWithDetailOnly(
        "Key (parent_id1, parent_id2)=(22222, 813205855) is not present in table \"parent\".")));

    IterableGet<String, String> values = new InvalidValueParser(em).parse();

    assertThat(values.size(), is(2));

    assertThat(values.containsKey("parent_id1"), is(true));
    assertThat(values.get("parent_id1"), is("22222"));

    assertThat(values.containsKey("parent_id2"), is(true));
    assertThat(values.get("parent_id2"), is("813205855"));
  }

  @Test
  public void trimExcessiveSpacesFromKeysValues() {
    ErrorMessageAdapter em = new ErrorMessageAdapter(new ErrorMessage(ErrorFactory.getErrorMapWithDetailOnly(
      "Key ( parent_id1  , parent_id2  ) = (  22222  , 813205855  ) is not present in table \"parent\".")));

    IterableGet<String, String> values = new InvalidValueParser(em).parse();

    assertThat(values.size(), is(2));

    assertThat(values.containsKey("parent_id1"), is(true));
    assertThat(values.get("parent_id1"), is("22222"));

    assertThat(values.containsKey("parent_id2"), is(true));
    assertThat(values.get("parent_id2"), is("813205855"));
  }

  @Test
  public void parsesToNoValuesIfDetailIsEmpty() {
    ErrorMessageAdapter em = new ErrorMessageAdapter(new ErrorMessage(ErrorFactory.getErrorMapWithDetailNull()));

    IterableGet<String, String> values = new InvalidValueParser(em).parse();

    assertThat(values.isEmpty(), is(true));
  }

  @Test
  public void parsesToNoValuesIfPatternDoesntMatch() {
    ErrorMessageAdapter em = new ErrorMessageAdapter(new ErrorMessage(ErrorFactory.getErrorMapWithDetailOnly(
      "Failing row contains (1697635108, 858317485, null, 4670207833.23).")));

    IterableGet<String, String> values = new InvalidValueParser(em).parse();

    assertThat(values.isEmpty(), is(true));
  }
}
