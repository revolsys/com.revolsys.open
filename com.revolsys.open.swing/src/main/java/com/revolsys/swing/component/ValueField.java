package com.revolsys.swing.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.FieldSupport;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.Exceptions;

public class ValueField extends JPanel implements Field {
  private static final long serialVersionUID = 1L;

  private boolean saved = false;

  private final FieldSupport fieldSupport;

  private String title;

  private Runnable saveAction = null;

  private Runnable cancelAction = null;

  private Image iconImage;

  public ValueField() {
    this("fieldValue", null);
  }

  public ValueField(final LayoutManager layout) {
    this(layout, null, null);
    setOpaque(false);
  }

  public ValueField(final LayoutManager layout, final String fieldName, final Object fieldValue) {
    super(layout, true);
    setOpaque(false);
    this.fieldSupport = new FieldSupport(this, fieldName, fieldValue, true);
    setTitle(CaseConverter.toCapitalizedWords(fieldName));
  }

  public ValueField(final Object fieldValue) {
    this(null, fieldValue);
  }

  public ValueField(final String fieldName, final Object fieldValue) {
    this(new VerticalLayout(), fieldName, fieldValue);
  }

  public final void cancel() {
    cancelDo();
    this.saved = false;
  }

  public void cancel(final JDialog dialog) {
    cancel();
    SwingUtil.setVisible(dialog, false);
  }

  protected void cancelDo() {
    if (this.cancelAction != null) {
      this.cancelAction.run();
    }
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
  public FieldSupport getFieldSupport() {
    return this.fieldSupport;
  }

  public String getTitle() {
    return this.title;
  }

  public boolean isSaved() {
    return this.saved;
  }

  public void save() {
    save(this);
    saveDo();
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

  protected void saveDo() {
    if (this.saveAction != null) {
      this.saveAction.run();
    }
  }

  public void setCancelAction(final Runnable cancelAction) {
    this.cancelAction = cancelAction;
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

  public void setIconImage(final Image icon) {
    this.iconImage = icon;
  }

  public void setIconImage(final String iconName) {
    this.iconImage = Icons.getImage(iconName);
  }

  public void setSaveAction(final Runnable saveAction) {
    this.saveAction = saveAction;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  @Override
  public void setToolTipText(final String text) {
    if (this.fieldSupport == null || this.fieldSupport.setOriginalTooltipText(text)) {
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
  public <V> V showDialog(final Component parent) {
    Window window;
    if (parent == null) {
      window = SwingUtil.getActiveWindow();
    } else if (parent instanceof Window) {
      window = (Window)parent;
    } else {
      window = SwingUtilities.windowForComponent(parent);
    }
    final JDialog dialog = new JDialog(window, this.title, ModalityType.APPLICATION_MODAL);
    if (this.iconImage != null) {
      dialog.setIconImage(this.iconImage);
    }
    dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    dialog.setLayout(new BorderLayout());

    dialog.add(this, BorderLayout.CENTER);

    final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttons.add(RunnableAction.newButton("Cancel", () -> cancel(dialog)));
    buttons.add(RunnableAction.newButton("OK", () -> save(dialog)));
    dialog.add(buttons, BorderLayout.SOUTH);

    dialog.pack();
    SwingUtil.autoAdjustPosition(dialog);
    this.saved = false;
    dialog.setVisible(true);
    Invoke.workerDone("Dispose", dialog::dispose);
    final V value = (V)getFieldValue();
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
