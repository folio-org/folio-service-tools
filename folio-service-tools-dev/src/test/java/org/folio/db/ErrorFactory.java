package org.folio.db;

import static org.folio.db.ErrorConstants.CHECK_VIOLATION_ERROR_CODE;
import static org.folio.db.ErrorConstants.DATATYPE_MISMATCH_ERROR_CODE;
import static org.folio.db.ErrorConstants.FOREIGN_KEY_VIOLATION_ERROR_CODE;
import static org.folio.db.ErrorConstants.INVALID_TEXT_REPRESENTATION_ERROR_CODE;
import static org.folio.db.ErrorConstants.NOT_NULL_VIOLATION_ERROR_CODE;
import static org.folio.db.ErrorConstants.SCHEMA_NAME;
import static org.folio.db.ErrorConstants.UNIQUE_VIOLATION_ERROR_CODE;

import scala.collection.immutable.Map;

public class ErrorFactory {

  private ErrorFactory() {}

 public  static Map<Object, String> getForeingKeyErrorMap(){
     return new ErrorBuilder()
       .setMessage("insert or update on table \"child\" violates foreign key constraint \"fk_parent\"")
       .setDetail("Key (parent_id1, parent_id2)=(22222, 813205855) is not a present in table \"parent\"")
       .setSchema(SCHEMA_NAME)
       .setTable("child")
       .setFieldName("fk_parent")
       .setLine("3321")
       .setFile("ri.triggers.c")
       .setSqlState(FOREIGN_KEY_VIOLATION_ERROR_CODE)
       .setRoutine("ri_ReportViolation")
       .setErrorType("ERROR")
       .setSeverity("ERROR").build();
 }

  public static Map<Object, String> getPrimaryKeyErrorMap(){
    return new ErrorBuilder()
      .setMessage("duplicate key value violates unique constraint \"pk_parent\"")
      .setDetail("Key (id1, id2)=(22222, 813205855) already exists")
      .setSchema(SCHEMA_NAME)
      .setTable("parent")
      .setFieldName("pk_parent")
      .setLine("434")
      .setFile("nbtinsert.c")
      .setSqlState(UNIQUE_VIOLATION_ERROR_CODE)
      .setRoutine("_bt_check_unique")
      .setErrorType("ERROR")
      .setSeverity("ERROR").build();
  }

  public static Map<Object, String> getUUIDErrorMap(){
    return new ErrorBuilder()
      .setMessage("invalid input syntax for type uuid: \"INVALID\"")
      .setLine("137")
      .setFile("uuid.c")
      .setSqlState(INVALID_TEXT_REPRESENTATION_ERROR_CODE)
      .setRoutine("string_to_uuid")
      .setErrorType("ERROR")
      .setSeverity("ERROR").build();
  }

  public static Map<Object, String> getNotNullViolationErrorMap(){
    return new ErrorBuilder()
      .setMessage("null value in column \"name\" violates not-null constraint")
      .setDetail("Failing row constraints (1697635108, 858317485, null, 4670207833.23)")
      .setSchema(SCHEMA_NAME)
      .setTable("parent")
      .setLine("2008")
      .setFile("execMain.c")
      .setFieldColumn("name")
      .setSqlState(NOT_NULL_VIOLATION_ERROR_CODE)
      .setRoutine("ExecConstraints")
      .setErrorType("ERROR")
      .setSeverity("ERROR").build();
  }

  public static Map<Object, String> getUniqueViolationErrorMap(){
    return new ErrorBuilder()
      .setMessage("duplicate key value violates unique constraint \"unq_name\"")
      .setDetail("Key (name)=(eOMtThyhVNLWUZNRcBaQKxl) already exists")
      .setSchema(SCHEMA_NAME)
      .setTable("parent")
      .setLine("434")
      .setFile("nbtinsert.c")
      .setFieldColumn("name")
      .setSqlState(UNIQUE_VIOLATION_ERROR_CODE)
      .setRoutine("_bt_check_unique")
      .setErrorType("ERROR")
      .setSeverity("ERROR").build();
  }

  public static Map<Object, String> getCheckViolationErrorMap(){
    return new ErrorBuilder()
      .setMessage("new ow for relation \"parent\" violates check constraint")
      .setDetail("Failing row contains (1704747953, 1372598141, eOMtThyhVNLWUZNRcBaQKxl, -1.00)")
      .setSchema(SCHEMA_NAME)
      .setTable("parent")
      .setLine("2055")
      .setFile("execMain.c")
      .setFieldName("positive_value")
      .setSqlState(CHECK_VIOLATION_ERROR_CODE)
      .setRoutine("ExecConstraints")
      .setErrorType("ERROR")
      .setSeverity("ERROR").build();
  }

  public static Map<Object, String> get(){
    return new ErrorBuilder()
      .setMessage("column \"addresses\" is of type json but expression is of type character varying")
      .setSchema(SCHEMA_NAME)
      .setTable("parent")
      .setLine("510")
      .setFile("parse_target.c")
      .setSqlState(DATATYPE_MISMATCH_ERROR_CODE)
      .setRoutine("transformAssignedExpr")
      .setErrorType("ERROR")
      .setSeverity("ERROR").build();
  }

  public static Map<Object, String> getErrorMapWithFieldNameOnly(String name){
    return new ErrorBuilder().setFieldName(name).build();
  }

  public static Map<Object, String> getErrorMapWithFieldNameNull(){
    return new ErrorBuilder().setFieldName(null).build();
  }

  public static Map<Object, String> getErrorMapWithDetailOnly(String detail){
    return new ErrorBuilder().setDetail(detail).build();
  }

  public static Map<Object, String> getErrorMapWithDetailNull(){
    return new ErrorBuilder().setDetail(null).build();
  }

  public static Map<Object, String> getErrorMapWithSqlStateOnly(String sqlState){
    return new ErrorBuilder().setSqlState(sqlState).build();
  }

  public static Map<Object, String> getErrorMapWithSqlStateNull(){
    return new ErrorBuilder().setSqlState(null).build();
  }

  public static Map<Object, String> getErrorMapWithPsql(String psql){
    return new ErrorBuilder().setSqlState(psql).build();
  }

  public static Map<Object, String> getErrorMapWithPsqlStateNull(){
    return new ErrorBuilder().setSqlState(null).build();
  }

  public static Map<Object, String> getErrorMapWithSchema(String schema){
    return new ErrorBuilder().setSchema(schema).build();
  }

  public static Map<Object, String> getErrorMapWithSchemaNull(){
    return new ErrorBuilder().setSchema(null).build();
  }

  public static Map<Object, String> getErrorMapWithTable(String table){
    return new ErrorBuilder().setTable(table).build();
  }

  public static Map<Object, String> getErrorMapWithTableNull(){
    return new ErrorBuilder().setTable(null).build();
  }

  public static Map<Object, String> getErrorMapWithMessage(String message){
    return new ErrorBuilder().setMessage(message).build();
  }

  public static Map<Object, String> getErrorMapWithMessageNull(){
    return new ErrorBuilder().setMessage(null).build();
  }

  public static Map<Object, String> getErrorMapWithColumn(String column){
    return new ErrorBuilder().setFieldColumn(column).build();
  }

  public static Map<Object, String> getErrorMapWithColumnNull(){
    return new ErrorBuilder().setMessage(null).build();
  }
}
