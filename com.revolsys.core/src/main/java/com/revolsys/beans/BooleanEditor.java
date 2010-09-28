package com.revolsys.beans;

import java.beans.PropertyEditorSupport;

import javax.xml.namespace.QName;

public class BooleanEditor extends PropertyEditorSupport {
  public BooleanEditor() {
  }

  public void setAsText(
    final String text)
    throws IllegalArgumentException {
    setValue(Boolean.valueOf(text));
  }

  public String getAsText() {
    final Boolean value = (Boolean)getValue();
    if (value == null) {
      return "";
    } else {
      return value.toString();
    }
  }
}
