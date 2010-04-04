package com.revolsys.jump.ui;

import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

public class VisibilityPlugin extends AbstractPlugIn {
  public static final String SHOW = "Show";

  public static final String HIDE = "Hide";

  static final String errorSeeOutputWindow = I18N.get("org.openjump.core.ui.plugin.layer.ToggleVisiblityPlugIn.Error-See-Output-Window");

  private String label;

  private boolean visible;

  public VisibilityPlugin(final boolean visible) {
    this.visible = visible;
    if (visible) {
      this.label = SHOW;
    } else {
      this.label = HIDE;
    }
  }

  public void initialize(final PlugInContext context) throws Exception {
    WorkbenchContext workbenchContext = context.getWorkbenchContext();
    FeatureInstaller featureInstaller = new FeatureInstaller(workbenchContext);
    WorkbenchFrame frame = workbenchContext.getWorkbench().getFrame();

    JPopupMenu categoryPopupMenu = frame.getCategoryPopupMenu();
    featureInstaller.addPopupMenuItem(categoryPopupMenu, this, label, false,
      null, VisibilityPlugin.createEnableCheck(workbenchContext));

    JPopupMenu layerPopupMenu = frame.getLayerNamePopupMenu();
    featureInstaller.addPopupMenuItem(layerPopupMenu, this, label, false, null,
      VisibilityPlugin.createEnableCheck(workbenchContext));

    JPopupMenu wmsLayerPopupMenu = frame.getWMSLayerNamePopupMenu();
    featureInstaller.addPopupMenuItem(wmsLayerPopupMenu, this, label, false,
      null, VisibilityPlugin.createEnableCheck(workbenchContext));
  }

  @SuppressWarnings("unchecked")
  public boolean execute(final PlugInContext context) throws Exception {
    try {
      context.getWorkbenchFrame().getOutputFrame().createNewDocument();
      LayerNamePanel layerNamePanel = context.getWorkbenchContext()
        .getLayerNamePanel();
      Collection<Category> categories = layerNamePanel.getSelectedCategories();
      for (Category category : categories) {
        CategoryUtil.setVisible(category, visible);
      }
      Collection<Layerable> layers = layerNamePanel.selectedNodes(Layerable.class);
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

  public static MultiEnableCheck createEnableCheck(
    final WorkbenchContext workbenchContext) {
    EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
    return new MultiEnableCheck().add(
      checkFactory.createWindowWithSelectionManagerMustBeActiveCheck()).add(
      createAtLeastNLayerablesOrCategoriesMustBeSelectedCheck(1,
        workbenchContext));
  }

  public static EnableCheck createAtLeastNLayerablesOrCategoriesMustBeSelectedCheck(
    final int n, final WorkbenchContext workbenchContext) {
    return new EnableCheck() {
      public String check(final JComponent component) {
        LayerNamePanel panel = workbenchContext.getLayerNamePanel();
        LayerNamePanel layerNamePanel = panel;
        if (layerNamePanel == null
          || n > panel.selectedNodes(Layerable.class).size()
            + panel.getSelectedCategories().size()) {
          return "At least " + n + " layer or categor" + StringUtil.ies(n)
            + " must be selected";
        } else {
          return null;
        }
      }
    };
  }
}
