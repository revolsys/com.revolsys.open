package com.revolsys.swing.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jdesktop.swingx.color.ColorUtil;
import org.springframework.util.StringUtils;

import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.CaseConverter;

@SuppressWarnings("serial")
public class ValueField extends JPanel implements Field {

  private final Color defaultBackground = getBackground();

  private final Color defaultForeground = getBackground();

  private String errorMessage;

  private String fieldName;

  private Object fieldValue;

  private String title;

  private String originalToolTip;

  private boolean saved = false;

  public ValueField() {
    this("fieldValue", null);
  }

  public ValueField(final boolean isDoubleBuffered) {
    super(isDoubleBuffered);
  }

  public ValueField(final LayoutManager layout) {
    super(layout);
  }

  public ValueField(final LayoutManager layout, final boolean isDoubleBuffered) {
    super(layout, isDoubleBuffered);
  }

  public ValueField(final Object fieldValue) {
    this(null, fieldValue);
  }

  public ValueField(final String fieldName, final Object fieldValue) {
    setFieldName(fieldName);
    setFieldValue(fieldValue);
    setTitle(CaseConverter.toCapitalizedWords(fieldName));
  }

  public void cancel() {
    this.saved = false;
  }

  public void cancel(final JDialog dialog) {
    cancel();
    SwingUtil.setVisible(dialog, false);
  }

  @Override
  public void firePropertyChange(final String propertyName,
    final Object oldValue, final Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
  }

  @Override
  public String getFieldName() {
    return this.fieldName;
  }

  @Override
  public String getFieldValidationMessage() {
    return this.errorMessage;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getFieldValue() {
    return (T)this.fieldValue;
  }

  public String getTitle() {
    return this.title;
  }

  @Override
  public boolean isFieldValid() {
    return true;
  }

  public boolean isSaved() {
    return this.saved;
  }

  public void save() {
    save(this);
  }

  private void save(final Container container) {
    final Component[] components = container.getComponents();
    for (final Component component : components) {
      if (component instanceof ValueField) {
        final ValueField valuePanel = (ValueField)component;
        valuePanel.save();
      } else if (component instanceof Container) {
        final Container childContainer = (Container)component;
        save(childContainer);
      }

    }
    this.saved = true;
  }

  public void save(final JDialog dialog) {
    save();
    dialog.setVisible(false);
  }

  @Override
  public void setFieldBackgroundColor(Color color) {
    if (color == null) {
      color = this.defaultBackground;
    }
    setBackground(color);
  }

  @Override
  public void setFieldForegroundColor(Color color) {
    if (color == null) {
      color = this.defaultForeground;
    }
    setForeground(color);
  }

  @Override
  public void setFieldInvalid(final String message, final Color color) {
    setForeground(color);
    setBackground(ColorUtil.setAlpha(color, 50));
    this.errorMessage = message;
    super.setToolTipText(this.errorMessage);
  }

  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    setToolTipText(toolTip);
  }

  @Override
  public void setFieldValid() {
    setForeground(this.defaultForeground);
    setBackground(this.defaultBackground);
    super.setToolTipText(this.originalToolTip);
  }

  @Override
  public void setFieldValue(final Object value) {
    final Object oldValue = this.fieldValue;
    this.fieldValue = value;
    firePropertyChange("fieldValue", oldValue, value);
    if (StringUtils.hasText(this.fieldName)) {
      firePropertyChange(this.fieldName, oldValue, value);
    }
  }

  public void setTitle(final String title) {
    this.title = title;
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
  }

  @SuppressWarnings("unchecked")
  public <V> V showDialog() {
    return (V)showDialog(null);
  }

  @SuppressWarnings("unchecked")
  public <V> V showDialog(final Component component) {
    Window window;
    if (component == null) {
      window = SwingUtil.getActiveWindow();
    } else if (component instanceof Window) {
      window = (Window)component;
    } else {
      window = SwingUtilities.windowForComponent(component);
    }
    final JDialog dialog = new JDialog(window, this.title,
      ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.setLayout(new BorderLayout());

    dialog.add(this, BorderLayout.CENTER);

    final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttons.add(InvokeMethodAction.createButton("Cancel", this, "cancel",
      dialog));
    buttons.add(InvokeMethodAction.createButton("OK", this, "save", dialog));
    dialog.add(buttons, BorderLayout.SOUTH);

    dialog.pack();
    SwingUtil.autoAdjustPosition(dialog);
    this.saved = false;
    dialog.setVisible(true);

    final V value = (V)getFieldValue();
    dialog.dispose();
    return value;
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }
}
