package com.revolsys.jump.ui.model;

import java.awt.geom.NoninvertibleTransformException;

import javax.xml.namespace.QName;

import org.openjump.core.ccordsys.epsg.EpsgConstants;
import org.openjump.core.model.OpenJumpTaskProperties;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.cs.projection.GeometryOperation;
import com.revolsys.gis.cs.projection.ProjectionFactory;
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

  public int getSrid() {
    QName srid = getTask().getProperty(OpenJumpTaskProperties.SRID);
    if (srid == null) {
      return 0;
    } else {
      return EpsgConstants.getSrid(srid);
    }
  }

  public void zoomToSheet(
    final String sheet) {
    final int srid = getSrid();
    if (srid != 0) {
      LayerViewPanel layerViewPanel = context.getLayerViewPanel();
      Viewport viewport = layerViewPanel.getViewport();
      try {
        CoordinateSystem coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(srid);
        GeographicCoordinateSystem geographicCs;
        GeometryOperation operation = null;
        if (coordinateSystem instanceof GeographicCoordinateSystem) {
          geographicCs = (GeographicCoordinateSystem)coordinateSystem;
        } else if (coordinateSystem instanceof ProjectedCoordinateSystem) {
          geographicCs = ((ProjectedCoordinateSystem)coordinateSystem).getGeographicCoordinateSystem();
          operation = ProjectionFactory.getGeometryOperation(geographicCs,
            coordinateSystem);
        } else {
          return;
        }
        Polygon polygon = grid.getTileByName(sheet).getPolygon(50);
        if (operation != null) {
          polygon = operation.perform(polygon);
        }
        viewport.zoom(polygon.getEnvelopeInternal());
      } catch (NoninvertibleTransformException e) {
      }
      layerViewPanel.fireSelectionChanged();
    }
  }
}
