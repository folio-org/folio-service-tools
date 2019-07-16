package org.folio.db.exc.translation.postgresql;

import static org.folio.db.ErrorConstants.INVALID_TEXT_REPRESENTATION_ERROR_CODE;
import static org.folio.db.ErrorConstants.SCHEMA_NAME;
import static org.folio.db.ErrorConstants.TABLE_NAME;
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
    org.junit.Assert.assertTrue(adapter.getName().isPresent());
    org.junit.Assert.assertThat(adapter.getName().get(), equalTo("fk_parent"));
  }

  @Test
  public void shouldReturnErrorWithNoFieldName() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithFieldNameNull()));
    org.junit.Assert.assertFalse(adapter.getName().isPresent());
  }

  @Test
  public void shouldReturnErrorWithMessage() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithMessage("insert or update on table \"child\" violates foreign key constraint \"fk_parent\"")));
    org.junit.Assert.assertTrue(adapter.getMessage().isPresent());
    org.junit.Assert.assertThat(adapter.getMessage().get(), containsString("violates foreign key constraint"));
  }

  @Test
  public void shouldReturnErrorWithNoMessage() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithMessageNull()));
    org.junit.Assert.assertFalse(adapter.getMessage().isPresent());
  }

  @Test
  public void shouldReturnErrorDetail() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithDetailOnly("Key (parent_id1, parent_id2)=(22222, 813205855) is not a present in table \"parent\"")));
    org.junit.Assert.assertTrue(adapter.getDetailedMessage().isPresent());
    org.junit.Assert.assertThat(adapter.getDetailedMessage().get(), equalTo("Key (parent_id1, parent_id2)=(22222, 813205855) is not a present in table \"parent\""));
  }

  @Test
  public void shouldReturnErrorWithNoDetail() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithDetailNull()));
    org.junit.Assert.assertFalse(adapter.getDetailedMessage().isPresent());
  }

  @Test
  public void shouldReturnErrorSqlStateOnly() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithSqlStateOnly(INVALID_TEXT_REPRESENTATION_ERROR_CODE)));
    org.junit.Assert.assertTrue(adapter.getSQLState().isPresent());
    org.junit.Assert.assertThat(adapter.getSQLState().get(), equalTo(INVALID_TEXT_REPRESENTATION_ERROR_CODE));
  }

  @Test
  public void shouldReturnErrorWithNoSqlState() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithSqlStateNull()));
    org.junit.Assert.assertFalse(adapter.getSQLState().isPresent());
  }

  @Test
  public void shouldReturnErrorWithPsqlState() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithPsql(INVALID_TEXT_REPRESENTATION_ERROR_CODE)));
    org.junit.Assert.assertTrue(adapter.getPSQLState().isPresent());
    org.junit.Assert.assertThat(adapter.getPSQLState().get(), equalTo(PSQLState.INVALID_TEXT_REPRESENTATION));
  }

  @Test
  public void shouldReturnNullPsqlStateWhenInvalidState() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithPsql("45454545454545")));
    org.junit.Assert.assertFalse(adapter.getPSQLState().isPresent());
  }

  @Test
  public void shouldReturnNullPsqlStateWhenNoMappingFound() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithPsqlStateNull()));
    org.junit.Assert.assertFalse(adapter.getPSQLState().isPresent());
  }

  @Test
  public void shouldReturnErrorWithSchema() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithSchema(SCHEMA_NAME)));
    org.junit.Assert.assertTrue(adapter.getSchema().isPresent());
    org.junit.Assert.assertThat(adapter.getSchema().get(), equalTo(SCHEMA_NAME));
  }

  @Test
  public void shouldReturnErrorWithNoSchema() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithSchemaNull()));
    org.junit.Assert.assertFalse(adapter.getSchema().isPresent());
  }

  @Test
  public void shouldReturnErrorWithTable() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithTable(TABLE_NAME)));
    org.junit.Assert.assertTrue(adapter.getTable().isPresent());
    org.junit.Assert.assertThat(adapter.getTable().get(), equalTo(TABLE_NAME));
  }

  @Test
  public void shouldReturnErrorWithNoTable() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithTableNull()));
    org.junit.Assert.assertFalse(adapter.getTable().isPresent());
  }

  @Test
  public void shouldReturnErrorWithColumn() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithColumn("name")));
    org.junit.Assert.assertTrue(adapter.getColumn().isPresent());
    org.junit.Assert.assertThat(adapter.getColumn().get(), equalTo("name"));
  }

  @Test
  public void shouldReturnErrorWithNoColumn() {
    ErrorMessageAdapter adapter = new ErrorMessageAdapter(new ErrorMessage(getErrorMapWithColumnNull()));
    org.junit.Assert.assertFalse(adapter.getColumn().isPresent());
  }
}
