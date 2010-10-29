package com.revolsys.jump.ui.model;

import java.awt.geom.NoninvertibleTransformException;

import org.openjump.core.model.OpenJumpTaskProperties;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.AbstractLayerable;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.Viewport;

public class GridLayer extends AbstractLayerable {
  private Blackboard blackboard = new Blackboard();

  private RectangularMapGrid grid;

  private WorkbenchContext context;

  public GridLayer(
    final WorkbenchContext context,
    final String name,
    final LayerManager layerManager,
    final RectangularMapGrid grid) {
    super(name, layerManager);
    this.context = context;
    this.grid = grid;
  }

  public Blackboard getBlackboard() {
    return blackboard;
  }

  public RectangularMapGrid getGrid() {
    return grid;
  }

  public GeometryFactory getGeometryFactory() {
    return (GeometryFactory)getTask().getProperty(
      OpenJumpTaskProperties.GEOMETRY_FACTORY);
  }

  public void zoomToSheet(
    final String sheet) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory != null) {
      LayerViewPanel layerViewPanel = context.getLayerViewPanel();
      Viewport viewport = layerViewPanel.getViewport();
      try {
        Polygon polygon = grid.getTileByName(sheet).getPolygon(50);

        polygon =  GeometryProjectionUtil.perform(polygon, geometryFactory);
        viewport.zoom(polygon.getEnvelopeInternal());
      } catch (NoninvertibleTransformException e) {
      }
      layerViewPanel.fireSelectionChanged();
    }
  }
}
