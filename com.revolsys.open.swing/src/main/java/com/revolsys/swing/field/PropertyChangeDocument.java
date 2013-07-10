package com.revolsys.swing.field;

import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

@SuppressWarnings("serial")
public class PropertyChangeDocument extends PlainDocument {
  private boolean ignoreEvents = false;

  private final JTextComponent textComponent;

  public PropertyChangeDocument(final JTextComponent textComponent) {
    this.textComponent = textComponent;
  }

  @Override
  protected void fireChangedUpdate(final DocumentEvent e) {
    super.fireChangedUpdate(e);
    if (!ignoreEvents) {
      fireTextChanged(null);
    }
  }

  @Override
  protected void fireInsertUpdate(final DocumentEvent e) {
    super.fireInsertUpdate(e);
    if (!ignoreEvents) {
      fireTextChanged(null);
    }
  }

  @Override
  protected void fireRemoveUpdate(final DocumentEvent e) {
    super.fireRemoveUpdate(e);
    if (!ignoreEvents) {
      fireTextChanged(null);
    }
  }

  protected void fireTextChanged(final String oldValue) {
    final String newValue = textComponent.getText();
    ((Field)textComponent).firePropertyChange("text", null, newValue);
  }

  @Override
  public void remove(final int offs, final int len) throws BadLocationException {
    final String oldValue = textComponent.getText();
    super.remove(offs, len);
    if (!ignoreEvents) {
      fireTextChanged(oldValue);
    }
  }

  @Override
  public void replace(final int offset, final int length, final String text,
    final AttributeSet attrs) throws BadLocationException {
    final String oldValue = textComponent.getText();
    this.ignoreEvents = true;
    super.replace(offset, length, text, attrs);
    this.ignoreEvents = false;
    fireTextChanged(oldValue);
  }
}
