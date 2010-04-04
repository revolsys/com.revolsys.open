package com.revolsys.jump.ui.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.List;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.cs.projection.GeometryOperation;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.revolsys.gis.grid.RectangularMapTile;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.Renderer;
import com.vividsolutions.jump.workbench.ui.renderer.style.StyleUtil;

public class GridRenderer implements Renderer {

  private GridLayer gridLayer;

  private boolean cancelled;

  private volatile boolean rendering = false;

  private LayerViewPanel layerViewPanel;

  public GridRenderer(
    final GridLayer gridLayer,
    final LayerViewPanel layerViewPanel) {
    this.gridLayer = gridLayer;
    this.layerViewPanel = layerViewPanel;
  }

  public void cancel() {
    cancelled = true;
  }

  public void clearImageCache() {

  }

  public void copyTo(
    final Graphics2D graphics) {
    try {
      rendering = true;
      cancelled = false;
      if (render()) {
        paint(graphics);
      }
    } catch (Throwable t) {
      t.printStackTrace();
      return;
    } finally {
      rendering = false;
      cancelled = false;
    }
  }

  public boolean render() {
    if (!gridLayer.isVisible()) {
      return false;
    }
    if (!gridLayer.getLayerManager().getLayerables(Layerable.class).contains(
      gridLayer)) {
      return false;
    }
    return withinVisibleScaleRange();
  }

  public boolean withinVisibleScaleRange() {
    if (gridLayer.isScaleDependentRenderingEnabled()) {
      Double maxScale = gridLayer.getMaxScale();
      Double minScale = gridLayer.getMinScale();
      if (maxScale != null && minScale != null) {
        Viewport viewport = layerViewPanel.getViewport();
        double scale = 1d / viewport.getScale();
        if (scale < gridLayer.getMaxScale()) {
          return false;
        }
        if (scale > gridLayer.getMinScale()) {
          return false;
        }

      }

    }
    return true;
  }

  private void paint(
    final Graphics2D graphics) {
    Viewport viewport = layerViewPanel.getViewport();
    Envelope envelope = viewport.getEnvelopeInModelCoordinates();
    int srid = gridLayer.getSrid();
    if (srid != 0) {
      GeometryOperation operation = null;
      CoordinateSystem coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(srid);
      BoundingBox boundingBox = new BoundingBox(coordinateSystem, envelope);
      if (coordinateSystem instanceof ProjectedCoordinateSystem) {
        ProjectedCoordinateSystem projectedCs = (ProjectedCoordinateSystem)coordinateSystem;
        final GeographicCoordinateSystem geoCs = projectedCs.getGeographicCoordinateSystem();
        operation = ProjectionFactory.getGeometryOperation(geoCs,
          coordinateSystem);
      }

      RectangularMapGrid grid = gridLayer.getGrid();
      List<RectangularMapTile> tiles = grid.getTiles(boundingBox);
      for (RectangularMapTile tile : tiles) {
        if (cancelled) {
          return;
        }
        Polygon polygon = tile.getPolygon(100);
        if (operation != null) {
          polygon = operation.perform(polygon);
        }
        paint(viewport, graphics, polygon);

        Point centroid = polygon.getCentroid();
        paint(viewport, graphics, centroid, tile.getName());
      }
    }

  }

  private void paint(
    final Viewport viewport,
    final Graphics2D graphics,
    final Point location,
    final String sheet) {
    TextLayout layout = new TextLayout(sheet, new Font("Helvetica", Font.PLAIN,
      12), graphics.getFontRenderContext());

    Point2D modelPoint = new Point2D.Double(location.getCoordinate().x,
      location.getCoordinate().y);
    Point2D viewPoint = new Point2D.Double();

    try {
      viewport.getModelToViewTransform().transform(modelPoint, viewPoint);
      layout.draw(graphics, (float)viewPoint.getX()
        - layout.getVisibleAdvance() / 2, (float)viewPoint.getY());
    } catch (NoninvertibleTransformException e) {
    }
  }

  private void paint(
    final Viewport viewport,
    final Graphics2D graphics,
    final Polygon polygon) {
    try {
      StyleUtil.paint(polygon, graphics, viewport, false, null, null, true,
        new BasicStroke(), new Color(128, 128, 128));
    } catch (NoninvertibleTransformException e) {
    }
  }

  public Runnable createRunnable() {
    return null;
  }

  public Object getContentID() {
    return gridLayer;
  }

  public boolean isRendering() {
    return rendering;
  }
}
