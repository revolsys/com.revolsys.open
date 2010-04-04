package com.revolsys.jump.ui.task;

import java.awt.Component;

import javax.swing.Icon;

import org.openjump.swing.factory.component.ComponentFactory;

import com.revolsys.jump.ui.swing.view.InfoTableViewFactory;
import com.revolsys.jump.ui.swing.view.LayerPanelComponentFactory;
import com.revolsys.jump.ui.swing.view.MapViewComponentFactory;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.TaskFrame;

public class DockingTaskFrameFactory implements ComponentFactory<TaskFrame> {

  private WorkbenchContext workbenchContext;

  private LayerPanelComponentFactory layersComponentFactory;

  private MapViewComponentFactory mapComponentFactory;

  private InfoTableViewFactory infoTableViewFactory;

  public DockingTaskFrameFactory(
    final WorkbenchContext workbenchContext,
    final LayerPanelComponentFactory layersComponentFactory,
    final MapViewComponentFactory mapComponentFactory,
    final InfoTableViewFactory infoTableViewFactory) {
    this.workbenchContext = workbenchContext;
    this.layersComponentFactory = layersComponentFactory;
    this.mapComponentFactory = mapComponentFactory;
    this.infoTableViewFactory = infoTableViewFactory;
  }

  public TaskFrame createComponent() {
    DockingTaskFrame frame = new DockingTaskFrame(workbenchContext);
    frame.setLayersComponentFactory(layersComponentFactory);
    frame.setMapComponentFactory(mapComponentFactory);
    frame.setInfoComponentFactory(infoTableViewFactory);
    return frame;
  }

  public Icon getIcon() {
    return null;
  }

  public String getName() {
    return null;
  }

  public String getToolTip() {
    return null;
  }

  public void close(
    final Component component) {

  }
}
