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

import javax.swing.SwingUtilities;

import com.revolsys.parallel.process.InvokeMethodRunnable;

public class InvokeMethodPropertyChangeListener implements
  PropertyChangeListener {
  private final Runnable runnable;

  private final boolean invokeLater;

  public InvokeMethodPropertyChangeListener(final Object object,
    final String methodName, final boolean invokeLater) {
    this(object, methodName, invokeLater, new Object[0]);
  }

  public InvokeMethodPropertyChangeListener(final Object object,
    final String methodName, final Object... parameters) {
    this(object, methodName, false, parameters);
  }

  public InvokeMethodPropertyChangeListener(final Object object,
    final String methodName, final boolean invokeLater,
    final Object... parameters) {
    runnable = new InvokeMethodRunnable(object, methodName, parameters);
    this.invokeLater = invokeLater;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    if (invokeLater) {
      SwingUtilities.invokeLater(runnable);
    } else {
      runnable.run();
    }
  }

}
