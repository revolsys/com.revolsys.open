package com.revolsys.swing.field;

public interface ValidatingField<V> extends Field<V> {

  String getFieldValidationMessage();

  boolean isFieldValid();
}
