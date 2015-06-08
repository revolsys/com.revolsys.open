package com.revolsys.swing.listener;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.swing.parallel.Invoke;

public class InvokeMethodSelectedItemListener implements ItemListener {

  private final boolean invokeLater;

  private final Object object;

  private final String methodName;

  public InvokeMethodSelectedItemListener(final boolean invokeLater, final Object object,
    final String methodName) {
    this.object = object;
    this.methodName = methodName;
    this.invokeLater = invokeLater;
  }

  public InvokeMethodSelectedItemListener(final Object object, final String methodName) {
    this(true, object, methodName);
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      final Runnable runnable = new InvokeMethodRunnable(this.object, this.methodName, e.getItem());
      if (this.invokeLater) {
        Invoke.later(runnable);
      } else {
        runnable.run();
      }
    }
  }
}
