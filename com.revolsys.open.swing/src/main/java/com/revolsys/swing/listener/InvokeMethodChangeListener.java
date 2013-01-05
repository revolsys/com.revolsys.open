package com.revolsys.swing.listener;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.revolsys.parallel.process.InvokeMethodRunnable;

/**
 * An ActionListener that invokes the method on the object when the action is
 * performed.
 * 
 * @author Paul Austin
 */
public class InvokeMethodChangeListener implements ChangeListener {
  private final Runnable runnable;

  private boolean invokeLater;

  public InvokeMethodChangeListener(final Class<?> clazz,
    final String methodName, final boolean invokeLater,
    final Object... parameters) {
    runnable = new InvokeMethodRunnable(clazz, methodName, parameters);
    this.invokeLater = invokeLater;
  }

  public InvokeMethodChangeListener(final Class<?> clazz,
    final String methodName, final Object... parameters) {
    runnable = new InvokeMethodRunnable(clazz, methodName, parameters);
  }

  public InvokeMethodChangeListener(final Object object, final String methodName) {
    this(object, methodName, new Object[0]);
  }

  public InvokeMethodChangeListener(final Object object,
    final String methodName, final boolean invokeLater) {
    this(object, methodName, invokeLater, new Object[0]);
  }

  public InvokeMethodChangeListener(final Object object,
    final String methodName, final boolean invokeLater,
    final Object... parameters) {
    runnable = new InvokeMethodRunnable(object, methodName, parameters);
    this.invokeLater = invokeLater;
  }

  public InvokeMethodChangeListener(final Object object,
    final String methodName, final Object... parameters) {
    this(object, methodName, false, parameters);
  }

  @Override
  public void stateChanged(final ChangeEvent e) {
    if (invokeLater) {
      SwingUtilities.invokeLater(runnable);
    } else {
      runnable.run();
    }
  }
}
