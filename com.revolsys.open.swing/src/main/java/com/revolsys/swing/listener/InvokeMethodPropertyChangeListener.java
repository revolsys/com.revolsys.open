/* *****************************************************************************
 The Open Java Unified Mapping Platform (OpenJUMP) is an extensible, interactive
 GUI for visualizing and manipulating spatial features with geometry and
 attributes. 

 Copyright (C) 2007  Revolution Systems Inc.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

 For more information see:
 
 http://openjump.org/

 ******************************************************************************/
package com.revolsys.swing.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.swing.parallel.Invoke;

public class InvokeMethodPropertyChangeListener implements
  PropertyChangeListener {

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
    return InvokeMethodRunnable.toString(object, methodName, parameters);
  }
}
