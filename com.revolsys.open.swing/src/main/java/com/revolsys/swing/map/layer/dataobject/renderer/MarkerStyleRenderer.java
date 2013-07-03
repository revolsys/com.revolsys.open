package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.CoordinatesWithOrientation;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.dataobject.style.MarkerStyle;
import com.revolsys.swing.map.layer.dataobject.style.marker.Marker;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class MarkerStyleRenderer extends AbstractDataObjectLayerRenderer {

  private MarkerStyle style;

  private static final Geometry EMPTY_GEOMETRY = GeometryFactory.getFactory()
    .createEmptyGeometry();

  public static Geometry getGeometry(final Viewport2D viewport,
    final Geometry geometry) {
    final BoundingBox viewExtent = viewport.getBoundingBox();
    if (geometry != null) {
      if (!viewExtent.isNull()) {
        final BoundingBox geometryExtent = BoundingBox.getBoundingBox(geometry);
        if (geometryExtent.intersects(viewExtent)) {
          final GeometryFactory geometryFactory = viewport.getGeometryFactory();
          return geometryFactory.createGeometry(geometry);
        }
      }
    }
    return EMPTY_GEOMETRY;
  }

  public static CoordinatesWithOrientation getMarkerLocation(
    final Viewport2D viewport, final Geometry geometry, final MarkerStyle style) {
    final GeometryFactory viewportGeometryFactory = viewport.getGeometryFactory();
    if (viewportGeometryFactory != null) {
      final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);

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
          if (geometry instanceof LineString && geometry.getNumPoints() > 1) {
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
                point = LineSegmentUtil.project(p1, p2,
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
      final boolean savedUseModelUnits = viewport.isUseModelCoordinates();
      viewport.setUseModelCoordinates(true, graphics);
      final Paint paint = graphics.getPaint();
      try {
        final Marker marker = style.getMarker();
        final double x = point.getX();
        final double y = point.getY();
        marker.render(viewport, graphics, style, x, y, orientation);
      } finally {
        viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
        graphics.setPaint(paint);
      }
    }
  }

  public static void renderMarker(final Viewport2D viewport,
    final Graphics2D graphics, final Geometry geometry, final MarkerStyle style) {
    if ("line".equals(style.getMarkerPlacement())) {
      renderMarkerVertices(viewport, graphics, geometry, style);
    } else {
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        final Geometry part = geometry.getGeometryN(i);
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
    final boolean savedUseModelUnits = viewport.isUseModelCoordinates();
    viewport.setUseModelCoordinates(true, graphics);
    final Paint paint = graphics.getPaint();
    try {
      final Marker marker = style.getMarker();
      for (int i = 0; i < points.size(); i++) {
        final double x = points.getX(i);
        final double y = points.getY(i);
        marker.render(viewport, graphics, style, x, y, 0);
      }
    } finally {
      viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
      graphics.setPaint(paint);
    }
  }

  public static final void renderMarkerVertices(final Viewport2D viewport,
    final Graphics2D graphics, Geometry geometry, final MarkerStyle style) {
    geometry = getGeometry(viewport, geometry);
    if (!geometry.isEmpty()) {
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        final Geometry part = geometry.getGeometryN(i);
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

  public MarkerStyleRenderer(final DataObjectLayer layer) {
    this(layer, new GeometryStyle());
  }

  public MarkerStyleRenderer(final DataObjectLayer layer,
    final LayerRenderer<?> parent, final Map<String, Object> geometryStyle) {
    super("geometryStyle", layer, parent, geometryStyle);
    final Map<String, Object> style = getAllDefaults();
    style.putAll(geometryStyle);
    this.style = new GeometryStyle(style);
  }

  public MarkerStyleRenderer(final DataObjectLayer layer,
    final LayerRenderer<?> parent, final MarkerStyle style) {
    super("geometryStyle", layer, parent);
    this.style = style;
  }

  public MarkerStyleRenderer(final DataObjectLayer layer,
    final MarkerStyle style) {
    this(layer, null, style);
  }

  public MarkerStyle getStyle() {
    return style;
  }

  @Override
  protected void renderObject(final Viewport2D viewport,
    final Graphics2D graphics, final BoundingBox visibleArea,
    final DataObjectLayer layer, final LayerDataObject object) {
    if (isVisible(object)) {
      final Geometry geometry = object.getGeometryValue();
      renderMarker(viewport, graphics, geometry, style);
    }
  }

  public void setStyle(final GeometryStyle style) {
    this.style = style;
  }

}
