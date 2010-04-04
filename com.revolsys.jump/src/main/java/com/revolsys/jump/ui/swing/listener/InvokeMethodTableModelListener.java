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
package com.revolsys.jump.ui.swing.listener;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.openjump.swing.util.InvokeMethodRunnable;

/**
 * An TableModelListener that invokes the method on the object when the table
 * model is changed is performed.
 * 
 * @author Paul Austin
 */
public class InvokeMethodTableModelListener implements TableModelListener {
  private Runnable runnable;

  private boolean invokeLater;

  public InvokeMethodTableModelListener(final Object object,
    final String methodName) {
    this(object, methodName, new Object[0]);
  }

  public InvokeMethodTableModelListener(final Object object,
    final String methodName, final boolean invokeLater) {
    this(object, methodName, new Object[0], invokeLater);
  }

  public InvokeMethodTableModelListener(final Object object,
    final String methodName, final Object[] parameters) {
    this(object, methodName, parameters, false);
  }

  public InvokeMethodTableModelListener(final Object object,
    final String methodName, final Object[] parameters, final boolean invokeLater) {
    runnable = new InvokeMethodRunnable(object, methodName, parameters);
    this.invokeLater = invokeLater;
  }

  public void tableChanged(final TableModelEvent e) {
    if (invokeLater) {
      SwingUtilities.invokeLater(runnable);
    } else {
      runnable.run();
    }
  }
}
