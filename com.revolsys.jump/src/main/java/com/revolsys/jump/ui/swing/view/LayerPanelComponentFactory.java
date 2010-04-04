package com.revolsys.jump.ui.swing.view;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreeCellRenderer;

import org.openjump.swing.factory.component.ComponentFactory;

import com.revolsys.jump.ui.model.LayerTreeModel;
import com.revolsys.jump.ui.model.TreeLayerNamePanel;
import com.revolsys.jump.ui.task.DockingTaskFrame;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.renderer.RenderingManager;

public class LayerPanelComponentFactory implements ComponentFactory<Component> {
  private WorkbenchContext workbenchContext;

  private DockingTaskFrame dockingTaskFrame;

  public LayerPanelComponentFactory(
    final WorkbenchContext workbenchContext) {
    this.workbenchContext = workbenchContext;
  }

  @SuppressWarnings("unchecked")
  public Component createComponent() {
    JUMPWorkbench workbench = workbenchContext.getWorkbench();
    WorkbenchFrame workbenchFrame = workbench.getFrame();
    DockingTaskFrame taskFrame = dockingTaskFrame;
    if (taskFrame == null) {
      taskFrame = (DockingTaskFrame)workbenchFrame.getActiveInternalFrame();
    }
    LayerViewPanel layerViewPanel = taskFrame.getLayerViewPanel();
    RenderingManager renderingManager = layerViewPanel.getRenderingManager();
    TreeLayerNamePanel treeLayerNamePanel = new TreeLayerNamePanel(taskFrame,
      new LayerTreeModel(taskFrame), renderingManager,
      new HashMap<Class<?>, TreeCellRenderer>());
    Map<Class<?>, JPopupMenu> menus = workbenchFrame.getNodeClassToPopupMenuMap();
    for (Entry<Class<?>, JPopupMenu> entry : menus.entrySet()) {
      Class<?> nodeClass = entry.getKey();
      JPopupMenu menu = entry.getValue();
      treeLayerNamePanel.addPopupMenu(nodeClass, menu);
    }
    return treeLayerNamePanel;
  }

  public Icon getIcon() {
    return null;
  }

  public String getName() {
    return "Layers";
  }

  public String getToolTip() {
    return null;
  }

  public void setTaskFrame(
    final DockingTaskFrame taskFrame) {
    this.dockingTaskFrame = taskFrame;

  }

  public void close(
    final Component component) {

  }
}
