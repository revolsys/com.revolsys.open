package com.revolsys.swing.map.layer.dataobject;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class LayerPropertiesPanel extends JPanel {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private final JTabbedPane tabs = new JTabbedPane();

  public LayerPropertiesPanel() {
    setLayout(new BorderLayout());
    add(this.tabs, BorderLayout.CENTER);
  }

  public JTabbedPane getTabs() {
    return this.tabs;
  }
}
