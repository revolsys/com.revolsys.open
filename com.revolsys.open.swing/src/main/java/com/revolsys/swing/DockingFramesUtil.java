package com.revolsys.swing;

import java.awt.Dimension;

import javax.swing.JComponent;

import bibliothek.gui.dock.common.CContentArea;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CWorkingArea;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.intern.CControlAccess;

public class DockingFramesUtil {

  public static DefaultSingleCDockable addDockable(
    final CWorkingArea workingArea, final String id, final String title,
    final JComponent component) {
    CControlAccess controlAccess = workingArea.getControl();
    CControl control = controlAccess.getOwner();
    final DefaultSingleCDockable dockable = new DefaultSingleCDockable(id,
      title, component);
    control.addDockable(dockable);
    dockable.setWorkingArea(workingArea);
    dockable.setVisible(true);
    dockable.setMinimizable(false);
    return dockable;
  }

  public static void setFlapSizes(CControl control) {
    CContentArea contentArea = control.getContentArea();
    Dimension size = new Dimension(2, 2);
//    contentArea.setMinimumAreaSize(size);
//    contentArea.getNorthArea().setPreferredSize(size);
    contentArea.getEastArea().setPreferredSize(size);
    contentArea.getSouthArea().setPreferredSize(size);
    contentArea.getWestArea().setPreferredSize(size);
  }

}
