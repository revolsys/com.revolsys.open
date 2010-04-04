package com.revolsys.jump.ui.plugin.layer;

import java.util.Collection;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.openjump.core.ui.plugin.AbstractThreadedUiPlugIn;

import com.revolsys.jump.ui.model.GridLayer;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

public class ZoomToGridLayerSheet extends AbstractThreadedUiPlugIn {

  private String sheet;

  public ZoomToGridLayerSheet() {
    super("Zoom to Sheet");
  }

  @SuppressWarnings("unchecked")
  public void initialize(final PlugInContext context) throws Exception {
    super.initialize(context);
    WorkbenchFrame workbenchFrame = context.getWorkbenchFrame();
    Map<Class<?>, JPopupMenu> menus = workbenchFrame.getNodeClassToPopupMenuMap();
    JPopupMenu menu = menus.get(GridLayer.class);
    FeatureInstaller installer = context.getFeatureInstaller();
    installer.addPopupMenuItem(menu, this, this.getName(), false, null, null);
  }

  @SuppressWarnings("unchecked")
  public boolean execute(final PlugInContext context) throws Exception {
    LayerNamePanel layerNamePanel = context.getLayerNamePanel();
    Preferences preferences = null;
    for (GridLayer layer : (Collection<GridLayer>)layerNamePanel.selectedNodes(GridLayer.class)) {
      Class<?> layerClass = layer.getGrid().getClass();
      String className = layerClass.getSimpleName();
      preferences = Preferences.userNodeForPackage(layerClass).node(className);
      sheet = preferences.get("zoomSheet", "");

    }
    sheet = JOptionPane.showInputDialog("Enter name of the sheet to Zoom to",
      sheet);
    if (sheet != null) {
      preferences.put("zoomSheet", sheet);
      return true;
    } else {
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  public void run(final TaskMonitor monitor, final PlugInContext context)
    throws Exception {
    LayerNamePanel layerNamePanel = context.getLayerNamePanel();
    for (GridLayer layer : (Collection<GridLayer>)layerNamePanel.selectedNodes(GridLayer.class)) {
      layer.zoomToSheet(sheet);
    }

  }
}
