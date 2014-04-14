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
import java.awt.Color;
import java.awt.SystemColor;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.revolsys.jtstest.testbuilder.model.Layer;
import com.revolsys.jtstest.testbuilder.model.LayerList;

/**
 * @version 1.7
 */
public class LayerListPanel extends JPanel {
    BorderLayout borderLayout1 = new BorderLayout();
    private DefaultListModel listModel = new DefaultListModel();
    JScrollPane jScrollPane1 = new JScrollPane();
    LayerCheckBoxList list = new LayerCheckBoxList(listModel);
    BorderLayout borderLayout2 = new BorderLayout();

    public LayerListPanel() {
        try {
            uiInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        registerListSelectionListener();
    }

    private void uiInit() throws Exception {
        setSize(200, 250);
        setLayout(borderLayout2);
        list.setBackground(SystemColor.control);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectionBackground(Color.GRAY);
        add(jScrollPane1, BorderLayout.CENTER);
        jScrollPane1.getViewport().add(list, null);
    }

    private void registerListSelectionListener() {
        list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (list.getSelectedValue() == null)
                    return;
//TODO: implement event logic        }
        }});
    }

    public void populateList() {
        listModel.clear();
        LayerList lyrList = JTSTestBuilderFrame.instance().getModel().getLayers();
        
        for (int i = 0; i < lyrList.size(); i++) {
          Layer lyr = lyrList.getLayer(i);
          listModel.addElement(lyr);
        }
    }

    
}


