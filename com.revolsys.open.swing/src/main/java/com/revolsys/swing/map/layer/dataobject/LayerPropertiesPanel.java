package com.revolsys.swing.map.layer.dataobject;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class LayerPropertiesPanel extends JPanel {
  private JTabbedPane tabs = new JTabbedPane();

  public LayerPropertiesPanel() {
    setLayout(new BorderLayout());
    add(tabs, BorderLayout.CENTER);
  }

  public JTabbedPane getTabs() {
    return tabs;
  }
}
