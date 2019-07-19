package org.folio.db.exc.translation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.folio.db.exc.translation.postgresql.PostgreSQLExceptionTranslator;

public class DBExceptionTranslatorFactory {

  private static final DBExceptionTranslatorFactory factory = new DBExceptionTranslatorFactory();

  // simple registry of translators
  private Map<String, DBExceptionTranslator> dbeTranslators;


  private DBExceptionTranslatorFactory() {
    dbeTranslators = new HashMap<>();
    // no other translators exists at the moment, except for PostgreSQL one, so it's instantiated explicitly
    // other option is to have a property file with "name->translator"
    dbeTranslators.put("postgresql", new PostgreSQLExceptionTranslator());
  }

  public static DBExceptionTranslatorFactory instance() {
    return factory;
  }

  public DBExceptionTranslator create(String translatorName) {
    Objects.requireNonNull(translatorName);

    DBExceptionTranslator translator = dbeTranslators.get(translatorName.toLowerCase());

    if (translator == null) {
      throw new IllegalArgumentException("Database exception translator is not registered: name = " + translatorName);
    }

    return translator;
  }
}
