package com.revolsys.swing.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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

public class EventQueueRunnableListener
  implements ActionListener, DocumentListener, ListSelectionListener, ItemListener,
  PropertyChangeListener, FocusListener, NonWeakListener, Runnable {

  private final Runnable runnable;

  public EventQueueRunnableListener(final Runnable runnable) {
    this.runnable = runnable;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    invokeMethod();
  }

  @Override
  public void changedUpdate(final DocumentEvent e) {
    invokeMethod();
  }

  @Override
  public void focusGained(final FocusEvent e) {
    invokeMethod();
  }

  @Override
  public void focusLost(final FocusEvent e) {
    invokeMethod();
  }

  @Override
  public void insertUpdate(final DocumentEvent e) {
    invokeMethod();
  }

  protected void invokeMethod() {
    Invoke.later(this.runnable);
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    invokeMethod();
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    invokeMethod();
  }

  @Override
  public void removeUpdate(final DocumentEvent e) {
    invokeMethod();
  }

  @Override
  public void run() {
    invokeMethod();
  }

  @Override
  public void valueChanged(final ListSelectionEvent e) {
    invokeMethod();
  }
}
