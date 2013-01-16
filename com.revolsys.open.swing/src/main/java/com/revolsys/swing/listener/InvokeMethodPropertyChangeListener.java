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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;

import com.revolsys.parallel.process.InvokeMethodRunnable;

public class InvokeMethodPropertyChangeListener implements
  PropertyChangeListener {
  private static final List<Class<PropertyChangeEvent>> EVENT_PARAMETERS = Collections.singletonList(
    PropertyChangeEvent.class
  );

  private Runnable runnable;

  private final boolean invokeLater;

  private Object object;

  private String methodName;

  public InvokeMethodPropertyChangeListener(final boolean invokeLater,
    final Object object, final String methodName, final Object... parameters) {
    this.object = object;
    this.methodName = methodName;
    if (!Arrays.asList(parameters).equals(EVENT_PARAMETERS)) {
      runnable = new InvokeMethodRunnable(object, methodName, parameters);
    }
    this.invokeLater = invokeLater;
  }

  public InvokeMethodPropertyChangeListener(final Object object,
    final String methodName, final Object... parameters) {
    this(false, object, methodName, parameters);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    Runnable runnable= this.runnable;
    if (runnable == null) {
      runnable = new InvokeMethodRunnable(object, methodName, evt);
    }
    if (invokeLater) {
      SwingUtilities.invokeLater(runnable);
    } else {
      runnable.run();
    }
  }

}
