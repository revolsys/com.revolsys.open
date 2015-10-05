package com.revolsys.swing.field;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;

import com.revolsys.equals.Equals;
import com.revolsys.swing.border.IconBorder;
import com.revolsys.swing.undo.CascadingUndoManager;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.Property;

public class FieldSupport {
  private final JComponent component;

  private String errorMessage;

  private final Field field;

  private final IconBorder iconBorder;

  private final String name;

  private final Color originalBackgroundColor;

  private final Color originalForegroundColor;

  private final Color originalSelectedTextColor;

  private String originalTooltipText;

  private final CascadingUndoManager undoManager = new CascadingUndoManager();

  private boolean updating = false;

  private Object value;

  private boolean fieldValid = true;

  public FieldSupport(final Field field, final JComponent component, final String name,
    final Object value) {
    this.field = field;
    if (Property.isEmpty(name)) {
      this.name = "fieldValue";
    } else {
      this.name = name;
    }
    this.value = value;
    this.component = component;
    this.undoManager.addKeyMap(this.component);
    this.originalForegroundColor = this.component.getForeground();
    this.originalBackgroundColor = this.component.getBackground();
    this.originalSelectedTextColor = field.getFieldSelectedTextColor();
    this.originalTooltipText = this.component.getToolTipText();
    this.iconBorder = new IconBorder(null);
    final Border originalBorder = this.component.getBorder();
    this.component.setBorder(BorderFactory.createCompoundBorder(originalBorder, this.iconBorder));
  }

  public FieldSupport(final Field field, final String name, final Object value) {
    this(field, (JComponent)field, name, value);
  }

  public void discardAllEdits() {
    this.undoManager.discardAllEdits();
  }

  public String getErrorMessage() {
    return this.errorMessage;
  }

  public String getName() {
    return this.name;
  }

  public Color getOriginalBackgroundColor() {
    return this.originalBackgroundColor;
  }

  public Color getOriginalForegroundColor() {
    return this.originalForegroundColor;
  }

  public CascadingUndoManager getUndoManager() {
    return this.undoManager;
  }

  @SuppressWarnings("unchecked")
  public <V> V getValue() {
    return (V)this.value;
  }

  public boolean hasError() {
    return Property.hasValue(this.errorMessage);
  }

  public boolean isFieldValid() {
    return this.fieldValid;
  }

  public void setFieldInvalid(final String message, final Color foregroundColor,
    final Color backgroundColor) {
    final boolean oldValid = this.fieldValid;
    this.updating = true;
    try {
      this.fieldValid = false;
      this.field.setFieldForegroundColor(foregroundColor);
      this.field.setFieldSelectedTextColor(foregroundColor);
      this.field.setFieldBackgroundColor(backgroundColor);
      this.component.setToolTipText(message);
      this.iconBorder.setIcon(Field.ERROR_ICON);
      this.errorMessage = message;
    } finally {
      this.updating = false;
    }
    if (oldValid) {
      this.field.firePropertyChange("fieldValid", oldValid, false);
    }
  }

  public void setFieldToolTip(final String toolTip) {
    this.component.setToolTipText(toolTip);
  }

  public void setFieldValid() {
    final boolean oldValid = this.fieldValid;
    this.updating = true;
    try {
      this.fieldValid = true;
      this.errorMessage = null;
      this.iconBorder.setIcon(null);
      this.field.setFieldForegroundColor(this.originalForegroundColor);
      this.field.setFieldSelectedTextColor(this.originalSelectedTextColor);
      this.field.setFieldBackgroundColor(this.originalBackgroundColor);
      this.component.setToolTipText(this.originalTooltipText);
    } finally {
      this.updating = false;
    }
    if (!oldValid) {
      this.field.firePropertyChange("fieldValid", oldValid, true);
    }
  }

  public boolean setOriginalTooltipText(final String text) {
    if (this.updating) {
      return true;
    } else {
      this.originalTooltipText = text;
      return !hasError();
    }
  }

  public void setToolTipText(final String text) {
    if (setOriginalTooltipText(text)) {
      this.component.setToolTipText(text);
    }
  }

  public void setUndoManager(final UndoManager undoManager) {
    this.undoManager.setParent(undoManager);
  }

  public void setValue(final Object value) {
    final Object oldValue = this.value;
    if (!Equals.equal(oldValue, value)) {
      this.value = value;
      this.field.firePropertyChange(this.name, oldValue, value);
      SetFieldValueUndoableEdit.create(this.undoManager.getParent(), this.field, oldValue, value);
    }
  }
}
