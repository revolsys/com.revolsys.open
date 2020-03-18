package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

import org.jeometry.common.awt.WebColors;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.swing.EventQueue;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.listener.WeakFocusListener;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.Property;

public class TextField extends JTextField implements Field, FocusListener {
  private static final long serialVersionUID = 1L;

  public static final Color DEFAULT_SELECTED_TEXT_COLOR = new JTextField().getSelectedTextColor();

  private final FieldSupport fieldSupport;

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

  public TextField(String fieldName, final Object fieldValue) {
    final String text = DataTypes.toString(fieldValue);
    this.fieldSupport = new FieldSupport(this, fieldName, text, true);
    setFont(SwingUtil.FONT);
    setDocument(new PropertyChangeDocument(this));
    if (!Property.hasValue(fieldName)) {
      fieldName = "fieldValue";
    }
    setText(text);
    addFocusListener(new WeakFocusListener(this));
    MenuFactory.getPopupMenuFactory(this);
    EventQueue.addAction(this, this::updateFieldValue);
  }

  public TextField(final String fieldName, final Object fieldValue, final int columns) {
    this(fieldName, fieldValue);
    setColumns(columns);
  }

  @Override
  public Field clone() {
    try {
      return (Field)super.clone();
    } catch (final CloneNotSupportedException e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  @Override
  public void firePropertyChange(final String propertyName, final Object oldValue,
    final Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
  }

  @Override
  public void focusGained(final FocusEvent e) {
  }

  @Override
  public void focusLost(final FocusEvent e) {
    updateFieldValue();
  }

  protected String getDisplayText(final Object value) {
    return DataTypes.toString(value);
  }

  @Override
  public String getFieldName() {
    return this.fieldSupport.getName();
  }

  @Override
  public Color getFieldSelectedTextColor() {
    return getSelectedTextColor();
  }

  @Override
  public FieldSupport getFieldSupport() {
    return this.fieldSupport;
  }

  @Override
  public String getFieldValidationMessage() {
    return this.fieldSupport.getErrorMessage();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getFieldValue() {
    final String text = getText();
    return (T)text;
  }

  protected <T> T getFieldValueInternal() {
    return this.fieldSupport.getValue();
  }

  @Override
  public boolean isFieldValid() {
    return this.fieldSupport.isFieldValid();
  }

  @Override
  public void setEditable(final boolean editable) {
    final boolean oldEditable = isEditable();
    if (editable != oldEditable) {
      super.setEditable(editable);
      setForeground(getForeground());
    }
  }

  @Override
  public void setFieldInvalid(final String message, final Color foregroundColor,
    final Color backgroundColor) {
    this.fieldSupport.setFieldInvalid(message, foregroundColor, backgroundColor);
  }

  @Override
  public void setFieldSelectedTextColor(Color color) {
    if (color == null) {
      color = Field.DEFAULT_SELECTED_FOREGROUND;
    }
    setSelectedTextColor(color);
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    setToolTipText(toolTip);
  }

  @Override
  public void setFieldValid() {
    this.fieldSupport.setFieldValid();
  }

  @Override
  public boolean setFieldValue(final Object value) {
    final String newText = getDisplayText(value);
    final String text = getText();
    this.fieldSupport.discardAllEdits();
    if (!DataType.equal(text, newText)) {
      if (newText == null) {
        if (Property.hasValue(text)) {
          setText("");
        }
      } else {
        setText(newText);
      }
      this.fieldSupport.discardAllEdits();
    }
    return this.fieldSupport.setValue(value);
  }

  @Override
  public void setForeground(Color color) {
    if (!isEditable()) {
      color = WebColors.newAlpha(color, 191);
    }
    super.setForeground(color);
  }

  @Override
  public void setToolTipText(final String text) {
    if (this.fieldSupport.setOriginalTooltipText(text)) {
      super.setToolTipText(text);
    }
  }

  @Override
  public void setUndoManager(final UndoManager undoManager) {
    this.fieldSupport.setUndoManager(undoManager);
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }

  @Override
  public void updateFieldValue() {
    final String text = getText();
    setFieldValue(text);
  }
}
