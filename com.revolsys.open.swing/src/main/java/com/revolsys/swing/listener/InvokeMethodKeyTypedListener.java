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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.swing.parallel.Invoke;

/**
 * An ActionListener that invokes the method on the object when the action is
 * performed.
 *
 * @author Paul Austin
 */
public class InvokeMethodKeyTypedListener extends KeyAdapter {
  private final boolean invokeLater;

  private final Runnable runnable;

  public InvokeMethodKeyTypedListener(final Object object, final String methodName) {
    this(object, methodName, new Object[0]);
  }

  public InvokeMethodKeyTypedListener(final Object object, final String methodName,
    final boolean invokeLater) {
    this(object, methodName, new Object[0], invokeLater);
  }

  public InvokeMethodKeyTypedListener(final Object object, final String methodName,
    final Object... parameters) {
    this(object, methodName, parameters, false);
  }

  public InvokeMethodKeyTypedListener(final Object object, final String methodName,
    final Object[] parameters, final boolean invokeLater) {
    this.runnable = new InvokeMethodRunnable(object, methodName, parameters);
    this.invokeLater = invokeLater;
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    if (this.invokeLater) {
      Invoke.later(this.runnable);
    } else {
      this.runnable.run();
    }
  }
}
