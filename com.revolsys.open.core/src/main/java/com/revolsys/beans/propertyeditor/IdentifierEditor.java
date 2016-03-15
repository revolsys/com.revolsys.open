package com.revolsys.beans.propertyeditor;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import com.revolsys.identifier.Identifier;

public class IdentifierEditor extends PropertyEditorSupport {
  public static final PropertyEditor INSTANCE = new IdentifierEditor();

  public IdentifierEditor() {
  }

  @Override
  public String getAsText() {
    final Identifier value = (Identifier)getValue();
    if (value == null) {
      return "";
    } else {
      return value.toString();
    }
  }

  @Override
  public void setAsText(final String text) throws IllegalArgumentException {
    setValue(Identifier.newIdentifier(text));
  }
}
