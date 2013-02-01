package com.revolsys.swing.map.layer.geometry;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.util.List;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.dataobject.style.Marker;
import com.revolsys.swing.map.layer.dataobject.style.Style;
import com.revolsys.swing.map.util.GeometryShapeUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryRendererUtil {

  private static final Geometry EMPTY_GEOMETRY = GeometryFactory.getFactory()
    .createEmptyGeometry();

  public static final void renderGeometry(Viewport2D viewport,
    Graphics2D graphics, Geometry geometry, Style style) {
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      Geometry part = geometry.getGeometryN(i);
      if (geometry instanceof Point) {
        Point point = (Point)geometry;
        renderPoint(viewport, graphics, point, style);
      } else if (part instanceof LineString) {
        LineString lineString = (LineString)part;
        renderLineString(viewport, graphics, lineString, style);
      } else if (part instanceof Polygon) {
        Polygon polygon = (Polygon)part;
        renderPolygon(viewport, graphics, polygon, style);
      }
    }
  }

  private static Geometry getGeometry(Viewport2D viewport, Style style,
    Geometry geometry) {
    BoundingBox viewExtent = viewport.getBoundingBox();
    if (geometry != null) {
      if (!viewExtent.isNull()) {
        BoundingBox geometryExtent = BoundingBox.getBoundingBox(geometry);
        if (geometryExtent.intersects(viewExtent)) {
          GeometryFactory geometryFactory = viewport.getGeometryFactory();
          return geometryFactory.createGeometry(geometry);
        }
      }
    }
    return EMPTY_GEOMETRY;
  }

  private static Shape getShape(Viewport2D viewport, Style style,
    Geometry geometry) {
    BoundingBox viewExtent = viewport.getBoundingBox();
    if (geometry != null) {
      if (!viewExtent.isNull()) {
        BoundingBox geometryExtent = BoundingBox.getBoundingBox(geometry);
        if (geometryExtent.intersects(viewExtent)) {
          GeometryFactory geometryFactory = viewport.getGeometryFactory();
          Geometry convertedGeometry = geometryFactory.createGeometry(geometry);
          // TODO clipping
          return GeometryShapeUtil.toShape(viewport, convertedGeometry);
        }
      }
    }
    return null;
  }
  public static final void renderOutline(Viewport2D viewport,
    Graphics2D graphics, Geometry geometry, Style style) {
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      Geometry part = geometry.getGeometryN(i);
      if (geometry instanceof Point) {
        Point point = (Point)geometry;
        renderPoint(viewport, graphics, point, style);
      } else if (part instanceof LineString) {
        LineString lineString = (LineString)part;
        renderLineString(viewport, graphics, lineString, style);
      } else if (part instanceof Polygon) {
        Polygon polygon = (Polygon)part;
        renderLineString(viewport, graphics, polygon.getExteriorRing(), style);
        for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
          LineString ring = polygon.getInteriorRingN(j);
          renderLineString(viewport, graphics, ring, style);
        }
      }
    }
  }

  public static final void renderVertices(Viewport2D viewport,
    Graphics2D graphics, Geometry geometry, Style style) {
    geometry = getGeometry(viewport, style, geometry);
    if (!geometry.isEmpty()) {
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        Geometry part = geometry.getGeometryN(i);
        if (geometry instanceof Point) {
          Point point = (Point)geometry;
          renderPoint(viewport, graphics, point, style);
        } else if (part instanceof LineString) {
          LineString lineString = (LineString)part;
          CoordinatesList points = CoordinatesListUtil.get(lineString);
          renderPoints(viewport, graphics, style, points);
        } else if (part instanceof Polygon) {
          Polygon polygon = (Polygon)part;
          List<CoordinatesList> pointsList = CoordinatesListUtil.getAll(polygon);
          for (CoordinatesList points : pointsList) {
            renderPoints(viewport, graphics, style, points);
          }
        }
      }
    }
  }

  public static final void renderPolygon(Viewport2D viewport,
    Graphics2D graphics, Polygon polygon, Style style) {
   Shape shape = getShape(viewport, style, polygon);
   if (shape != null) {
     final boolean savedUseModelUnits = viewport.isUseModelCoordinates();
      viewport.setUseModelCoordinates(true, graphics);
      Paint paint = graphics.getPaint();
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

  public static final void renderLineString(Viewport2D viewport,
    Graphics2D graphics, LineString lineString, Style style) {
    Shape shape = getShape(viewport, style, lineString);
    if (shape != null) {
      final boolean savedUseModelUnits = viewport.isUseModelCoordinates();
      viewport.setUseModelCoordinates(true, graphics);
      Paint paint = graphics.getPaint();
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

  public static final void renderPoint(Viewport2D viewport,
    Graphics2D graphics, Point point, Style style) {
    Geometry geometry = getGeometry(viewport, style, point);
    if (!geometry.isEmpty()) {
      Coordinates coordinates = CoordinatesUtil.get((Point)geometry);
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
  public static void renderPoint(Viewport2D viewport, Graphics2D graphics,
    Style style, Coordinates point) {
    final boolean savedUseModelUnits = viewport.isUseModelCoordinates();
    viewport.setUseModelCoordinates(true, graphics);
    Paint paint = graphics.getPaint();
    try {
      Marker marker = style.getMarker();
      double x = point.getX();
      double y = point.getY();
      marker.render(viewport, graphics, style, x, y);
    } finally {
      viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
      graphics.setPaint(paint);
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
  public static void renderPoints(Viewport2D viewport, Graphics2D graphics,
    Style style, CoordinatesList points) {
    final boolean savedUseModelUnits = viewport.isUseModelCoordinates();
    viewport.setUseModelCoordinates(true, graphics);
    Paint paint = graphics.getPaint();
    try {
      Marker marker = style.getMarker();
      for (int i = 0; i < points.size(); i++) {
        double x = points.getX(i);
        double y = points.getY(i);
        marker.render(viewport, graphics, style, x, y);
      }
    } finally {
      viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
      graphics.setPaint(paint);
    }
  }
}
