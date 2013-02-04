package com.revolsys.swing.map.layer.geometry;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.List;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.dataobject.style.Marker;
import com.revolsys.swing.map.layer.dataobject.style.TextStyle;
import com.revolsys.swing.map.util.GeometryShapeUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryRendererUtil {

  private static final Geometry EMPTY_GEOMETRY = GeometryFactory.getFactory()
    .createEmptyGeometry();

  private static Geometry getGeometry(final Viewport2D viewport,
    final GeometryStyle style, final Geometry geometry) {
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

  private static Shape getShape(final Viewport2D viewport,
    final GeometryStyle style, final Geometry geometry) {
    final BoundingBox viewExtent = viewport.getBoundingBox();
    if (geometry != null) {
      if (!viewExtent.isNull()) {
        final BoundingBox geometryExtent = BoundingBox.getBoundingBox(geometry);
        if (geometryExtent.intersects(viewExtent)) {
          final GeometryFactory geometryFactory = viewport.getGeometryFactory();
          final Geometry convertedGeometry = geometryFactory.createGeometry(geometry);
          // TODO clipping
          return GeometryShapeUtil.toShape(viewport, convertedGeometry);
        }
      }
    }
    return null;
  }

  public static final void renderGeometry(final Viewport2D viewport,
    final Graphics2D graphics, final Geometry geometry,
    final GeometryStyle style) {
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      final Geometry part = geometry.getGeometryN(i);
      if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        renderPoint(viewport, graphics, point, style);
      } else if (part instanceof LineString) {
        final LineString lineString = (LineString)part;
        renderLineString(viewport, graphics, lineString, style);
      } else if (part instanceof Polygon) {
        final Polygon polygon = (Polygon)part;
        renderPolygon(viewport, graphics, polygon, style);
      }
    }
  }

  public static final void renderLineString(final Viewport2D viewport,
    final Graphics2D graphics, final LineString lineString,
    final GeometryStyle style) {
    final Shape shape = getShape(viewport, style, lineString);
    if (shape != null) {
      final boolean savedUseModelUnits = viewport.isUseModelCoordinates();
      viewport.setUseModelCoordinates(true, graphics);
      final Paint paint = graphics.getPaint();
      try {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);

        style.setLineStyle(viewport, graphics);
        graphics.draw(shape);
      } finally {
        viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
        graphics.setPaint(paint);
      }
    }
  }

  public static final void renderOutline(final Viewport2D viewport,
    final Graphics2D graphics, final Geometry geometry,
    final GeometryStyle style) {
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      final Geometry part = geometry.getGeometryN(i);
      if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        renderPoint(viewport, graphics, point, style);
      } else if (part instanceof LineString) {
        final LineString lineString = (LineString)part;
        renderLineString(viewport, graphics, lineString, style);
      } else if (part instanceof Polygon) {
        final Polygon polygon = (Polygon)part;
        renderLineString(viewport, graphics, polygon.getExteriorRing(), style);
        for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
          final LineString ring = polygon.getInteriorRingN(j);
          renderLineString(viewport, graphics, ring, style);
        }
      }
    }
  }

  /**
   * Coordinates must be in the same geometry factory as the view.
   * 
   * @param viewport
   * @param graphics
   * @param style
   * @param point
   */
  public static void renderPoint(final Viewport2D viewport,
    final Graphics2D graphics, final GeometryStyle style,
    final Coordinates point) {
    final boolean savedUseModelUnits = viewport.isUseModelCoordinates();
    viewport.setUseModelCoordinates(true, graphics);
    final Paint paint = graphics.getPaint();
    try {
      final Marker marker = style.getMarker();
      final double x = point.getX();
      final double y = point.getY();
      marker.render(viewport, graphics, style, x, y);
    } finally {
      viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
      graphics.setPaint(paint);
    }
  }

  public static final void renderPoint(final Viewport2D viewport,
    final Graphics2D graphics, final Point point, final GeometryStyle style) {
    final Geometry geometry = getGeometry(viewport, style, point);
    if (!geometry.isEmpty()) {
      final Coordinates coordinates = CoordinatesUtil.get(geometry);
      renderPoint(viewport, graphics, style, coordinates);
    }
  }

  /**
   * Coordinates must be in the same geometry factory as the view.
   * 
   * @param viewport
   * @param graphics
   * @param style
   * @param point
   */
  public static void renderPoints(final Viewport2D viewport,
    final Graphics2D graphics, final GeometryStyle style,
    final CoordinatesList points) {
    final boolean savedUseModelUnits = viewport.isUseModelCoordinates();
    viewport.setUseModelCoordinates(true, graphics);
    final Paint paint = graphics.getPaint();
    try {
      final Marker marker = style.getMarker();
      for (int i = 0; i < points.size(); i++) {
        final double x = points.getX(i);
        final double y = points.getY(i);
        marker.render(viewport, graphics, style, x, y);
      }
    } finally {
      viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
      graphics.setPaint(paint);
    }
  }

  public static final void renderPolygon(final Viewport2D viewport,
    final Graphics2D graphics, final Polygon polygon, final GeometryStyle style) {
    final Shape shape = getShape(viewport, style, polygon);
    if (shape != null) {
      final boolean savedUseModelUnits = viewport.isUseModelCoordinates();
      viewport.setUseModelCoordinates(true, graphics);
      final Paint paint = graphics.getPaint();
      try {
        style.setFillStyle(viewport, graphics);
        graphics.fill(shape);
        style.setLineStyle(viewport, graphics);
        graphics.draw(shape);
      } finally {
        viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
        graphics.setPaint(paint);
      }
    }
  }

  public static final void renderText(final Viewport2D viewport,
    final Graphics2D graphics, final DataObject object,
    final Geometry geometry, final TextStyle style) {
    final GeometryFactory geometryFactory = viewport.getGeometryFactory();
    if (geometryFactory != null) {
      final Coordinates point = CoordinatesUtil.get(geometryFactory.project(geometry));
      final boolean savedUseModelUnits = viewport.isUseModelCoordinates();
      final Paint paint = graphics.getPaint();
      viewport.setUseModelCoordinates(false, graphics);
      graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
      graphics.drawString("Hello", 100, 100);
      viewport.setUseModelCoordinates(true, graphics);
      style.setTextStyle(viewport, graphics);
      try {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);

        final String label = "Test";// object.getValue(symbolizer.getLabelPropertyName());
        if (label != null) {
          final double scaleFactor = 1.0 / viewport.getModelUnitsPerViewUnit();
          final double[] location = viewport.toViewCoordinates(point.getX(),
            point.getY());
          final double x = location[0];
          final double y = location[1];
          final AffineTransform savedTransform = graphics.getTransform();
          graphics.translate(x, y);
          graphics.scale(scaleFactor, scaleFactor);

          style.setTextStyle(viewport, graphics);

          double rotation = style.getTextOrientation();

          rotation = (450 - rotation) % 360;
          if (rotation != 0) {
            graphics.rotate(Math.toRadians(-rotation), 0, 0);
          }

          final FontMetrics fontMetrics = graphics.getFontMetrics();
          final double width = fontMetrics.stringWidth(label);
          final double height = fontMetrics.getAscent();
          // graphics.translate(-width * symbolizer.getAnchorX().doubleValue(),
          // height
          // * symbolizer.getAnchorY().doubleValue());
          // graphics.translate(
          // -viewport.toDisplayValue(symbolizer.getDisplacementX()),
          // viewport.toDisplayValue(symbolizer.getDisplacementY()));

          final double textHaloRadius = viewport.toDisplayValue(style.getTextHaloRadius());
          if (textHaloRadius > 0) {
            final Stroke savedStroke = graphics.getStroke();
            final Stroke outlineStroke = new BasicStroke((int)textHaloRadius,
              BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
            graphics.setColor(style.getTextHaloFill());
            graphics.setStroke(outlineStroke);

            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
              RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            final Font font = graphics.getFont();
            final FontRenderContext fontRenderContext = graphics.getFontRenderContext();
            final TextLayout textLayout = new TextLayout(label, font,
              fontRenderContext);

            final AffineTransform outlineTransform = AffineTransform.getTranslateInstance(
              0, 0);
            final Shape outlineShape = textLayout.getOutline(outlineTransform);
            graphics.draw(outlineShape);
            graphics.setStroke(savedStroke);
          }

          graphics.drawString(label, (float)0, (float)0);
          graphics.setTransform(savedTransform);
        }

      } finally {
        viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
        graphics.setPaint(paint);
      }
    }
  }

  public static final void renderVertices(final Viewport2D viewport,
    final Graphics2D graphics, Geometry geometry, final GeometryStyle style) {
    geometry = getGeometry(viewport, style, geometry);
    if (!geometry.isEmpty()) {
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        final Geometry part = geometry.getGeometryN(i);
        if (geometry instanceof Point) {
          final Point point = (Point)geometry;
          renderPoint(viewport, graphics, point, style);
        } else if (part instanceof LineString) {
          final LineString lineString = (LineString)part;
          final CoordinatesList points = CoordinatesListUtil.get(lineString);
          renderPoints(viewport, graphics, style, points);
        } else if (part instanceof Polygon) {
          final Polygon polygon = (Polygon)part;
          final List<CoordinatesList> pointsList = CoordinatesListUtil.getAll(polygon);
          for (final CoordinatesList points : pointsList) {
            renderPoints(viewport, graphics, style, points);
          }
        }
      }
    }
  }
}
