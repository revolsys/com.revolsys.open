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

import org.springframework.util.StringUtils;

import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.field.Field;
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
  }

  public void cancel(final JDialog dialog) {
    cancel();
    dialog.setVisible(false);
  }

  @Override
  public String getFieldName() {
    return fieldName;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getFieldValue() {
    return (T)fieldValue;
  }

  public String getTitle() {
    return title;
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
  }

  public void save(final JDialog dialog) {
    save();
    dialog.setVisible(false);
  }

  @Override
  public void setFieldInvalid(final String message) {
    setForeground(Color.RED);
    setBackground(Color.PINK);
    this.errorMessage = message;
    super.setToolTipText(errorMessage);
  }

  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public void setFieldValid() {
    setForeground(defaultForeground);
    setBackground(defaultBackground);
    super.setToolTipText(originalToolTip);
  }

  @Override
  public void setFieldValue(final Object value) {
    final Object oldValue = this.fieldValue;
    this.fieldValue = value;
    firePropertyChange("fieldValue", oldValue, value);
    if (StringUtils.hasText(fieldName)) {
      firePropertyChange(fieldName, oldValue, value);
    }
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  @Override
  public void setToolTipText(final String text) {
    this.originalToolTip = text;
    if (!StringUtils.hasText(errorMessage)) {
      super.setToolTipText(text);
    }
  }

  public Object showDialog(final Component component) {
    Window window;
    if (component == null) {
      window = null;
    } else if (component instanceof Window) {
      window = (Window)component;

    } else {
      window = SwingUtilities.windowForComponent(component);
    }
    final JDialog dialog = new JDialog(window, title,
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
    dialog.setVisible(true);

    return getFieldValue();
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }
}
