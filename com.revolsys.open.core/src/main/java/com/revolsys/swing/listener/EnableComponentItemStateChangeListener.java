package com.revolsys.swing.listener;

import java.awt.Component;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class EnableComponentItemStateChangeListener implements ItemListener {
  private Component component;

  public EnableComponentItemStateChangeListener(Component component) {
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
}
