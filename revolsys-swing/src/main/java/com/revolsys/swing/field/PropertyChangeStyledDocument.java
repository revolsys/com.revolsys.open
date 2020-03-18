package com.revolsys.swing.field;

import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.JTextComponent;

public class PropertyChangeStyledDocument extends DefaultStyledDocument {
  private static final long serialVersionUID = 1L;

  private boolean ignoreEvents = false;

  private final JTextComponent textComponent;

  public PropertyChangeStyledDocument(final JTextComponent textComponent) {
    this.textComponent = textComponent;
  }

  @Override
  protected void fireChangedUpdate(final DocumentEvent e) {
    super.fireChangedUpdate(e);
    if (!this.ignoreEvents) {
      fireTextChanged(null);
    }
  }

  @Override
  protected void fireInsertUpdate(final DocumentEvent e) {
    super.fireInsertUpdate(e);
    if (!this.ignoreEvents) {
      fireTextChanged(null);
    }
  }

  @Override
  protected void fireRemoveUpdate(final DocumentEvent e) {
    super.fireRemoveUpdate(e);
    if (!this.ignoreEvents) {
      fireTextChanged(null);
    }
  }

  protected void fireTextChanged(final String oldValue) {
    if (this.textComponent != null) {
      final String newValue = this.textComponent.getText();
      ((Field)this.textComponent).firePropertyChange("text", null, newValue);
    }
  }

  @Override
  public void remove(final int offs, final int len) throws BadLocationException {
    final String oldValue = this.textComponent.getText();
    super.remove(offs, len);
    if (!this.ignoreEvents) {
      fireTextChanged(oldValue);
    }
  }

  @Override
  public void replace(final int offset, final int length, final String text,
    final AttributeSet attrs) throws BadLocationException {
    final String oldValue = this.textComponent.getText();
    this.ignoreEvents = true;
    super.replace(offset, length, text, attrs);
    this.ignoreEvents = false;
    fireTextChanged(oldValue);
  }
}
