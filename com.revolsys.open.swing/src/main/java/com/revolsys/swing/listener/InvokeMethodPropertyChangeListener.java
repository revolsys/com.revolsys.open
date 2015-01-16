package com.revolsys.swing.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.revolsys.beans.NonWeakListener;
import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;

public class InvokeMethodPropertyChangeListener implements
PropertyChangeListener, NonWeakListener {

  private final boolean invokeLater;

  private final Object object;

  private final String methodName;

  private final Object[] parameters;

  public InvokeMethodPropertyChangeListener(final boolean invokeLater,
    final Object object, final String methodName, final Object... parameters) {
    this.object = object;
    this.methodName = methodName;
    this.parameters = parameters;
    this.invokeLater = invokeLater;
  }

  public InvokeMethodPropertyChangeListener(final Object object,
    final String methodName, final Object... parameters) {
    this(true, object, methodName, parameters);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    final Object[] parameters = new Object[this.parameters.length];
    for (int i = 0; i < this.parameters.length; i++) {
      Object parameter = this.parameters[i];
      if (parameter instanceof Class<?>) {
        final Class<?> parameterClass = (Class<?>)parameter;
        if (PropertyChangeEvent.class.isAssignableFrom(parameterClass)) {
          parameter = evt;
        }
      }
      parameters[i] = parameter;
    }
    final Runnable runnable = new InvokeMethodRunnable(this.object,
      this.methodName, parameters);
    if (this.invokeLater) {
      Invoke.later(runnable);
    } else {
      runnable.run();
    }
  }

  @Override
  public String toString() {
    return Property.toString(this.object, this.methodName, this.parameters);
  }
}
