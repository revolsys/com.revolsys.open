package com.revolsys.swing.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;

import com.revolsys.parallel.process.InvokeMethodRunnable;

/**
 * An ActionListener that invokes the method on the object when the action is
 * performed.
 * 
 * @author Paul Austin
 */
public class InvokeMethodActionListener implements ActionListener {
  private final Runnable runnable;

  private final boolean invokeLater;

  public InvokeMethodActionListener(final boolean invokeLater,
    final Class<?> clazz, final String methodName, final Object... parameters) {
    runnable = new InvokeMethodRunnable(clazz, methodName, parameters);
    this.invokeLater = invokeLater;
  }

  public InvokeMethodActionListener(final Class<?> clazz,
    final String methodName, final Object... parameters) {
    this(false, clazz, methodName, parameters);
  }

  public InvokeMethodActionListener(final boolean invokeLater,
    final Object object, final String methodName, final Object... parameters) {
    runnable = new InvokeMethodRunnable(object, methodName, parameters);
    this.invokeLater = invokeLater;
  }

  public InvokeMethodActionListener(final Object object,
    final String methodName, final Object... parameters) {
    this(false, object, methodName, parameters);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    if (invokeLater) {
      SwingUtilities.invokeLater(runnable);
    } else {
      runnable.run();
    }
  }

}
