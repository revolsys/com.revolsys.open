package com.revolsys.jump.ui.swing;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.vividsolutions.jump.workbench.ui.EnableableToolBar;

public class ToolbarListSelectionListener implements ListSelectionListener {
  private EnableableToolBar toolBar;

  public ToolbarListSelectionListener(final EnableableToolBar toolBar) {
    this.toolBar = toolBar;
  }

  public void valueChanged(final ListSelectionEvent e) {
    toolBar.updateEnabledState();
  }

}
