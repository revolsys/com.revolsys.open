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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionListener;

import com.revolsys.jtstest.function.BaseGeometryFunction;
import com.revolsys.jtstest.function.GeometryFunction;
import com.revolsys.jtstest.testrunner.StringUtil;

/**
 * @version 1.7
 */
public class GeometryFunctionListPanel extends JPanel {
  private class GeometryFunctionCellRenderer extends JLabel implements
    ListCellRenderer {
    Border spaceBorder = BorderFactory.createEmptyBorder(0, 4, 1, 0);

    private final ImageIcon binaryIcon = new ImageIcon(this.getClass()
      .getResource("BinaryGeomFunction.png"));

    private final ImageIcon unaryIcon = new ImageIcon(this.getClass()
      .getResource("UnaryGeomFunction.png"));

    @Override
    public Component getListCellRendererComponent(final JList list,
      final Object value, final int index, final boolean isSelected,
      final boolean cellHasFocus) {
      final GeometryFunction func = (GeometryFunction)value;
      final String name = StringUtil.capitalize(func.getName());
      setBorder(spaceBorder);
      setText(name);
      setOpaque(true);
      final boolean isBinaryFunc = BaseGeometryFunction.isBinaryGeomFunction(func);
      setIcon(isBinaryFunc ? binaryIcon : unaryIcon);
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      setEnabled(list.isEnabled());
      setFont(list.getFont());
      return this;
    }

    /*
     * public String getToolTipText(MouseEvent e) { return getText(); }
     */
  }

  BorderLayout borderLayout1 = new BorderLayout();

  private final DefaultListModel listModel = new DefaultListModel();

  JScrollPane jScrollPane1 = new JScrollPane();

  JList list = new JList(listModel) {
    @Override
    public String getToolTipText(final MouseEvent e) {
      final int index = locationToIndex(e.getPoint());
      if (-1 < index) {
        final GeometryFunction func = (GeometryFunction)getModel().getElementAt(
          index);
        return func.getSignature();
      } else {
        return null;
      }
    }
  };

  BorderLayout borderLayout2 = new BorderLayout();

  Border border1;

  public GeometryFunctionListPanel() {
    try {
      jbInit();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
    list.setCellRenderer(new GeometryFunctionCellRenderer());
  }

  public GeometryFunction getFunction() {
    if (list.getSelectedValue() == null) {
      return null;
    }
    return (GeometryFunction)list.getSelectedValue();
  }

  /*
   * private void registerListSelectionListener() {
   * list.getSelectionModel().addListSelectionListener( new
   * ListSelectionListener() { public void valueChanged(ListSelectionEvent e) {
   * if (list.getSelectedValue() == null) return; } }); }
   */

  private void jbInit() throws Exception {
    setSize(200, 250);
    border1 = BorderFactory.createEmptyBorder(4, 4, 4, 4);
    setLayout(borderLayout2);
    setBorder(border1);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    add(jScrollPane1, BorderLayout.CENTER);
    jScrollPane1.getViewport().add(list, null);
  }

  public void populate(final List funcs) {
    // listModel.clear();
    for (final Iterator i = funcs.iterator(); i.hasNext();) {
      final GeometryFunction func = (GeometryFunction)i.next();
      listModel.addElement(func);
    }
  }

  public void registerListSelectionListener(final ListSelectionListener listener) {
    list.getSelectionModel().addListSelectionListener(listener);
  }

  public void registerMouseListener(final MouseListener listener) {
    list.addMouseListener(listener);
  }
}
