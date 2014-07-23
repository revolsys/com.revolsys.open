package com.revolsys.swing.map.layer.record.renderer;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.PointWithOrientation;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.math.Angle;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.marker.Marker;
import com.revolsys.swing.map.layer.record.style.panel.MarkerStylePanel;

public class MarkerStyleRenderer extends AbstractRecordLayerRenderer {

  private static final Geometry EMPTY_GEOMETRY = GeometryFactory.floating3()
    .geometry();

  private static final Icon ICON = SilkIconLoader.getIcon("style_marker");

  public static Geometry getGeometry(final Viewport2D viewport,
    final Geometry geometry) {
    final BoundingBox viewExtent = viewport.getBoundingBox();
    if (geometry != null) {
      if (!viewExtent.isEmpty()) {
        final BoundingBox geometryExtent = geometry.getBoundingBox();
        if (geometryExtent.intersects(viewExtent)) {
          final GeometryFactory geometryFactory = viewport.getGeometryFactory();
          return geometryFactory.geometry(geometry);
        }
      }
    }
    return EMPTY_GEOMETRY;
  }

  public static PointWithOrientation getMarkerLocation(
    final Viewport2D viewport, final Geometry geometry, final MarkerStyle style) {
    final GeometryFactory viewportGeometryFactory = viewport.getGeometryFactory();
    if (viewportGeometryFactory != null) {
      final GeometryFactory geometryFactory = geometry.getGeometryFactory();

      Point point = null;
      double orientation = 0;
      final String placement = style.getMarkerPlacement();
      final Matcher matcher = Pattern.compile("point\\((.*)\\)").matcher(
        placement);
      // TODO optimize projection?
      final int numPoints = geometry.getVertexCount();
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
            point = geometry.getVertex(index).convert(viewportGeometryFactory);
            final Point p2 = geometry.getVertex(index - 1).convert(
              viewportGeometryFactory);
            orientation = Math.toDegrees(p2.angle2d(point));

          } else {
            index = Integer.parseInt(argument);
            if (index + 1 == numPoints) {
              index--;
            }
            point = geometry.getVertex(index).convert(viewportGeometryFactory);
            final Point p2 = geometry.getVertex(index + 1).convert(
              viewportGeometryFactory);
            orientation = Math.toDegrees(-point.angle2d(p2));
          }
        } else if ("center".equals(placement)) {
          if (geometry instanceof LineString && geometry.getVertexCount() > 1) {
            final Geometry projectedGeometry = geometry.convert(viewportGeometryFactory);
            final double totalLength = projectedGeometry.getLength();
            final double centreLength = totalLength / 2;
            double currentLength = 0;
            for (int i = 1; i < numPoints && currentLength < centreLength; i++) {
              final Point p1 = projectedGeometry.getVertex(i - 1);
              final Point p2 = projectedGeometry.getVertex(i);
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
          point = ProjectionFactory.convert(geometry.getPoint(),
            geometryFactory, viewportGeometryFactory);
        }

        if (point != null && viewport.getBoundingBox().covers(point)) {
          return new PointWithOrientation(point, orientation);
        }
      }
    }
    return null;
  }

  public static void renderMarker(final Viewport2D viewport,
    final Geometry geometry, final MarkerStyle style) {
    final Graphics2D graphics = viewport.getGraphics();
    if (graphics != null) {
      if ("line".equals(style.getMarkerPlacement())) {
        renderMarkerVertices(viewport, graphics, geometry, style);
      } else {
        for (int i = 0; i < geometry.getGeometryCount(); i++) {
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
  }

  private static void renderMarker(final Viewport2D viewport,
    final Graphics2D graphics, final LineString line, final MarkerStyle style) {
    final PointWithOrientation point = getMarkerLocation(viewport, line, style);
    if (point != null) {
      final double orientation = point.getOrientation();
      renderMarker(viewport, graphics, point, style, orientation);
    }
  }

  public static final void renderMarker(final Viewport2D viewport,
    final Graphics2D graphics, final Point point, final MarkerStyle style) {
    final Geometry geometry = getGeometry(viewport, point);
    if (!geometry.isEmpty()) {
      final Point coordinates = geometry.getPoint();
      renderMarker(viewport, graphics, coordinates, style, 0);
    }
  }

  /**
   * Point must be in the same geometry factory as the view.
   * 
   * @param viewport
   * @param graphics
   * @param point
   * @param style
   */
  public static void renderMarker(final Viewport2D viewport,
    final Graphics2D graphics, final Point point, final MarkerStyle style,
    final double orientation) {
    if (viewport.getBoundingBox().covers(point)) {
      final Paint paint = graphics.getPaint();
      try {
        final Marker marker = style.getMarker();
        final double x = point.getX();
        final double y = point.getY();
        marker.render(viewport, graphics, style, x, y, orientation);
      } finally {
        graphics.setPaint(paint);
      }
    }
  }

  private static void renderMarker(final Viewport2D viewport,
    final Graphics2D graphics, final Polygon polygon, final MarkerStyle style) {
    // TODO Auto-generated method stub

  }

  /**
   * Point must be in the same geometry factory as the view.
   * 
   * @param viewport
   * @param graphics
   * @param style
   * @param point
   */
  public static void renderMarkers(final Viewport2D viewport,
    final Graphics2D graphics, final LineString line, final MarkerStyle style) {
    final Paint paint = graphics.getPaint();
    try {
      final Marker marker = style.getMarker();
      final String orientationType = style.getMarkerOrientationType();
      final boolean hasOrientationType = !"none".equals(orientationType);
      final boolean isNext = "next".equals(orientationType);
      final int vertexCount = line.getVertexCount();
      for (int i = 0; i < vertexCount; i++) {
        final double x = line.getX(i);
        final double y = line.getY(i);
        double orientation = 0;
        if (hasOrientationType && vertexCount > 1) {
          if (i == 0 || isNext) {
            final double x1 = line.getX(i + 1);
            final double y1 = line.getY(i + 1);
            orientation = Angle.angleDegrees(x, y, x1, y1);
          } else {
            final double x1 = line.getX(i - 1);
            final double y1 = line.getY(i - 1);
            orientation = Angle.angleDegrees(x1, y1, x, y);
          }
        }
        marker.render(viewport, graphics, style, x, y, orientation);
      }
    } finally {
      graphics.setPaint(paint);
    }
  }

  public static void renderMarkers(final Viewport2D viewport,
    final Graphics2D graphics, final LineString line,
    final MarkerStyle styleFirst, final MarkerStyle styleLast,
    final MarkerStyle styleVertex) {
    if (line != null) {
      final Paint paint = graphics.getPaint();
      try {
        final int vertexCount = line.getVertexCount();
        if (vertexCount > 1) {
          for (int i = 0; i < vertexCount; i++) {
            MarkerStyle style;
            if (i == 0) {
              style = styleFirst;
            } else if (i == vertexCount - 1) {
              style = styleLast;
            } else {
              style = styleVertex;
            }
            if (style != null) {
              final double x = line.getX(i);
              final double y = line.getY(i);
              double orientation = 0;
              if (i == 0) {
                final double x1 = line.getX(i + 1);
                final double y1 = line.getY(i + 1);
                orientation = Angle.angleDegrees(x, y, x1, y1);
              } else {
                final double x1 = line.getX(i - 1);
                final double y1 = line.getY(i - 1);
                orientation = Angle.angleDegrees(x1, y1, x, y);
              }
              final Marker marker = style.getMarker();
              marker.render(viewport, graphics, style, x, y, orientation);
            }
          }
        }
      } finally {
        graphics.setPaint(paint);
      }
    }
  }

  public static final void renderMarkerVertices(final Viewport2D viewport,
    final Graphics2D graphics, Geometry geometry, final MarkerStyle style) {
    geometry = getGeometry(viewport, geometry);
    if (!geometry.isEmpty()) {
      for (final Geometry part : geometry.geometries()) {
        if (part instanceof Point) {
          final Point point = (Point)part;
          renderMarker(viewport, graphics, point, style);
        } else if (part instanceof LineString) {
          final LineString line = (LineString)part;
          renderMarkers(viewport, graphics, line, style);
        } else if (part instanceof Polygon) {
          final Polygon polygon = (Polygon)part;
          for (final LinearRing ring : polygon.rings()) {
            renderMarkers(viewport, graphics, ring, style);
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
      final List<LineString> lines = geometry.getGeometryComponents(LineString.class);
      for (final LineString line : lines) {
        renderMarkers(viewport, graphics, line, style);
      }
    }
  }

  private MarkerStyle style;

  public MarkerStyleRenderer(final AbstractRecordLayer layer,
    final LayerRenderer<?> parent) {
    this(layer, parent, new MarkerStyle());
  }

  public MarkerStyleRenderer(final AbstractRecordLayer layer,
    final LayerRenderer<?> parent, final Map<String, Object> geometryStyle) {
    super("markerStyle", "Marker Style", layer, parent, geometryStyle);
    this.style = new MarkerStyle(geometryStyle);
    setIcon(ICON);
  }

  public MarkerStyleRenderer(final AbstractRecordLayer layer,
    final LayerRenderer<?> parent, final MarkerStyle style) {
    super("markerStyle", "Marker Style", layer, parent);
    this.style = style;
    setIcon(ICON);
  }

  public MarkerStyleRenderer(final AbstractRecordLayer layer,
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
    final BoundingBox visibleArea, final AbstractLayer layer,
    final LayerRecord object) {
    if (isVisible(object)) {
      final Geometry geometry = object.getGeometryValue();
      renderMarker(viewport, geometry, this.style);
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
