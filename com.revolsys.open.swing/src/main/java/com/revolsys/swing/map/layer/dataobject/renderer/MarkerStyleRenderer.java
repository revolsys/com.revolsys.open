package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.CoordinatesWithOrientation;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.layer.dataobject.style.MarkerStyle;
import com.revolsys.swing.map.layer.dataobject.style.marker.Marker;
import com.revolsys.swing.map.layer.dataobject.style.panel.MarkerStylePanel;
import com.revolsys.util.MathUtil;

public class MarkerStyleRenderer extends AbstractDataObjectLayerRenderer {

  private static final Geometry EMPTY_GEOMETRY = GeometryFactory.getFactory()
    .geometry();

  private static final Icon ICON = SilkIconLoader.getIcon("style_marker");

  public static Geometry getGeometry(final Viewport2D viewport,
    final Geometry geometry) {
    final BoundingBox viewExtent = viewport.getBoundingBox();
    if (geometry != null) {
      if (!viewExtent.isEmpty()) {
        final BoundingBox geometryExtent = BoundingBox.getBoundingBox(geometry);
        if (geometryExtent.intersects(viewExtent)) {
          final com.revolsys.jts.geom.GeometryFactory geometryFactory = viewport.getGeometryFactory();
          return geometryFactory.createGeometry(geometry);
        }
      }
    }
    return EMPTY_GEOMETRY;
  }

  public static CoordinatesWithOrientation getMarkerLocation(
    final Viewport2D viewport, final Geometry geometry, final MarkerStyle style) {
    final com.revolsys.jts.geom.GeometryFactory viewportGeometryFactory = viewport.getGeometryFactory();
    if (viewportGeometryFactory != null) {
      final com.revolsys.jts.geom.GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);

      Coordinates point = null;
      double orientation = 0;
      final String placement = style.getMarkerPlacement();
      final Matcher matcher = Pattern.compile("point\\((.*)\\)").matcher(
        placement);
      // TODO optimize projection?
      CoordinatesList points = CoordinatesListUtil.get(geometry);
      final int numPoints = points.size();
      if (numPoints > 1) {
        final boolean matches = matcher.matches();
        if (matches) {
          final String argument = matcher.group(1);
          int index;
          if (argument.matches("n(?:\\s*-\\s*(\\d+)\\s*)?")) {
            final String indexString = argument.replaceAll("[^0-9]+", "");
            index = numPoints - 1;
            if (indexString.length() > 0) {
              index -= Integer.parseInt(indexString);
            }
            if (index == 0) {
              index++;
            }
            point = ProjectionFactory.convert(points.get(index),
              geometryFactory, viewportGeometryFactory);
            final Coordinates p2 = ProjectionFactory.convert(
              points.get(index - 1), geometryFactory, viewportGeometryFactory);
            orientation = Math.toDegrees(p2.angle2d(point));

          } else {
            index = Integer.parseInt(argument);
            if (index + 1 == numPoints) {
              index--;
            }
            point = ProjectionFactory.convert(points.get(index),
              geometryFactory, viewportGeometryFactory);
            final Coordinates p2 = ProjectionFactory.convert(
              points.get(index + 1), geometryFactory, viewportGeometryFactory);
            orientation = Math.toDegrees(-point.angle2d(p2));
          }
        } else if ("center".equals(placement)) {
          if (geometry instanceof LineString && geometry.getVertexCount() > 1) {
            final Geometry projectedGeometry = viewportGeometryFactory.copy(geometry);
            points = CoordinatesListUtil.get(projectedGeometry);
            final double totalLength = projectedGeometry.getLength();
            final double centreLength = totalLength / 2;
            double currentLength = 0;
            for (int i = 1; i < numPoints && currentLength < centreLength; i++) {
              final Coordinates p1 = points.get(i - 1);
              final Coordinates p2 = points.get(i);
              final double segmentLength = p1.distance(p2);
              if (segmentLength + currentLength >= centreLength) {
                point = LineSegmentUtil.project(2, p1, p2,
                  (centreLength - currentLength) / segmentLength);
                // TODO parameter to use orientation or not
                orientation = Math.toDegrees(-p1.angle2d(p2));
              }
              currentLength += segmentLength;
            }
          }
        } else {
          point = ProjectionFactory.convert(CoordinatesUtil.get(geometry),
            geometryFactory, viewportGeometryFactory);
        }

        if (point != null && viewport.getBoundingBox().contains(point)) {
          return new CoordinatesWithOrientation(point, orientation);
        }
      }
    }
    return null;
  }

  /**
   * Coordinates must be in the same geometry factory as the view.
   * 
   * @param viewport
   * @param graphics
   * @param point
   * @param style
   */
  public static void renderMarker(final Viewport2D viewport,
    final Graphics2D graphics, final Coordinates point,
    final MarkerStyle style, final double orientation) {
    if (viewport.getBoundingBox().contains(point)) {
      final boolean savedUseModelUnits = viewport.setUseModelCoordinates(false,
        graphics);
      final Paint paint = graphics.getPaint();
      try {
        final Marker marker = style.getMarker();
        final double x = point.getX();
        final double y = point.getY();
        marker.render(viewport, graphics, style, x, y, orientation);
      } finally {
        graphics.setPaint(paint);
        viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
      }
    }
  }

  public static void renderMarker(final Viewport2D viewport,
    final Graphics2D graphics, final Geometry geometry, final MarkerStyle style) {
    if ("line".equals(style.getMarkerPlacement())) {
      renderMarkerVertices(viewport, graphics, geometry, style);
    } else {
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        final Geometry part = geometry.getGeometry(i);
        if (part instanceof Point) {
          final Point point = (Point)part;
          renderMarker(viewport, graphics, point, style);
        } else if (part instanceof LineString) {
          final LineString line = (LineString)part;
          renderMarker(viewport, graphics, line, style);
        } else if (part instanceof Polygon) {
          final Polygon polygon = (Polygon)part;
          renderMarker(viewport, graphics, polygon, style);
        }
      }
    }
  }

  private static void renderMarker(final Viewport2D viewport,
    final Graphics2D graphics, final LineString line, final MarkerStyle style) {
    final CoordinatesWithOrientation point = getMarkerLocation(viewport, line,
      style);
    if (point != null) {
      final double orientation = point.getOrientation();
      renderMarker(viewport, graphics, point, style, orientation);
    }
  }

  public static final void renderMarker(final Viewport2D viewport,
    final Graphics2D graphics, final Point point, final MarkerStyle style) {
    final Geometry geometry = getGeometry(viewport, point);
    if (!geometry.isEmpty()) {
      final Coordinates coordinates = CoordinatesUtil.get(geometry);
      renderMarker(viewport, graphics, coordinates, style, 0);
    }
  }

  private static void renderMarker(final Viewport2D viewport,
    final Graphics2D graphics, final Polygon polygon, final MarkerStyle style) {
    // TODO Auto-generated method stub

  }

  /**
   * Coordinates must be in the same geometry factory as the view.
   * 
   * @param viewport
   * @param graphics
   * @param style
   * @param point
   */
  public static void renderMarkers(final Viewport2D viewport,
    final Graphics2D graphics, final CoordinatesList points,
    final MarkerStyle style) {
    final boolean savedUseModelUnits = viewport.setUseModelCoordinates(false,
      graphics);
    final Paint paint = graphics.getPaint();
    try {
      final Marker marker = style.getMarker();
      final String orientationType = style.getMarkerOrientationType();
      final boolean hasOrientationType = !"none".equals(orientationType);
      final boolean isNext = "next".equals(orientationType);
      final int pointCount = points.size();
      for (int i = 0; i < pointCount; i++) {
        final double x = points.getX(i);
        final double y = points.getY(i);
        double orientation = 0;
        if (hasOrientationType && pointCount > 1) {
          if (i == 0 || isNext) {
            final double x1 = points.getX(i + 1);
            final double y1 = points.getY(i + 1);
            orientation = MathUtil.angleDegrees(x, y, x1, y1);
          } else {
            final double x1 = points.getX(i - 1);
            final double y1 = points.getY(i - 1);
            orientation = MathUtil.angleDegrees(x1, y1, x, y);
          }
        }
        marker.render(viewport, graphics, style, x, y, orientation);
      }
    } finally {
      graphics.setPaint(paint);
      viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
    }
  }

  public static void renderMarkers(final Viewport2D viewport,
    final Graphics2D graphics, final CoordinatesList points,
    final MarkerStyle styleFirst, final MarkerStyle styleLast,
    final MarkerStyle styleVertex) {
    if (points != null) {
      final boolean savedUseModelUnits = viewport.setUseModelCoordinates(false,
        graphics);
      final Paint paint = graphics.getPaint();
      try {
        final int pointCount = points.size();
        if (pointCount > 1) {
          for (int i = 0; i < pointCount; i++) {
            MarkerStyle style;
            if (i == 0) {
              style = styleFirst;
            } else if (i == pointCount - 1) {
              style = styleLast;
            } else {
              style = styleVertex;
            }
            if (style != null) {
              final double x = points.getX(i);
              final double y = points.getY(i);
              double orientation = 0;
              if (i == 0) {
                final double x1 = points.getX(i + 1);
                final double y1 = points.getY(i + 1);
                orientation = MathUtil.angleDegrees(x, y, x1, y1);
              } else {
                final double x1 = points.getX(i - 1);
                final double y1 = points.getY(i - 1);
                orientation = MathUtil.angleDegrees(x1, y1, x, y);
              }
              final Marker marker = style.getMarker();
              marker.render(viewport, graphics, style, x, y, orientation);
            }
          }
        }
      } finally {
        graphics.setPaint(paint);
        viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
      }
    }
  }

  public static final void renderMarkerVertices(final Viewport2D viewport,
    final Graphics2D graphics, Geometry geometry, final MarkerStyle style) {
    geometry = getGeometry(viewport, geometry);
    if (!geometry.isEmpty()) {
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        final Geometry part = geometry.getGeometry(i);
        if (part instanceof Point) {
          final Point point = (Point)part;
          renderMarker(viewport, graphics, point, style);
        } else if (part instanceof LineString) {
          final LineString lineString = (LineString)part;
          final CoordinatesList points = CoordinatesListUtil.get(lineString);
          renderMarkers(viewport, graphics, points, style);
        } else if (part instanceof Polygon) {
          final Polygon polygon = (Polygon)part;
          final List<CoordinatesList> pointsList = CoordinatesListUtil.getAll(polygon);
          for (final CoordinatesList points : pointsList) {
            renderMarkers(viewport, graphics, points, style);
          }
        }
      }
    }
  }

  public static final void renderMarkerVerticesNoPoint(
    final Viewport2D viewport, final Graphics2D graphics, Geometry geometry,
    final MarkerStyle style) {
    geometry = getGeometry(viewport, geometry);
    if (!geometry.isEmpty()) {
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        final Geometry part = geometry.getGeometry(i);
        if (part instanceof LineString) {
          final LineString lineString = (LineString)part;
          final CoordinatesList points = CoordinatesListUtil.get(lineString);
          renderMarkers(viewport, graphics, points, style);
        } else if (part instanceof Polygon) {
          final Polygon polygon = (Polygon)part;
          final List<CoordinatesList> pointsList = CoordinatesListUtil.getAll(polygon);
          for (final CoordinatesList points : pointsList) {
            renderMarkers(viewport, graphics, points, style);
          }
        }
      }
    }
  }

  private MarkerStyle style;

  public MarkerStyleRenderer(final AbstractDataObjectLayer layer,
    final LayerRenderer<?> parent) {
    this(layer, parent, new MarkerStyle());
  }

  public MarkerStyleRenderer(final AbstractDataObjectLayer layer,
    final LayerRenderer<?> parent, final Map<String, Object> geometryStyle) {
    super("markerStyle", "Marker Style", layer, parent, geometryStyle);
    this.style = new MarkerStyle(geometryStyle);
    setIcon(ICON);
  }

  public MarkerStyleRenderer(final AbstractDataObjectLayer layer,
    final LayerRenderer<?> parent, final MarkerStyle style) {
    super("markerStyle", "Marker Style", layer, parent);
    this.style = style;
    setIcon(ICON);
  }

  public MarkerStyleRenderer(final AbstractDataObjectLayer layer,
    final MarkerStyle style) {
    this(layer, null, style);
  }

  @Override
  public MarkerStyleRenderer clone() {
    final MarkerStyleRenderer clone = (MarkerStyleRenderer)super.clone();
    clone.style = style.clone();
    return clone;
  }

  @Override
  public ValueField createStylePanel() {
    return new MarkerStylePanel(this);
  }

  @Override
  public Icon getIcon() {
    final Marker marker = style.getMarker();
    final Icon icon = marker.getIcon(style);
    if (icon == null) {
      return super.getIcon();
    } else {
      return icon;
    }
  }

  public MarkerStyle getStyle() {
    return this.style;
  }

  @Override
  public void renderRecord(final Viewport2D viewport,
    final Graphics2D graphics, final BoundingBox visibleArea,
    final AbstractDataObjectLayer layer, final LayerDataObject object) {
    if (isVisible(object)) {
      final Geometry geometry = object.getGeometryValue();
      renderMarker(viewport, graphics, geometry, this.style);
    }
  }

  public void setStyle(final MarkerStyle style) {
    this.style = style;
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    if (this.style != null) {
      final Map<String, Object> styleMap = this.style.toMap();
      map.putAll(styleMap);
    }
    return map;
  }
}
