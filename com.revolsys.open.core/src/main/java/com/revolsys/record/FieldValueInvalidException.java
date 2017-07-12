package com.revolsys.record;

public class FieldValueInvalidException extends IllegalArgumentException {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final String fieldName;

  private final Object value;

  public FieldValueInvalidException(final String fieldName, final Object value,
    final String message) {
    this(fieldName, value, message, null);
  }

  public FieldValueInvalidException(final String fieldName, final Object value,
    final String message, final Throwable e) {
    super("Invalid " + fieldName + "=" + value + ":" + message, e);
    this.fieldName = fieldName;
    this.value = value;
  }

  public FieldValueInvalidException(final String fieldName, final Object value, final Throwable e) {
    this(fieldName, value, e.getMessage(), e);
  }

  public String getFieldName() {
    return this.fieldName;
  }

  public Object getValue() {
    return this.value;
  }

}
