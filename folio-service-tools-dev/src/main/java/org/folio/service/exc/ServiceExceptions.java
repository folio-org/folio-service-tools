package org.folio.service.exc;

import static java.lang.String.format;
import jakarta.ws.rs.NotFoundException;

public class ServiceExceptions {

  private static final String NOT_FOUND_MSG = "%s not found by id: %s";
  private static final String INVALID_FIELD_VALUE = "'%s' field value is invalid: %s";

  private ServiceExceptions() {
  }

  public static NotFoundException notFound(String entity, String id) {
    return new NotFoundException(format(NOT_FOUND_MSG, entity, id));
  }

  public static NotFoundException notFound(Class<?> entityClass, String id) {
    return notFound(entityClass.getSimpleName(), id);
  }

  public static InvalidFieldValueException invalidValue(String field, Object value) {
    return new InvalidFieldValueException(field, value, format(INVALID_FIELD_VALUE, field, value));
  }
}
