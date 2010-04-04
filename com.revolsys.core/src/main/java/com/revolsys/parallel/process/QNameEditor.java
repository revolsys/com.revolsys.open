package com.revolsys.parallel.process;

import java.beans.PropertyEditorSupport;

import javax.xml.namespace.QName;

public class QNameEditor extends PropertyEditorSupport {
  public QNameEditor() {
  }

  public void setAsText(
    final String text)
    throws IllegalArgumentException {
    setValue(QName.valueOf(text));
  }

  public String getAsText() {
    final QName value = (QName)getValue();
    if (value == null) {
      return "";
    } else {
      return value.toString();
    }
  }
}
