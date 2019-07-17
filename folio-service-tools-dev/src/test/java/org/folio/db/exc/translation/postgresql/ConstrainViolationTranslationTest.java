package org.folio.db.exc.translation.postgresql;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import static org.folio.db.ErrorFactory.getCheckViolationErrorMap;
import static org.folio.db.ErrorFactory.getDataLengthMismatch;
import static org.folio.db.ErrorFactory.getDataTypeMismatchViolation;
import static org.folio.db.ErrorFactory.getErrorMapWithSqlStateNull;
import static org.folio.db.ErrorFactory.getExclusionViolationErrorMap;
import static org.folio.db.ErrorFactory.getForeignKeyErrorMap;
import static org.folio.db.ErrorFactory.getNotNullViolationErrorMap;
import static org.folio.db.ErrorFactory.getPrimaryKeyErrorMap;
import static org.folio.db.ErrorFactory.getUniqueViolationErrorMap;

import com.github.mauricio.async.db.postgresql.exceptions.GenericDatabaseException;
import com.github.mauricio.async.db.postgresql.messages.backend.ErrorMessage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import org.folio.common.pf.PartialFunction;
import org.folio.db.exc.Constraint;
import org.folio.db.exc.ConstraintViolationException;
import org.folio.db.exc.DatabaseException;
import org.folio.test.junit.TestStartLoggingRule;

public class ConstrainViolationTranslationTest {

  @Rule
  public TestRule startLogger = TestStartLoggingRule.instance();

  @Test
  public void shouldReturnDatabaseExceptionWithIntegrityConstraintViolationCode(){
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getUniqueViolationErrorMap()));
    final PartialFunction<GenericDatabaseException, DatabaseException> asPartial = ConstrainViolationTranslation.asPartial();
    final DatabaseException apply = asPartial.apply(exception);

    assertThat(apply.getSqlState(), equalTo(PSQLState.UNIQUE_VIOLATION.getCode()));
  }

  @Test
  public void shouldReturnTrueWhenExceptionIsDataException(){
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getUniqueViolationErrorMap()));
    final boolean isDataException = new ConstrainViolationTranslation.TPredicate().test(exception);

    assertTrue(isDataException);
  }

  @Test
  public void shouldReturnFalseWhenExceptionIsNotDataException(){
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getDataLengthMismatch()));
    final boolean isDataException = new ConstrainViolationTranslation.TPredicate().test(exception);

    assertFalse(isDataException);
  }

  @Test
  public void shouldReturnFalseWhenExceptionIsNull(){
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getErrorMapWithSqlStateNull()));
    final boolean isDataException = new ConstrainViolationTranslation.TPredicate().test(exception);

    assertFalse(isDataException);
  }

  @Test
  public void shouldReturnUniqueConstraintViolationExceptionWithSqlStateWhenExceptionIsDataException(){
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getUniqueViolationErrorMap()));
    final ConstraintViolationException resultException = new ConstrainViolationTranslation.TFunction().apply(exception);

    assertThat(resultException.getSqlState(), equalTo(PSQLState.UNIQUE_VIOLATION.getCode()));
    assertThat(resultException.getConstraintType(), equalTo(Constraint.Type.UNIQUE));
  }

  @Test
  public void shouldReturnNotNullConstraintViolationExceptionWithSqlStateWhenExceptionIsDataException(){
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getNotNullViolationErrorMap()));
    final ConstraintViolationException resultException = new ConstrainViolationTranslation.TFunction().apply(exception);

    assertThat(resultException.getSqlState(), equalTo(PSQLState.NOT_NULL_VIOLATION.getCode()));
    assertThat(resultException.getConstraintType(), equalTo(Constraint.Type.NOT_NULL));
  }

  @Test
  public void shouldReturnCheckConstraintViolationExceptionWithSqlStateWhenExceptionIsDataException(){
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getCheckViolationErrorMap()));
    final ConstraintViolationException resultException = new ConstrainViolationTranslation.TFunction().apply(exception);

    assertThat(resultException.getSqlState(), equalTo(PSQLState.CHECK_VIOLATION.getCode()));
    assertThat(resultException.getConstraintType(), equalTo(Constraint.Type.CHECK));
  }

  @Test
  public void shouldReturnPrimaryKeyConstraintViolationExceptionWithSqlStateWhenExceptionIsDataException(){
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getPrimaryKeyErrorMap()));
    final ConstraintViolationException resultException = new ConstrainViolationTranslation.TFunction().apply(exception);

    assertThat(resultException.getSqlState(), equalTo(PSQLState.UNIQUE_VIOLATION.getCode()));
    assertThat(resultException.getConstraintType(), equalTo(Constraint.Type.PRIMARY_KEY));
  }

  @Test
  public void shouldReturnForeignKeyConstraintViolationExceptionWithSqlStateWhenExceptionIsDataException(){
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getForeignKeyErrorMap()));
    final ConstraintViolationException resultException = new ConstrainViolationTranslation.TFunction().apply(exception);

    assertThat(resultException.getSqlState(), equalTo(PSQLState.FOREIGN_KEY_VIOLATION.getCode()));
    assertThat(resultException.getConstraintType(), equalTo(Constraint.Type.FOREIGN_KEY));
  }

  @Test
  public void shouldReturnOtherConstraintViolationWithSqlStateWhenExceptionIsDataException(){
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getExclusionViolationErrorMap()));
    final ConstraintViolationException resultException = new ConstrainViolationTranslation.TFunction().apply(exception);

    assertThat(resultException.getSqlState(), equalTo(PSQLState.EXCLUSION_VIOLATION.getCode()));
    assertThat(resultException.getConstraintType(), equalTo(Constraint.Type.OTHER));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionWhenReceivesNotIntegrityConstraintViolation(){
    GenericDatabaseException exception = new GenericDatabaseException(new ErrorMessage(getDataTypeMismatchViolation()));
    new ConstrainViolationTranslation.TFunction().apply(exception);
  }
}
