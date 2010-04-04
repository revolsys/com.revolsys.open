package com.revolsys.jump.ui.plugin.file.newmenu.grid;

import org.openjump.core.ui.plugin.AbstractThreadedUiPlugIn;

import com.revolsys.gis.grid.Nts250000RectangularMapGrid;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.revolsys.jump.ui.model.GridLayer;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

public class NewNtsGridPlugin extends AbstractThreadedUiPlugIn {

  public NewNtsGridPlugin() {
    super("NTS 250k Grid");
  }

  public void initialize(final PlugInContext context) throws Exception {
    super.initialize(context);

    FeatureInstaller installer = context.getFeatureInstaller();
    installer.addMainMenuItem(new String[] {
      "File", "New", "Grid Layer"
    }, this);
  }

  public void run(final TaskMonitor monitor, final PlugInContext context) throws Exception {
    LayerManager layerManager = context.getLayerManager();
    Category category = layerManager.getCategory("Grids");
    if (category == null) {
      layerManager.addCategory("Grids");
    }
    if (layerManager.getLayer("NTS 250k") == null) {
      addGridLayer(layerManager, "NTS", new Nts250000RectangularMapGrid(), Double.MAX_VALUE,
        200);
    }
  }

  private void addGridLayer(final LayerManager layerManager, final String title,
    final RectangularMapGrid grid, final double minScale, final double maxScale) {
    GridLayer layer = new GridLayer(workbenchContext, title, layerManager, grid);
    layerManager.addLayerable("Grids", layer);
    layer.setMinScale(minScale);
    layer.setMaxScale(maxScale);
    layer.setScaleDependentRenderingEnabled(true);
  }
}
