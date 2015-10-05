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

import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.FieldSupport;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.ExceptionUtil;

public class ValueField extends JPanel implements Field {
  private static final long serialVersionUID = 1L;

  private boolean saved = false;

  private FieldSupport fieldSupport;

  private String title;

  public ValueField() {
    this("fieldValue", null);
  }

  public ValueField(final boolean isDoubleBuffered) {
    super(isDoubleBuffered);
    setOpaque(false);
  }

  public ValueField(final LayoutManager layout) {
    super(layout);
    setOpaque(false);
  }

  public ValueField(final LayoutManager layout, final boolean isDoubleBuffered) {
    super(layout, isDoubleBuffered);
    setOpaque(false);
  }

  public ValueField(final Object fieldValue) {
    this(null, fieldValue);
  }

  public ValueField(final String fieldName, final Object fieldValue) {
    this.fieldSupport = new FieldSupport(this, fieldName, fieldValue);
    setTitle(CaseConverter.toCapitalizedWords(fieldName));
    setOpaque(false);
  }

  public void cancel() {
    this.saved = false;
  }

  public void cancel(final JDialog dialog) {
    cancel();
    SwingUtil.setVisible(dialog, false);
  }

  @Override
  public Field clone() {
    try {
      return (Field)super.clone();
    } catch (final CloneNotSupportedException e) {
      return ExceptionUtil.throwUncheckedException(e);
    }
  }

  protected void doSave() {
  }

  @Override
  public void firePropertyChange(final String propertyName, final Object oldValue,
    final Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
  }

  public String getTitle() {
    return this.title;
  }

  public boolean isSaved() {
    return this.saved;
  }

  public void save() {
    save(this);
    doSave();
  }

  private void save(final Container container) {
    final Component[] components = container.getComponents();
    for (final Component component : components) {
      if (component instanceof ValueField) {
        final ValueField valuePanel = (ValueField)component;
        valuePanel.save();
      } else if (component instanceof Field) {
        final Field field = (Field)component;
        field.updateFieldValue();
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

  protected void setColor(final Color foregroundColor, final Color backgroundColor) {
    setForeground(foregroundColor);
    setBackground(backgroundColor);
  }

  @Override
  public void setEditable(final boolean editable) {
    setEnabled(editable);
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    setToolTipText(toolTip);
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  @Override
  public void setToolTipText(final String text) {
    if (this.fieldSupport.setOriginalTooltipText(text)) {
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
    final JDialog dialog = new JDialog(window, this.title, ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.setLayout(new BorderLayout());

    dialog.add(this, BorderLayout.CENTER);

    final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttons.add(RunnableAction.createButton("Cancel", () -> cancel(dialog)));
    buttons.add(RunnableAction.createButton("OK", () -> save(dialog)));
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

  @Override
  public void updateFieldValue() {
  }
}
