/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.jtstest.testbuilder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import com.revolsys.jtstest.function.BaseGeometryFunction;
import com.revolsys.jtstest.function.DoubleKeyMap;
import com.revolsys.jtstest.function.GeometryFunction;
import com.revolsys.jtstest.testrunner.StringUtil;

/**
 * @version 1.7
 */
public class GeometryFunctionTreePanel extends JPanel {
  private class GeometryFunctionRenderer extends DefaultTreeCellRenderer {
    private final ImageIcon binaryIcon = new ImageIcon(this.getClass()
      .getResource("BinaryGeomFunction.png"));

    private final ImageIcon unaryIcon = new ImageIcon(this.getClass()
      .getResource("UnaryGeomFunction.png"));

    public GeometryFunctionRenderer() {
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree tree,
      final Object value, final boolean sel, final boolean expanded,
      final boolean leaf, final int row, final boolean hasFocus) {

      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,
        hasFocus);
      if (leaf) {
        final GeometryFunction func = getFunctionFromNode(value);
        final boolean isBinaryFunc = BaseGeometryFunction.isBinaryGeomFunction(func);
        setIcon(isBinaryFunc ? binaryIcon : unaryIcon);
        // setToolTipText("This book is in the Tutorial series.");
        final String name = StringUtil.capitalize(func.getName());
        setText(name);
        setToolTipText(func.getSignature() + func.getDescription()); // no tool
                                                                     // tip
      } else {
        setToolTipText(null); // no tool tip
      }
      return this;
    }

  }

  private static GeometryFunction getFunctionFromNode(final Object value) {
    final DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
    if (node == null) {
      return null;
    }
    final Object nodeValue = node.getUserObject();
    if (nodeValue instanceof GeometryFunction) {
      return (GeometryFunction)nodeValue;
    }
    return null;
  }

  JScrollPane jScrollPane = new JScrollPane();

  JTree tree = new JTree();

  BorderLayout borderLayout = new BorderLayout();

  Border border1;

  private transient Vector eventListeners;

  public GeometryFunctionTreePanel() {
    try {
      jbInit();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }

  public synchronized void addGeometryFunctionListener(
    final GeometryFunctionListener l) {
    final Vector v = eventListeners == null ? new Vector(2)
      : (Vector)eventListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      eventListeners = v;
    }
  }

  private TreeModel createModel(final DoubleKeyMap funcMap) {
    final DefaultMutableTreeNode top = new DefaultMutableTreeNode();

    final Collection categories = funcMap.keySet();
    for (final Iterator i = categories.iterator(); i.hasNext();) {
      final String category = (String)i.next();
      final DefaultMutableTreeNode catNode = new DefaultMutableTreeNode(
        category);
      top.add(catNode);

      final Collection funcs = funcMap.values(category);
      for (final Iterator j = funcs.iterator(); j.hasNext();) {
        final Object func = j.next();
        catNode.add(new DefaultMutableTreeNode(func));
      }
    }
    return new DefaultTreeModel(top);
  }

  protected void fireFunctionInvoked(final GeometryFunctionEvent e) {
    if (eventListeners != null) {
      final Vector listeners = eventListeners;
      final int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((GeometryFunctionListener)listeners.elementAt(i)).functionInvoked(e);
      }
    }
  }

  protected void fireFunctionSelected(final GeometryFunctionEvent e) {
    if (eventListeners != null) {
      final Vector listeners = eventListeners;
      final int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((GeometryFunctionListener)listeners.elementAt(i)).functionSelected(e);
      }
    }
  }

  public GeometryFunction getFunction() {
    return getFunctionFromNode(tree.getLastSelectedPathComponent());
  }

  private void jbInit() throws Exception {
    setSize(200, 250);
    border1 = BorderFactory.createEmptyBorder(4, 4, 4, 4);
    setLayout(borderLayout);
    setBorder(border1);
    add(jScrollPane, BorderLayout.CENTER);
    jScrollPane.getViewport().add(tree, null);

    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setCellRenderer(new GeometryFunctionRenderer());
    tree.getSelectionModel().setSelectionMode(
      TreeSelectionModel.SINGLE_TREE_SELECTION);

    tree.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2) {
          final GeometryFunction fun = getFunction();
          if (fun != null) {
            fireFunctionInvoked(new GeometryFunctionEvent(fun));
          }
        }

      }
    });
    tree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(final TreeSelectionEvent e) {
        final GeometryFunction fun = getFunction();
        if (fun != null) {
          fireFunctionSelected(new GeometryFunctionEvent(fun));
        }
      }
    });
  }

  public void populate(final DoubleKeyMap funcs) {
    tree.setModel(createModel(funcs));
  }

  public synchronized void removeGeometryFunctionListener(
    final GeometryFunctionListener l) {
    if (eventListeners != null && eventListeners.contains(l)) {
      final Vector v = (Vector)eventListeners.clone();
      v.removeElement(l);
      eventListeners = v;
    }
  }

}
