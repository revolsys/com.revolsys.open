package com.revolsys.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.revolsys.parallel.process.InvokeMethodRunnable;

public class InvokeMethodPropertyChangeListener extends InvokeMethodRunnable
  implements PropertyChangeListener, NonWeakListener {

  public InvokeMethodPropertyChangeListener(final Class<?> clazz,
    final String methodName, final Object... parameters) {
    super(clazz, methodName, parameters);
  }

  public InvokeMethodPropertyChangeListener(final Object object,
    final String methodName, final Object... parameters) {
    super(object, methodName, parameters);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    run();
  }
}
