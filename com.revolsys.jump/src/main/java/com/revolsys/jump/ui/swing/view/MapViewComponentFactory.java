package com.revolsys.jump.ui.swing.view;

import java.awt.Component;

import javax.swing.Icon;

import org.openjump.swing.factory.component.ComponentFactory;

import com.revolsys.jump.ui.task.DockingTaskFrame;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;

public class MapViewComponentFactory implements ComponentFactory<Component> {
  private WorkbenchContext workbenchContext;

  private DockingTaskFrame dockingTaskFrame;

  public MapViewComponentFactory(
    final WorkbenchContext workbenchContext) {
    this.workbenchContext = workbenchContext;
  }

  public Component createComponent() {
    JUMPWorkbench workbench = workbenchContext.getWorkbench();
    WorkbenchFrame workbenchFrame = workbench.getFrame();
    DockingTaskFrame taskFrame = dockingTaskFrame;
    if (taskFrame == null) {
      taskFrame = (DockingTaskFrame)workbenchFrame.getActiveInternalFrame();
    }

    return taskFrame.getLayerViewPanel();
  }

  public Icon getIcon() {
    return null;
  }

  public String getName() {
    return "Map";
  }

  public String getToolTip() {
    return null;
  }

  public void setTaskFrame(
    final DockingTaskFrame dockingTaskFrame) {
    this.dockingTaskFrame = dockingTaskFrame;
  }

  public void close(
    final Component component) {

  }
}
