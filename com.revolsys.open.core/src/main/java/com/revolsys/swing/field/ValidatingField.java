package com.revolsys.swing.field;

public interface ValidatingField {

  public boolean isFieldValid();
  
  public String getFieldValidationMessage();
}
