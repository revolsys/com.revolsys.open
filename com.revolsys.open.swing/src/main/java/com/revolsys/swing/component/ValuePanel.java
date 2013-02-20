package com.revolsys.swing.component;

import java.awt.BorderLayout;
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

import com.revolsys.swing.action.InvokeMethodAction;

@SuppressWarnings("serial")
public class ValuePanel<T> extends JPanel {
  private String title;

  private T value;

  public ValuePanel() {
  }

  public ValuePanel(final boolean isDoubleBuffered) {
    super(isDoubleBuffered);
  }

  public ValuePanel(final LayoutManager layout) {
    super(layout);
  }

  public ValuePanel(final LayoutManager layout, final boolean isDoubleBuffered) {
    super(layout, isDoubleBuffered);
  }

  public void cancel() {
  }

  public void cancel(final JDialog dialog) {
    cancel();
    dialog.setVisible(false);
  }

  public T getValue() {
    return value;
  }

  public void save() {
    save(this);
  }

  private void save(final Container container) {
    final Component[] components = container.getComponents();
    for (final Component component : components) {
      if (component instanceof ValuePanel<?>) {
        final ValuePanel<?> valuePanel = (ValuePanel<?>)component;
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

  public void setValue(final T value) {
    final T oldValue = this.value;
    this.value = value;
    firePropertyChange("value", oldValue, value);
  }

  public T showDialog(final Component component) {
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

    return getValue();
  }
}
