package com.revolsys.swing.field;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.swing.border.IconBorder;
import com.revolsys.swing.undo.CascadingUndoManager;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.Property;

public class FieldSupport {
  private final Field field;

  private final JComponent component;

  private final String name;

  private Object value;

  private String errorMessage;

  private final Color originalBackgroundColor;

  private final Color originalForegroundColor;

  private Color originalSelectedTextColor;

  private String originalTooltipText;

  private final CascadingUndoManager undoManager = new CascadingUndoManager();

  private final IconBorder iconBorder;

  private boolean updating = false;

  public FieldSupport(final Field field, final JComponent component, final String name,
    final Object value) {
    this.field = field;
    this.name = name;
    this.value = value;
    this.component = component;
    this.undoManager.addKeyMap(this.component);
    this.originalForegroundColor = this.component.getForeground();
    this.originalBackgroundColor = this.component.getBackground();
    if (component instanceof JTextComponent) {
      final JTextComponent textComponent = (JTextComponent)component;
      this.originalSelectedTextColor = textComponent.getSelectedTextColor();
    }
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

  public void setFieldInvalid(final String message, final Color foregroundColor,
    final Color backgroundColor) {
    this.updating = true;
    try {
      this.component.setForeground(foregroundColor);
      if (this.component instanceof JTextComponent) {
        final JTextComponent textComponent = (JTextComponent)this.component;
        textComponent.setSelectedTextColor(foregroundColor);
      }
      this.component.setBackground(backgroundColor);
      this.component.setToolTipText(message);
      this.iconBorder.setIcon(Field.ERROR_ICON);
      this.errorMessage = message;
    } finally {
      this.updating = false;
    }
  }

  public void setFieldValid() {
    this.updating = true;
    try {
      this.errorMessage = null;
      this.iconBorder.setIcon(null);
      this.component.setForeground(this.originalForegroundColor);
      if (this.component instanceof JTextComponent) {
        final JTextComponent textComponent = (JTextComponent)this.component;
        textComponent.setSelectedTextColor(this.originalSelectedTextColor);
      }
      this.component.setBackground(this.originalBackgroundColor);
      this.component.setToolTipText(this.originalTooltipText);
    } finally {
      this.updating = false;
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

  public void setUndoManager(final UndoManager undoManager) {
    this.undoManager.setParent(undoManager);
  }

  public void setValue(final Object value) {
    final Object oldValue = this.value;
    if (!EqualsRegistry.equal(oldValue, value)) {
      this.value = value;
      this.field.firePropertyChange(this.name, oldValue, value);
      SetFieldValueUndoableEdit.create(this.undoManager.getParent(), this.field, oldValue, value);
    }
  }
}
