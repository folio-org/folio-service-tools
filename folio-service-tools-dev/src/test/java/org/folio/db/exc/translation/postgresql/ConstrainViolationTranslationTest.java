package org.folio.db.exc.translation.postgresql;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.folio.db.ErrorConstants.CHILD_TABLE_NAME;
import static org.folio.db.ErrorFactory.getCheckViolationErrorMap;
import static org.folio.db.ErrorFactory.getDataLengthMismatch;
import static org.folio.db.ErrorFactory.getDataTypeMismatchViolation;
import static org.folio.db.ErrorFactory.getErrorMapWithSqlStateNull;
import static org.folio.db.ErrorFactory.getExclusionViolationErrorMap;
import static org.folio.db.ErrorFactory.getForeignKeyErrorMap;
import static org.folio.db.ErrorFactory.getNotNullViolationErrorMap;
import static org.folio.db.ErrorFactory.getPrimaryKeyErrorMap;
import static org.folio.db.ErrorFactory.getUniqueViolationErrorMap;
import static org.folio.rest.persist.PgExceptionUtil.createPgExceptionFromMap;

import io.vertx.pgclient.PgException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import org.folio.db.exc.Constraint;
import org.folio.db.exc.ConstraintViolationException;
import org.folio.db.exc.DatabaseException;
import org.folio.test.junit.TestStartLoggingRule;

public class ConstrainViolationTranslationTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();

  @Test
  public void shouldReturnDatabaseExceptionWithIntegrityConstraintViolationCode() {
    PgException exception = createPgExceptionFromMap((getUniqueViolationErrorMap()));
    DatabaseException apply = ConstrainViolationTranslation.asPartial().apply(exception);

    assertThat(apply.getSqlState(), equalTo(PSQLState.UNIQUE_VIOLATION.getCode()));
  }

  @Test
  public void shouldReturnTrueWhenExceptionIsDataException() {
    PgException exception = createPgExceptionFromMap((getUniqueViolationErrorMap()));
    boolean isDataException = new ConstrainViolationTranslation.TPredicate().test(exception);

    assertTrue(isDataException);
  }

  @Test
  public void shouldReturnFalseWhenExceptionIsNotDataException() {
    PgException exception = createPgExceptionFromMap((getDataLengthMismatch()));
    boolean isDataException = new ConstrainViolationTranslation.TPredicate().test(exception);

    assertFalse(isDataException);
  }

  @Test
  public void shouldReturnFalseWhenExceptionIsNull() {
    PgException exception = createPgExceptionFromMap((getErrorMapWithSqlStateNull()));
    boolean isDataException = new ConstrainViolationTranslation.TPredicate().test(exception);

    assertFalse(isDataException);
  }

  @Test
  public void shouldReturnUniqueConstraintViolationExceptionWithSqlStateWhenExceptionIsDataException() {
    PgException exception = createPgExceptionFromMap((getUniqueViolationErrorMap()));
    ConstraintViolationException resultException = new ConstrainViolationTranslation.TFunction().apply(exception);

    assertThat(resultException.getSqlState(), equalTo(PSQLState.UNIQUE_VIOLATION.getCode()));
    assertThat(resultException.getConstraintType(), equalTo(Constraint.Type.UNIQUE));
  }

  @Test
  public void shouldReturnNotNullConstraintViolationExceptionWithSqlStateWhenExceptionIsDataException() {
    PgException exception = createPgExceptionFromMap((getNotNullViolationErrorMap()));
    ConstraintViolationException resultException = new ConstrainViolationTranslation.TFunction().apply(exception);

    assertThat(resultException.getSqlState(), equalTo(PSQLState.NOT_NULL_VIOLATION.getCode()));
    assertThat(resultException.getConstraintType(), equalTo(Constraint.Type.NOT_NULL));
  }

  @Test
  public void shouldReturnCheckConstraintViolationExceptionWithSqlStateWhenExceptionIsDataException() {
    PgException exception = createPgExceptionFromMap((getCheckViolationErrorMap()));
    ConstraintViolationException resultException = new ConstrainViolationTranslation.TFunction().apply(exception);

    assertThat(resultException.getSqlState(), equalTo(PSQLState.CHECK_VIOLATION.getCode()));
    assertThat(resultException.getConstraintType(), equalTo(Constraint.Type.CHECK));
  }

  @Test
  public void shouldReturnPrimaryKeyConstraintViolationExceptionWithSqlStateWhenExceptionIsDataException() {
    PgException exception = createPgExceptionFromMap((getPrimaryKeyErrorMap()));
    ConstraintViolationException resultException = new ConstrainViolationTranslation.TFunction().apply(exception);

    assertThat(resultException.getSqlState(), equalTo(PSQLState.UNIQUE_VIOLATION.getCode()));
    assertThat(resultException.getConstraintType(), equalTo(Constraint.Type.PRIMARY_KEY));
  }

  @Test
  public void shouldReturnForeignKeyConstraintViolationExceptionWithAllFieldsWhenExceptionIsDataException() {
    PgException exception = createPgExceptionFromMap((getForeignKeyErrorMap()));
    ConstraintViolationException resultException = new ConstrainViolationTranslation.TFunction().apply(exception);

    assertThat(resultException.getSqlState(), equalTo(PSQLState.FOREIGN_KEY_VIOLATION.getCode()));
    Constraint constraint = resultException.getConstraint();
    assertThat(constraint.getType(), equalTo(Constraint.Type.FOREIGN_KEY));
    assertThat(constraint.getName(), equalTo("fk_parent"));
    assertThat(constraint.getTable(), equalTo(CHILD_TABLE_NAME));
    assertThat(resultException.getDetailedMessage(), containsString("is not a present in table"));
  }

  @Test
  public void shouldReturnOtherConstraintViolationWithSqlStateWhenExceptionIsDataException() {
    PgException exception = createPgExceptionFromMap((getExclusionViolationErrorMap()));
    ConstraintViolationException resultException = new ConstrainViolationTranslation.TFunction().apply(exception);

    assertThat(resultException.getSqlState(), equalTo(PSQLState.EXCLUSION_VIOLATION.getCode()));
    assertThat(resultException.getConstraintType(), equalTo(Constraint.Type.OTHER));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionWhenReceivesNotIntegrityConstraintViolation() {
    PgException exception = createPgExceptionFromMap((getDataTypeMismatchViolation()));
    new ConstrainViolationTranslation.TFunction().apply(exception);
  }

  @Test
  public void shouldReturnConstraintViolationExceptionWithInvalidFieldsPopulated() {
    // detail: "Key (id1, id2)=(22222, 813205855) already exists"
    PgException exception = createPgExceptionFromMap((getPrimaryKeyErrorMap()));
    ConstraintViolationException resultException = new ConstrainViolationTranslation.TFunction().apply(exception);

    assertThat(resultException.getInvalidValues(), notNullValue());
    assertThat(resultException.getInvalidValues().size(), is(2));
    assertThat(resultException.getInvalidValues(), hasEntry("id1", "22222"));
    assertThat(resultException.getInvalidValues(), hasEntry("id2", "813205855"));
  }

  @Test
  public void shouldReturnConstraintViolationExceptionWithColumnNamesPopulatedFromDetail() {
    // detail: "Key (id1, id2)=(22222, 813205855) already exists"
    PgException exception = createPgExceptionFromMap((getPrimaryKeyErrorMap()));
    ConstraintViolationException resultException = new ConstrainViolationTranslation.TFunction().apply(exception);

    Constraint cons = resultException.getConstraint();
    assertThat(cons.getColumns(), containsInAnyOrder("id1", "id2"));
  }

  @Test
  public void shouldReturnConstraintViolationExceptionWithSingleColumnPopulatedFromColumnField() {
    PgException exception = createPgExceptionFromMap((getNotNullViolationErrorMap()));
    ConstraintViolationException resultException = new ConstrainViolationTranslation.TFunction().apply(exception);

    Constraint cons = resultException.getConstraint();
    assertThat(cons.getColumns(), containsInAnyOrder("name"));
  }
}
