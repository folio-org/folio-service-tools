package org.folio.db.exc.translation.postgresql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import static org.folio.db.ErrorFactory.getErrorMapWithDetailOnly;
import static org.folio.rest.persist.PgExceptionUtil.createPgExceptionFromMap;

import org.apache.commons.collections4.IterableGet;
import org.folio.test.extensions.TestStartLoggingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.folio.db.ErrorFactory;

class InvalidValueParserTest {

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();


  @Test
  void parsesToSingleValue() {
    PgExceptionAdapter em = new PgExceptionAdapter(createPgExceptionFromMap(
      getErrorMapWithDetailOnly("Key (name)=(John) already exists")));

    IterableGet<String, String> values = new InvalidValueParser(em).parse();

    assertThat(values.size(), is(1));
    assertThat(values.containsKey("name"), is(true));
    assertThat(values.containsValue("John"), is(true));
  }

  @Test
  void parsesToSeveralValues() {
    PgExceptionAdapter em = new PgExceptionAdapter(createPgExceptionFromMap(getErrorMapWithDetailOnly(
      "Key (parent_id1, parent_id2)=(22222, 813205855) is not present in table \"parent\".")));

    IterableGet<String, String> values = new InvalidValueParser(em).parse();

    assertThat(values.size(), is(2));

    assertThat(values.containsKey("parent_id1"), is(true));
    assertThat(values.get("parent_id1"), is("22222"));

    assertThat(values.containsKey("parent_id2"), is(true));
    assertThat(values.get("parent_id2"), is("813205855"));
  }

  @Test
  void trimExcessiveSpacesFromKeysValues() {
    PgExceptionAdapter em = new PgExceptionAdapter(createPgExceptionFromMap(getErrorMapWithDetailOnly(
      "Key ( parent_id1  , parent_id2  ) = (  22222  , 813205855  ) is not present in table \"parent\".")));

    IterableGet<String, String> values = new InvalidValueParser(em).parse();

    assertThat(values.size(), is(2));

    assertThat(values.containsKey("parent_id1"), is(true));
    assertThat(values.get("parent_id1"), is("22222"));

    assertThat(values.containsKey("parent_id2"), is(true));
    assertThat(values.get("parent_id2"), is("813205855"));
  }

  @Test
  void parsesToNoValuesIfDetailIsEmpty() {
    PgExceptionAdapter em = new PgExceptionAdapter(createPgExceptionFromMap(ErrorFactory.getErrorMapWithDetailNull()));

    IterableGet<String, String> values = new InvalidValueParser(em).parse();

    assertThat(values.isEmpty(), is(true));
  }

  @Test
  void parsesToNoValuesIfPatternDoesntMatch() {
    PgExceptionAdapter em = new PgExceptionAdapter(createPgExceptionFromMap(getErrorMapWithDetailOnly(
      "Failing row contains (1697635108, 858317485, null, 4670207833.23).")));

    IterableGet<String, String> values = new InvalidValueParser(em).parse();

    assertThat(values.isEmpty(), is(true));
  }
}
