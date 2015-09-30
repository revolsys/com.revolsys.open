package com.revolsys.swing.listener;

import java.awt.Component;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.revolsys.beans.NonWeakListener;
import com.revolsys.swing.parallel.Invoke;

public class EnableComponentListener implements ItemListener, ListSelectionListener,
  PropertyChangeListener, DocumentListener, NonWeakListener {
  private final Component component;

  public EnableComponentListener(final Component component) {
    this.component = component;
  }

  @Override
  public void changedUpdate(final DocumentEvent e) {
    if (e.getDocument().getLength() == 0) {
      this.component.setEnabled(false);
    } else {
      this.component.setEnabled(true);
    }
  }

  @Override
  public void insertUpdate(final DocumentEvent e) {
    changedUpdate(e);
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    final ItemSelectable itemSelectable = e.getItemSelectable();
    final Object[] selectedObjects = itemSelectable.getSelectedObjects();
    if (selectedObjects == null) {
      this.component.setEnabled(false);
    } else {
      this.component.setEnabled(true);
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object newValue = event.getNewValue();
    if (newValue instanceof Boolean) {
      final Boolean enabled = (Boolean)newValue;
      Invoke.later(() -> this.component.setEnabled(enabled));
    }
  }

  @Override
  public void removeUpdate(final DocumentEvent e) {
    changedUpdate(e);
  }

  @Override
  public void valueChanged(final ListSelectionEvent e) {
    if (!e.getValueIsAdjusting()) {

      final int firstIndex = e.getFirstIndex();
      if (firstIndex == -1) {
        this.component.setEnabled(false);
      } else {
        this.component.setEnabled(true);
      }
    }
  }
}
