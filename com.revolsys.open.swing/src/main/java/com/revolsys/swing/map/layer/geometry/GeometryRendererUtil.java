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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.Measure;
import javax.measure.quantity.Length;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
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
      Geometry projectedGeometry = geometryFactory.project(geometry);
      final Coordinates point = getTextPoint(projectedGeometry, style);
      final boolean savedUseModelUnits = viewport.isUseModelCoordinates();
      final Paint paint = graphics.getPaint();
      viewport.setUseModelCoordinates(true, graphics);
      try {
        style.setTextStyle(viewport, graphics);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);

        final String label = getLabel(object, style);
        if (StringUtils.hasText(label)) {
          final double x = point.getX();
          final double y = point.getY();

          graphics.translate(x, y);
          final AffineTransform savedTransform = graphics.getTransform();

          style.setTextStyle(viewport, graphics);

          double rotation = getTextOrientation(geometry, style);

          if (rotation != 0) {
            graphics.rotate(Math.toRadians(-rotation), 0, 0);
          }

          // TODO deltaX, deltaY along rotation axis
          final FontMetrics fontMetrics = graphics.getFontMetrics();
          final double width = fontMetrics.stringWidth(label);
          final double height = fontMetrics.getAscent();

          String verticalAlignment = style.getTextVerticalAlignment();
          if ("top".equals(verticalAlignment)) {
            graphics.translate(0, -height);
          } else if ("middle".equals(verticalAlignment)) {
            graphics.translate(0, -height / 2);
          }
          String horizontalAlignment = style.getTextAlign();
          if ("right".equals(horizontalAlignment)) {
            graphics.translate(-width, 0);
          } else if ("center".equals(horizontalAlignment)) {
            graphics.translate(-width / 2, 0);
          }

          Measure<Length> textDx = style.getTextDeltaX();
          Measure<Length> textDy = style.getTextDeltaY();
          graphics.translate(viewport.toDisplayValue(textDx),
            viewport.toDisplayValue(textDy));

          graphics.scale(1, -1);
          final double textHaloRadius = viewport.toDisplayValue(style.getTextHaloRadiusMeasure());
          if (textHaloRadius > 0) {
            final Stroke savedStroke = graphics.getStroke();
            final Stroke outlineStroke = new BasicStroke((float)textHaloRadius,
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

          graphics.setColor(style.getTextFill());
          graphics.drawString(label, (float)0, (float)0);
          graphics.setTransform(savedTransform);
        }

      } finally {
        viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
        graphics.setPaint(paint);
      }
    }
  }

  private static String getLabel(final DataObject object, TextStyle style) {
    StringBuffer label = new StringBuffer();
    String labelPattern = style.getTextName();
    Matcher matcher = Pattern.compile("\\[([\\w.]+)\\]").matcher(labelPattern);
    while (matcher.find()) {
      String propertyName = matcher.group(1);
      Object value = object.getValueByPath(propertyName);
      String text;
      if (value == null) {
        text = "";
      } else {
        text = StringConverterRegistry.toString(value);
      }
      matcher.appendReplacement(label, text);
    }
    matcher.appendTail(label);

    return label.toString().trim();
  }

  private static Coordinates getTextPoint(Geometry geometry, TextStyle style) {
    String textPlacementType = style.getTextPlacementType();
    Matcher matcher = Pattern.compile("point\\((.*)\\)").matcher(
      textPlacementType);
    boolean matches = matcher.matches();
    if (matches) {
      String argument = matcher.group(1);
      CoordinatesList points = CoordinatesListUtil.get(geometry);
      int index;
      if (argument.matches("n(?:\\s*-\\s*(\\d+)\\s*)?")) {
        String indexString = argument.replaceAll("[^0-9]+", "");
        index = points.size() - 1;
        if (indexString.length() > 0) {
          index -= Integer.parseInt(indexString);
        }
      } else {
        index = Integer.parseInt(argument);
      }
      if (index < 0) {
        return points.get(0);
      } else if (index >= points.size()) {
        return points.get(points.size() - 1);
      } else {
        return points.get(index);
      }

    } else if ("center".equals(textPlacementType)) {
      if (geometry instanceof Point) {
        return CoordinatesUtil.get(geometry);
      } else if (geometry instanceof LineString && geometry.getNumPoints() > 1) {
        double totalLength = geometry.getLength();
        double centreLength = totalLength / 2;
        double currentLength = 0;
        CoordinatesList points = CoordinatesListUtil.get(geometry);
        for (int i = 1; i < points.size(); i++) {
          Coordinates p1 = points.get(i - 1);
          Coordinates p2 = points.get(i);
          double segmentLength = p1.distance(p2);
          if (segmentLength + currentLength >= centreLength) {
            double ratio = (centreLength - currentLength) / segmentLength;
            return LineSegmentUtil.project(p1, p2, ratio);
          } else {
            currentLength += segmentLength;
          }
        }
        return CoordinatesUtil.get(geometry);
      } else {
        return CoordinatesUtil.get(geometry.getCentroid());
      }
    } else {
      return CoordinatesUtil.get(geometry);
    }
  }

  private static double getTextOrientation(Geometry geometry, TextStyle style) {
    double orientation = style.getTextOrientation();
    String textPlacementType = style.getTextPlacementType();
    Matcher matcher = Pattern.compile("point\\((.*)\\)").matcher(
      textPlacementType);
    CoordinatesList points = CoordinatesListUtil.get(geometry);
    int numPoints = points.size();
    if (numPoints > 1) {
      boolean matches = matcher.matches();
      if (matches) {
        String argument = matcher.group(1);
        int index;
        if (argument.matches("n(?:\\s*-\\s*(\\d+)\\s*)?")) {
          String indexString = argument.replaceAll("[^0-9]+", "");
          index = numPoints - 1;
          if (indexString.length() > 0) {
            index -= Integer.parseInt(indexString);
          }
          if (index == 0) {
            index++;
          }
          Coordinates p1 = points.get(index);
          Coordinates p2 = points.get(index - 1);
          double angle = Math.toDegrees(p2.angle2d(p1));
          orientation -= angle;
        } else {
          index = Integer.parseInt(argument);
          if (index + 1 == numPoints) {
            index--;
          }
          Coordinates p1 = points.get(index);
          Coordinates p2 = points.get(index + 1);
          double angle = Math.toDegrees(p1.angle2d(p2));
          orientation -= angle;
        }

      } else if ("center".equals(textPlacementType)) {
        if (geometry instanceof LineString && geometry.getNumPoints() > 1) {
          double totalLength = geometry.getLength();
          double centreLength = totalLength / 2;
          double currentLength = 0;
          for (int i = 1; i < numPoints && currentLength < centreLength; i++) {
            Coordinates p1 = points.get(i - 1);
            Coordinates p2 = points.get(i);
            double segmentLength = p1.distance(p2);
            if (segmentLength + currentLength >= centreLength) {
              double angle = Math.toDegrees(p1.angle2d(p2));
              orientation -= angle;
            }
            currentLength += segmentLength;
          }
        }
      }
    }
    return (360 + orientation) % 360;
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
