package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

import org.jdesktop.swingx.JXTextField;
import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.menu.PopupMenu;

@SuppressWarnings("serial")
public class TextField extends JXTextField implements Field, FocusListener {

  private final String fieldName;

  private String fieldValue;

  public static final Color DEFAULT_SELECTED_FOREGROUND = new JTextField().getSelectedTextColor();

  public static final Color DEFAULT_BACKGROUND = new JTextField().getBackground();

  public static final Color DEFAULT_FOREGROUND = new JTextField().getForeground();

  private String errorMessage;

  private String originalToolTip;

  public TextField(final int columns) {
    this(null);
    setColumns(columns);
  }

  public TextField(final String fieldName) {
    this(fieldName, "");
  }

  public TextField(final String fieldName, final int columns) {
    this(fieldName);
    setColumns(columns);
    PopupMenu.getPopupMenuFactory(this);
  }

  public TextField(final String fieldName, final Object fieldValue) {
    if (StringUtils.hasText(fieldName)) {
      this.fieldName = fieldName;
    } else {
      this.fieldName = "fieldValue";
    }
    this.fieldValue = StringConverterRegistry.toString(fieldValue);
    setText(this.fieldValue);
    addFocusListener(this);
  }

  @Override
  public void focusGained(final FocusEvent e) {
  }

  @Override
  public void focusLost(final FocusEvent e) {
    final String text = getText();
    setFieldValue(text);
  }

  @Override
  public String getFieldName() {
    return fieldName;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getFieldValue() {
    return (T)getText();
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
  public void setFieldInvalid(final String message) {
    setForeground(Color.RED);
    setSelectedTextColor(Color.RED);
    setBackground(Color.PINK);
    this.errorMessage = message;
    super.setToolTipText(errorMessage);
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
    super.setToolTipText(originalToolTip);
  }

  @Override
  public void setFieldValue(final Object value) {
    final String newValue = StringConverterRegistry.toString(value);
    final String oldValue = fieldValue;
    final String text = getText();
    if (!EqualsRegistry.equal(text, newValue)) {
      if (newValue == null) {
        if (StringUtils.hasText(text)) {
          setText("");
        }
      } else {
        setText(newValue);
      }
    }
    this.fieldValue = newValue;
    firePropertyChange(fieldName, oldValue, newValue);
  }

  @Override
  public void setToolTipText(final String text) {
    this.originalToolTip = text;
    if (!StringUtils.hasText(errorMessage)) {
      super.setToolTipText(text);
    }
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }
}
