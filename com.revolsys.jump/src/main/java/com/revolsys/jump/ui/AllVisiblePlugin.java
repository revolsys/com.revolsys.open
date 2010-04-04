package com.revolsys.jump.ui;

import java.util.Collection;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

public class AllVisiblePlugin extends AbstractPlugIn {
  public static final String SHOW_ALL = I18N.getText("com.revolsys.jump",
    AllVisiblePlugin.class.getName() + ".show-all");

  public static final String HIDE_ALL = "Hide All Layers";

  private static final String errorSeeOutputWindow = I18N.get("org.openjump.core.ui.plugin.layer.ToggleVisiblityPlugIn.Error-See-Output-Window");

  private boolean visible;

  private String label;

  public AllVisiblePlugin(final boolean visible) {
    this.visible = visible;
    if (visible) {
      this.label = SHOW_ALL;
    } else {
      this.label = HIDE_ALL;
    }
  }

  public void initialize(final PlugInContext context) throws Exception {
    WorkbenchContext workbenchContext = context.getWorkbenchContext();
    FeatureInstaller featureInstaller = new FeatureInstaller(workbenchContext);
    featureInstaller.addLayerViewMenuItem(this, "View", label);
  }

  @SuppressWarnings("unchecked")
  public boolean execute(final PlugInContext context) throws Exception {
    try {
      context.getWorkbenchFrame().getOutputFrame().createNewDocument();
      WorkbenchContext workbenchContext = context.getWorkbenchContext();
      LayerNamePanel layerNamePanel = workbenchContext.getLayerNamePanel();

      Collection<Layerable> layers = layerNamePanel.getLayerManager()
        .getLayers();
      for (Layerable layer : layers) {
        layer.setVisible(visible);
      }
      return true;

    } catch (Exception e) {
      context.getWorkbenchFrame().warnUser(errorSeeOutputWindow);
      context.getWorkbenchFrame().getOutputFrame().createNewDocument();
      context.getWorkbenchFrame().getOutputFrame().addText(
        getClass() + " Exception:" + e.toString());
      return false;
    }
  }
}
