package org.folio.service.exc;

import java.io.Serial;
import org.apache.commons.lang3.StringUtils;

public class InvalidFieldValueException extends InvalidDataException {

  @Serial
  private static final long serialVersionUID = 7995122190288101587L;

  private final String field;
  private final Object value;

  public InvalidFieldValueException(String field, Object value, String message) {
    super(message);

    if (StringUtils.isBlank(field)) {
      throw new IllegalArgumentException("Field cannot be empty");
    }

    this.field = field;
    this.value = value;
  }

  public String getField() {
    return field;
  }

  public Object getValue() {
    return value;
  }
}
