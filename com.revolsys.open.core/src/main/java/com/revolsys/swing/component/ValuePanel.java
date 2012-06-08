package com.revolsys.swing.component;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;

import javax.swing.JPanel;

public class ValuePanel<T> extends JPanel {
  /**
   * 
   */
  private static final long serialVersionUID = -6266029828086786947L;

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

  public void setValue(final T value) {
    final T oldValue = this.value;
    this.value = value;
    firePropertyChange("value", oldValue, value);
  }
}
