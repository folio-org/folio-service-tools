package org.folio.db.exc.translation.postgresql;

import static junit.framework.TestCase.assertTrue;
import static org.folio.db.ErrorConstants.INVALID_TEXT_REPRESENTATION_ERROR_CODE;
import static org.folio.db.ErrorConstants.SCHEMA_NAME;
import static org.folio.db.ErrorConstants.CHILD_TABLE_NAME;
import static org.folio.db.ErrorFactory.getErrorMapWithColumn;
import static org.folio.db.ErrorFactory.getErrorMapWithColumnNull;
import static org.folio.db.ErrorFactory.getErrorMapWithDetailNull;
import static org.folio.db.ErrorFactory.getErrorMapWithDetailOnly;
import static org.folio.db.ErrorFactory.getErrorMapWithFieldNameNull;
import static org.folio.db.ErrorFactory.getErrorMapWithFieldNameOnly;
import static org.folio.db.ErrorFactory.getErrorMapWithMessage;
import static org.folio.db.ErrorFactory.getErrorMapWithMessageNull;
import static org.folio.db.ErrorFactory.getErrorMapWithPsql;
import static org.folio.db.ErrorFactory.getErrorMapWithPsqlStateNull;
import static org.folio.db.ErrorFactory.getErrorMapWithSchema;
import static org.folio.db.ErrorFactory.getErrorMapWithSchemaNull;
import static org.folio.db.ErrorFactory.getErrorMapWithSqlStateNull;
import static org.folio.db.ErrorFactory.getErrorMapWithSqlStateOnly;
import static org.folio.db.ErrorFactory.getErrorMapWithTable;
import static org.folio.db.ErrorFactory.getErrorMapWithTableNull;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.folio.test.junit.TestStartLoggingRule;
import org.junit.Rule;
import org.junit.Test;

import com.github.mauricio.async.db.postgresql.messages.backend.ErrorMessage;
import org.junit.rules.TestRule;

public class ErrorMessageAdapterTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();

  @Test
  public void shouldReturnErrorFieldName() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithFieldNameOnly("fk_parent")));
    assertTrue(adapter.getName().isPresent());
    assertThat(adapter.getName().get(), equalTo("fk_parent"));
  }

  @Test
  public void shouldReturnErrorWithNoFieldName() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithFieldNameNull()));
    assertFalse(adapter.getName().isPresent());
  }

  @Test
  public void shouldReturnErrorWithMessage() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithMessage("insert or update on table \"child\" violates foreign key constraint \"fk_parent\"")));
    assertTrue(adapter.getMessage().isPresent());
    assertThat(adapter.getMessage().get(), containsString("violates foreign key constraint"));
  }

  @Test
  public void shouldReturnErrorWithNoMessage() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithMessageNull()));
    assertFalse(adapter.getMessage().isPresent());
  }

  @Test
  public void shouldReturnErrorDetail() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithDetailOnly("Key (parent_id1, parent_id2)=(22222, 813205855) is not a present in table \"parent\"")));
    assertTrue(adapter.getDetailedMessage().isPresent());
    assertThat(adapter.getDetailedMessage().get(), equalTo("Key (parent_id1, parent_id2)=(22222, 813205855) is not a present in table \"parent\""));
  }

  @Test
  public void shouldReturnErrorWithNoDetail() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithDetailNull()));
    assertFalse(adapter.getDetailedMessage().isPresent());
  }

  @Test
  public void shouldReturnErrorSqlStateOnly() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithSqlStateOnly(INVALID_TEXT_REPRESENTATION_ERROR_CODE)));
    assertTrue(adapter.getSQLState().isPresent());
    assertThat(adapter.getSQLState().get(), equalTo(INVALID_TEXT_REPRESENTATION_ERROR_CODE));
  }

  @Test
  public void shouldReturnErrorWithNoSqlState() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithSqlStateNull()));
    assertFalse(adapter.getSQLState().isPresent());
  }

  @Test
  public void shouldReturnErrorWithPsqlState() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithPsql(INVALID_TEXT_REPRESENTATION_ERROR_CODE)));
    assertTrue(adapter.getPSQLState().isPresent());
    assertThat(adapter.getPSQLState().get(), equalTo(PSQLState.INVALID_TEXT_REPRESENTATION));
  }

  @Test
  public void shouldReturnNullPsqlStateWhenInvalidState() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithPsql("45454545454545")));
    assertFalse(adapter.getPSQLState().isPresent());
  }

  @Test
  public void shouldReturnNullPsqlStateWhenNoMappingFound() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithPsqlStateNull()));
    assertFalse(adapter.getPSQLState().isPresent());
  }

  @Test
  public void shouldReturnErrorWithSchema() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithSchema(SCHEMA_NAME)));
    assertTrue(adapter.getSchema().isPresent());
    assertThat(adapter.getSchema().get(), equalTo(SCHEMA_NAME));
  }

  @Test
  public void shouldReturnErrorWithNoSchema() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithSchemaNull()));
    assertFalse(adapter.getSchema().isPresent());
  }

  @Test
  public void shouldReturnErrorWithTable() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithTable(CHILD_TABLE_NAME)));
    assertTrue(adapter.getTable().isPresent());
    assertThat(adapter.getTable().get(), equalTo(CHILD_TABLE_NAME));
  }

  @Test
  public void shouldReturnErrorWithNoTable() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithTableNull()));
    assertFalse(adapter.getTable().isPresent());
  }

  @Test
  public void shouldReturnErrorWithColumn() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithColumn("name")));
    assertTrue(adapter.getColumn().isPresent());
    assertThat(adapter.getColumn().get(), equalTo("name"));
  }

  @Test
  public void shouldReturnErrorWithNoColumn() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithColumnNull()));
    assertFalse(adapter.getColumn().isPresent());
  }
}
