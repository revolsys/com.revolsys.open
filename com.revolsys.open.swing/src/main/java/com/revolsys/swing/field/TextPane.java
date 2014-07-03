package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JEditorPane;
import javax.swing.text.Element;

import jsyntaxpane.DefaultSyntaxKit;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.swing.listener.WeakFocusListener;
import com.revolsys.swing.menu.PopupMenu;
import com.revolsys.swing.undo.CascadingUndoManager;
import com.revolsys.swing.undo.UndoManager;

public class TextPane extends JEditorPane implements Field, FocusListener {
  private static final long serialVersionUID = 1L;

  private final String fieldName;

  private String fieldValue;

  private String errorMessage;

  private String originalToolTip;

  static {
    DefaultSyntaxKit.initKit();
  }

  private final CascadingUndoManager undoManager = new CascadingUndoManager();

  public TextPane() {
    this("fieldValue");
  }

  public TextPane(final int rows, final int columns) {
    this("text");
    setRowsAndColumns(rows, columns);
  }

  public TextPane(final String fieldName) {
    this(fieldName, "");
  }

  public TextPane(final String fieldName, final int rows, final int columns) {
    this(fieldName, "");
    setRowsAndColumns(rows, columns);
  }

  public TextPane(final String fieldName, final Object fieldValue) {
    this.fieldName = fieldName;
    this.fieldValue = StringConverterRegistry.toString(fieldValue);
    setDocument(new PropertyChangeStyledDocument(this));
    setText(this.fieldValue);
    addFocusListener(new WeakFocusListener(this));
    PopupMenu.getPopupMenuFactory(this);
    this.undoManager.addKeyMap(this);
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

  public int getLineStartOffset(final int line) {
    if (line < 0) {
      return -1;
    } else {
      final Element map = getDocument().getDefaultRootElement();
      final Element lineElem = map.getElement(line);
      if (lineElem == null) {
        return -1;
      } else {
        return lineElem.getStartOffset();
      }
    }
  }

  @Override
  public boolean isFieldValid() {
    return true;
  }

  @Override
  public void setFieldBackgroundColor(Color color) {
    if (color == null) {
      color = TextField.DEFAULT_BACKGROUND;
    }
    setBackground(color);
  }

  @Override
  public void setFieldForegroundColor(Color color) {
    if (color == null) {
      color = TextField.DEFAULT_BACKGROUND;
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
    final String newValue = StringConverterRegistry.toString(value);
    final String oldValue = this.fieldValue;
    if (!EqualsRegistry.equal(getText(), newValue)) {
      setText(newValue);
    }
    if (!EqualsRegistry.equal(oldValue, value)) {
      this.fieldValue = (String)value;
      firePropertyChange(this.fieldName, oldValue, value);
      SetFieldValueUndoableEdit.create(this.undoManager.getParent(), this,
        oldValue, value);
    }
  }

  protected void setRowsAndColumns(final int rows, final int columns) {
    setPreferredSize(new Dimension(12 * columns, 15 * rows));
  }

  @Override
  public void setToolTipText(final String text) {
    this.originalToolTip = text;
    if (!StringUtils.hasText(this.errorMessage)) {
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
