package com.revolsys.swing.listener;

import java.awt.Component;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class EnableComponentListener implements ItemListener,
  ListSelectionListener,PropertyChangeListener {
  private Component component;

  public EnableComponentListener(Component component) {
    this.component = component;
  }

  @Override
  public void itemStateChanged(ItemEvent e) {
    ItemSelectable itemSelectable = e.getItemSelectable();
    Object[] selectedObjects = itemSelectable.getSelectedObjects();
    if (selectedObjects == null) {
      component.setEnabled(false);
    } else {
      component.setEnabled(true);
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    Object newValue = event.getNewValue();
    if (newValue instanceof Boolean) {
      Boolean enabled = (Boolean)newValue;
      component.setEnabled(enabled);
      
    }
  }
  @Override
  public void valueChanged(ListSelectionEvent e) {
    if (!e.getValueIsAdjusting()) {

      int firstIndex = e.getFirstIndex();
      if (firstIndex == -1) {
        component.setEnabled(false);
      } else {
        component.setEnabled(true);
      }
    }
  }
}
