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

import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.swing.parallel.SwingWorkerManager;

public class InvokeMethodListener extends InvokeMethodRunnable implements
  ActionListener, DocumentListener, ListSelectionListener, ItemListener,
  PropertyChangeListener, FocusListener {

  private final boolean invokeLater;

  public InvokeMethodListener(final boolean invokeLater, final Class<?> clazz,
    final String methodName, final Object... parameters) {
    super(clazz, methodName, parameters);
    this.invokeLater = invokeLater;
  }

  public InvokeMethodListener(final boolean invokeLater, final Object object,
    final String methodName, final Object... parameters) {
    super(object, methodName, parameters);
    this.invokeLater = invokeLater;
  }

  public InvokeMethodListener(final Class<?> clazz, final String methodName,
    final Object... parameters) {
    this(true, clazz, methodName, parameters);
  }

  public InvokeMethodListener(final Object object, final String methodName,
    final Object... parameters) {
    this(false, object, methodName, parameters);
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
    if (this.invokeLater) {
      SwingWorkerManager.invokeLater(this);
    } else {
      run();
    }
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
  public void valueChanged(final ListSelectionEvent e) {
    invokeMethod();
  }
}
