package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

import org.jdesktop.swingx.JXTextField;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.swing.listener.WeakFocusListener;
import com.revolsys.swing.menu.PopupMenu;
import com.revolsys.swing.undo.CascadingUndoManager;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.Property;

public class TextField extends JXTextField implements Field, FocusListener {
  private static final long serialVersionUID = 1L;

  private final String fieldName;

  private String fieldValue;

  public static final Color DEFAULT_SELECTED_FOREGROUND = new JTextField().getSelectedTextColor();

  public static final Color DEFAULT_BACKGROUND = new JTextField().getBackground();

  public static final Color DEFAULT_FOREGROUND = new JTextField().getForeground();

  private String errorMessage;

  private String originalToolTip;

  private final CascadingUndoManager undoManager = new CascadingUndoManager();

  public TextField(final int columns) {
    this("text");
    setColumns(columns);
  }

  public TextField(final String fieldName) {
    this(fieldName, "");
  }

  public TextField(final String fieldName, final int columns) {
    this(fieldName);
    setColumns(columns);
  }

  public TextField(final String fieldName, final Object fieldValue) {
    setDocument(new PropertyChangeDocument(this));
    if (Property.hasValue(fieldName)) {
      this.fieldName = fieldName;
    } else {
      this.fieldName = "fieldValue";
    }
    this.fieldValue = StringConverterRegistry.toString(fieldValue);
    setText(this.fieldValue);
    addFocusListener(new WeakFocusListener(this));
    this.undoManager.addKeyMap(this);
    PopupMenu.getPopupMenuFactory(this);
  }

  public TextField(final String fieldName, final Object fieldValue,
    final int columns) {
    this(fieldName, fieldValue);
    setColumns(columns);
  }

  @Override
  public void firePropertyChange(final String propertyName,
    final Object oldValue, final Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
  }

  @Override
  public void focusGained(final FocusEvent e) {
  }

  @Override
  public void focusLost(final FocusEvent e) {
    final String text = getText();
    setFieldValue(text);
  }

  protected String getDisplayText(final Object value) {
    return StringConverterRegistry.toString(value);
  }

  @Override
  public String getFieldName() {
    return this.fieldName;
  }

  @Override
  public String getFieldValidationMessage() {
    return this.errorMessage;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getFieldValue() {
    return (T)getText();
  }

  @Override
  public boolean isFieldValid() {
    return true;
  }

  @Override
  public void setFieldBackgroundColor(Color color) {
    if (color == null) {
      color = DEFAULT_BACKGROUND;
    }
    setBackground(color);
  }

  @Override
  public void setFieldForegroundColor(Color color) {
    if (color == null) {
      color = DEFAULT_FOREGROUND;
    }
    setForeground(color);
  }

  @Override
  public void setFieldInvalid(final String message,
    final Color foregroundColor, final Color backgroundColor) {
    setForeground(foregroundColor);
    setSelectedTextColor(foregroundColor);
    setBackground(backgroundColor);
    this.errorMessage = message;
    super.setToolTipText(this.errorMessage);
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    setToolTipText(toolTip);
  }

  @Override
  public void setFieldValid() {
    setForeground(TextField.DEFAULT_FOREGROUND);
    setSelectedTextColor(TextField.DEFAULT_SELECTED_FOREGROUND);
    setBackground(TextField.DEFAULT_BACKGROUND);
    this.errorMessage = null;
    super.setToolTipText(this.originalToolTip);
  }

  @Override
  public void setFieldValue(final Object value) {
    final String newText = getDisplayText(value);
    final String oldValue = this.fieldValue;
    final String text = getText();
    this.undoManager.discardAllEdits();
    if (!EqualsRegistry.equal(text, newText)) {
      if (newText == null) {
        if (Property.hasValue(text)) {
          setText("");
        }
      } else {
        setText(newText);
      }
      this.undoManager.discardAllEdits();
    }
    if (!EqualsRegistry.equal(oldValue, newText)) {
      this.fieldValue = newText;
      firePropertyChange(this.fieldName, oldValue, newText);
      SetFieldValueUndoableEdit.create(this.undoManager.getParent(), this,
        oldValue, newText);
    }
  }

  @Override
  public void setToolTipText(final String text) {
    this.originalToolTip = text;
    if (!Property.hasValue(this.errorMessage)) {
      super.setToolTipText(text);
    }
  }

  @Override
  public void setUndoManager(final UndoManager undoManager) {
    this.undoManager.setParent(undoManager);
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }

  @Override
  public void updateFieldValue() {
    setFieldValue(getText());
  }
}
