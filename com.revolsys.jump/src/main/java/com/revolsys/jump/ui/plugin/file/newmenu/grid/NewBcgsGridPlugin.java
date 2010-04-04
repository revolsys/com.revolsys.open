package com.revolsys.jump.ui.plugin.file.newmenu.grid;

import java.util.List;

import org.openjump.core.ui.plugin.AbstractThreadedUiPlugIn;

import com.revolsys.gis.grid.Bcgs20000RectangularMapGrid;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.revolsys.jump.ui.model.GridLayer;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

public class NewBcgsGridPlugin extends AbstractThreadedUiPlugIn {

  public NewBcgsGridPlugin() {
    super("BCGS Grid");
  }

  public void initialize(final PlugInContext context) throws Exception {
    super.initialize(context);

    FeatureInstaller installer = context.getFeatureInstaller();
    installer.addMainMenuItem(new String[] {
      "File", "New", "Grid Layer"
    }, this);
  }

  public void run(final TaskMonitor monitor, final PlugInContext context)
    throws Exception {
    addLayer(context);
  }

  @SuppressWarnings("unchecked")
  public static GridLayer addLayer(final PlugInContext context) {
    LayerManager layerManager = context.getLayerManager();
    Category category = layerManager.getCategory("Grids");
    if (category == null) {
      layerManager.addCategory("Grids");
    }
    for (GridLayer layer : (List<GridLayer>)layerManager.getLayerables(GridLayer.class)) {
      if (layer.getGrid() instanceof Bcgs20000RectangularMapGrid) {
        return layer;
      }
    }
    return addGridLayer(context.getWorkbenchContext(), layerManager, "BCGS",
      new Bcgs20000RectangularMapGrid(), 200, 0);
  }

  private static GridLayer addGridLayer(
    final WorkbenchContext workbenchContext, final LayerManager layerManager,
    final String title, final RectangularMapGrid grid, final double minScale,
    final double maxScale) {
    GridLayer layer = new GridLayer(workbenchContext, title, layerManager, grid);
    layerManager.addLayerable("Grids", layer);
    layer.setMinScale(minScale);
    layer.setMaxScale(maxScale);
    layer.setScaleDependentRenderingEnabled(true);
    return layer;
  }
}
