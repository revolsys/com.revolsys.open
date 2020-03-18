package com.revolsys.swing.map.layer.record.style.marker;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.PointDoubleXYOrientation;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.io.BaseCloseable;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.util.Property;

public abstract class AbstractMarkerRenderer implements MarkerRenderer {

  protected final ViewRenderer view;

  protected final MarkerStyle style;

  protected final BaseCloseable styleCloseable;

  protected final double mapHeight;

  protected final double mapWidth;

  protected final boolean fill;

  protected final boolean stroke;

  public AbstractMarkerRenderer(final ViewRenderer view, final MarkerStyle style) {
    this.view = view;
    this.style = style;
    this.styleCloseable = view.applyMarkerStyle(style);
    final Quantity<Length> markerWidth = style.getMarkerWidth();
    final Quantity<Length> markerHeight = style.getMarkerHeight();
    this.mapWidth = view.toDisplayValue(markerWidth);
    this.mapHeight = view.toDisplayValue(markerHeight);
    this.fill = style.getMarkerFillOpacity() > 0;
    this.stroke = style.getMarkerLineOpacity() > 0;

  }

  @Override
  public void close() {
    this.styleCloseable.close();
  }

  public PointDoubleXYOrientation getMarkerLocation(final Geometry geometry) {
    final String placementType = this.style.getMarkerPlacementType();
    return this.view.getPointWithOrientation(geometry, placementType);
  }

  @Override
  public void renderMarker(final double modelX, final double modelY, final double orientation) {
    try (
      BaseCloseable closable = this.view.useViewCoordinates()) {
      translateMarker(modelX, modelY, orientation);
      renderMarkerDo();
    }
  }

  protected void renderMarkerDo() {
  }

  @Override
  public void renderMarkerGeometry(final Geometry geometry) {
    if (geometry != null) {
      if ("vertices".equals(this.style.getMarkerPlacementType())) {
        renderMarkerVertices(geometry);
      } else if ("segments".equals(this.style.getMarkerPlacementType())) {
        renderMarkerSegments(geometry);
      } else {
        for (int i = 0; i < geometry.getGeometryCount(); i++) {
          final Geometry part = geometry.getGeometry(i);
          if (part instanceof Point) {
            final Point point = (Point)part;
            renderMarkerPoint(point);
          } else if (part instanceof LineString) {
            final LineString line = (LineString)part;
            final PointDoubleXYOrientation point = getMarkerLocation(line);
            if (point != null) {
              final double orientation = point.getOrientation();
              renderMarkerPoint(point, orientation);
            }
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final Point point = polygon.getPointWithin();
            renderMarkerPoint(point);
          }
        }
      }
    }
  }

  @Override
  public void renderMarkerSegments(Geometry geometry) {
    geometry = this.view.getGeometry(geometry);
    if (Property.hasValue(geometry)) {
      final String orientationType = this.style.getMarkerOrientationType();
      if ("none".equals(orientationType)) {
        for (final Segment segment : geometry.segments()) {
          final Point point = segment.midPoint();
          renderMarkerPoint(point);
        }
      } else {
        for (final Segment segment : geometry.segments()) {
          final Point point = segment.midPoint();
          final double x = point.getX();
          final double y = point.getY();
          final double orientation = segment.getOrientaton();
          renderMarker(x, y, orientation);
        }
      }
    }
  }

  @Override
  public void renderMarkerVertices(Geometry geometry) {
    geometry = this.view.getGeometry(geometry);
    if (Property.hasValue(geometry)) {
      final String orientationType = this.style.getMarkerOrientationType();
      if ("none".equals(orientationType)) {
        renderMarkers(geometry.vertices());
      } else {
        for (final Vertex vertex : geometry.vertices()) {
          renderMarkerVertex(vertex);
        }
      }
    }
  }

  protected abstract void translateDo(final double x, final double y, final double orientation,
    final double dx, double dy);

  protected void translateMarker(final double x, final double y, double orientation) {
    final MarkerStyle style = this.style;
    final String orientationType = style.getMarkerOrientationType();
    if ("none".equals(orientationType)) {
      orientation = 0;
    }
    final double markerOrientation = style.getMarkerOrientation();
    orientation = -orientation + markerOrientation;

    final Quantity<Length> deltaX = style.getMarkerDx();
    final Quantity<Length> deltaY = style.getMarkerDy();
    double dx = this.view.toDisplayValue(deltaX);
    double dy = this.view.toDisplayValue(deltaY);
    final String verticalAlignment = style.getMarkerVerticalAlignment();
    if ("bottom".equals(verticalAlignment)) {
      dy -= this.mapHeight;
    } else if ("auto".equals(verticalAlignment) || "middle".equals(verticalAlignment)) {
      dy -= this.mapHeight / 2;
    }
    final String horizontalAlignment = style.getMarkerHorizontalAlignment();
    if ("right".equals(horizontalAlignment)) {
      dx -= this.mapWidth;
    } else if ("auto".equals(horizontalAlignment) || "center".equals(horizontalAlignment)) {
      dx -= this.mapWidth / 2;
    }

    translateDo(x, y, orientation, dx, dy);
  }

}
