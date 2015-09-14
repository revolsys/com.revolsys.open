package com.revolsys.swing.field;

import com.revolsys.identifier.Identifier;
import com.revolsys.identifier.TypedIdentifier;

public class IdentifierField extends TextField {
  private static final long serialVersionUID = 1L;

  public IdentifierField(final String fieldName) {
    super(fieldName);
  }

  public IdentifierField(final String fieldName, final int columns) {
    super(fieldName, columns);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getFieldValue() {
    final String text = getText();
    final Identifier id = TypedIdentifier.create(text);
    return (T)id;
  }

  @Override
  public void updateFieldValue() {
    final String text = getText();
    final Identifier id = TypedIdentifier.create(text);
    setFieldValue(id);
  }
}
