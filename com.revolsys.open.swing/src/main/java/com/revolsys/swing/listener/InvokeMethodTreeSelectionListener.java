package com.revolsys.swing.listener;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import com.revolsys.parallel.process.InvokeMethodRunnable;

/**
 * An ActionListener that invokes the method on the object when the action is
 * performed.
 * 
 * @author Paul Austin
 */
public class InvokeMethodTreeSelectionListener implements TreeSelectionListener {
  private final Runnable runnable;

  private final boolean invokeLater;

  public InvokeMethodTreeSelectionListener(final Object object,
    final String methodName) {
    this(object, methodName, new Object[0]);
  }

  public InvokeMethodTreeSelectionListener(final Object object,
    final String methodName, final boolean invokeLater) {
    this(object, methodName, invokeLater, new Object[0]);
  }

  public InvokeMethodTreeSelectionListener(final Object object,
    final String methodName, final boolean invokeLater,
    final Object... parameters) {
    runnable = new InvokeMethodRunnable(object, methodName, parameters);
    this.invokeLater = invokeLater;
  }

  public InvokeMethodTreeSelectionListener(final Object object,
    final String methodName, final Object... parameters) {
    this(object, methodName, false, parameters);
  }

  @Override
  public void valueChanged(final TreeSelectionEvent e) {
    if (invokeLater) {
      SwingUtilities.invokeLater(runnable);
    } else {
      runnable.run();
    }
  }

}
