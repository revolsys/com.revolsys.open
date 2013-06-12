package com.revolsys.swing.field;

public interface ValidatingField extends Field {

  String getFieldValidationMessage();

  boolean isFieldValid();
}
