package org.folio.db.exc.translation.postgresql;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;

import static org.folio.db.ErrorConstants.INVALID_TEXT_REPRESENTATION_ERROR_CODE;
import static org.folio.db.ErrorFactory.getErrorMapWithColumnNull;
import static org.folio.db.ErrorFactory.getErrorMapWithDetailNull;
import static org.folio.db.ErrorFactory.getErrorMapWithDetailOnly;
import static org.folio.db.ErrorFactory.getErrorMapWithFieldNameNull;
import static org.folio.db.ErrorFactory.getErrorMapWithMessage;
import static org.folio.db.ErrorFactory.getErrorMapWithMessageNull;
import static org.folio.db.ErrorFactory.getErrorMapWithPsql;
import static org.folio.db.ErrorFactory.getErrorMapWithPsqlStateNull;
import static org.folio.db.ErrorFactory.getErrorMapWithSchemaNull;
import static org.folio.db.ErrorFactory.getErrorMapWithSqlStateNull;
import static org.folio.db.ErrorFactory.getErrorMapWithSqlStateOnly;
import static org.folio.db.ErrorFactory.getErrorMapWithTableNull;
import static org.folio.rest.persist.PgExceptionUtil.createPgExceptionFromMap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import org.folio.test.junit.TestStartLoggingRule;

public class PgExceptionAdapterTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();

  @Test
  public void shouldReturnErrorWithNoFieldName() {
    PgExceptionAdapter adapter = new PgExceptionAdapter(createPgExceptionFromMap(getErrorMapWithFieldNameNull()));
    assertFalse(adapter.getName().isPresent());
  }

  @Test
  public void shouldReturnErrorWithMessage() {
    PgExceptionAdapter adapter = new PgExceptionAdapter(createPgExceptionFromMap(
      getErrorMapWithMessage("insert or update on table \"child\" violates foreign key constraint \"fk_parent\"")));
    assertTrue(adapter.getMessage().isPresent());
    assertThat(adapter.getMessage().get(), containsString("violates foreign key constraint"));
  }

  @Test
  public void shouldReturnErrorWithNoMessage() {
    PgExceptionAdapter adapter = new PgExceptionAdapter(createPgExceptionFromMap(getErrorMapWithMessageNull()));
    assertFalse(adapter.getMessage().isPresent());
  }

  @Test
  public void shouldReturnErrorDetail() {
    PgExceptionAdapter adapter = new PgExceptionAdapter(createPgExceptionFromMap(
      getErrorMapWithDetailOnly("Key (parent_id1, parent_id2)=(22222, 813205855) is not a present in table \"parent\"")));
    assertTrue(adapter.getDetailedMessage().isPresent());
    assertThat(adapter.getDetailedMessage().get(),
      equalTo("Key (parent_id1, parent_id2)=(22222, 813205855) is not a present in table \"parent\""));
  }

  @Test
  public void shouldReturnErrorWithNoDetail() {
    PgExceptionAdapter adapter = new PgExceptionAdapter(createPgExceptionFromMap(getErrorMapWithDetailNull()));
    assertFalse(adapter.getDetailedMessage().isPresent());
  }

  @Test
  public void shouldReturnErrorSqlStateOnly() {
    PgExceptionAdapter adapter =
      new PgExceptionAdapter(createPgExceptionFromMap(getErrorMapWithSqlStateOnly(INVALID_TEXT_REPRESENTATION_ERROR_CODE)));
    assertTrue(adapter.getSQLState().isPresent());
    assertThat(adapter.getSQLState().get(), equalTo(INVALID_TEXT_REPRESENTATION_ERROR_CODE));
  }

  @Test
  public void shouldReturnErrorWithNoSqlState() {
    PgExceptionAdapter adapter = new PgExceptionAdapter(createPgExceptionFromMap(getErrorMapWithSqlStateNull()));
    assertFalse(adapter.getSQLState().isPresent());
  }

  @Test
  public void shouldReturnErrorWithPsqlState() {
    PgExceptionAdapter adapter =
      new PgExceptionAdapter(createPgExceptionFromMap(getErrorMapWithPsql(INVALID_TEXT_REPRESENTATION_ERROR_CODE)));
    assertTrue(adapter.getPSQLState().isPresent());
    assertThat(adapter.getPSQLState().get(), equalTo(PSQLState.INVALID_TEXT_REPRESENTATION));
  }

  @Test
  public void shouldReturnNullPsqlStateWhenInvalidState() {
    PgExceptionAdapter adapter = new PgExceptionAdapter(createPgExceptionFromMap(getErrorMapWithPsql("45454545454545")));
    assertFalse(adapter.getPSQLState().isPresent());
  }

  @Test
  public void shouldReturnNullPsqlStateWhenNoMappingFound() {
    PgExceptionAdapter adapter = new PgExceptionAdapter(createPgExceptionFromMap(getErrorMapWithPsqlStateNull()));
    assertFalse(adapter.getPSQLState().isPresent());
  }

  @Test
  public void shouldReturnErrorWithNoSchema() {
    PgExceptionAdapter adapter = new PgExceptionAdapter(createPgExceptionFromMap(getErrorMapWithSchemaNull()));
    assertFalse(adapter.getSchema().isPresent());
  }

  @Test
  public void shouldReturnErrorWithNoTable() {
    PgExceptionAdapter adapter = new PgExceptionAdapter(createPgExceptionFromMap(getErrorMapWithTableNull()));
    assertFalse(adapter.getTable().isPresent());
  }

  @Test
  public void shouldReturnErrorWithNoColumn() {
    PgExceptionAdapter adapter = new PgExceptionAdapter(createPgExceptionFromMap(getErrorMapWithColumnNull()));
    assertFalse(adapter.getColumn().isPresent());
  }
}
