package com.revolsys.swing.map.layer.geometry;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.dataobject.symbolizer.SymbolizerUtil;
import com.revolsys.swing.map.style.Style;
import com.revolsys.swing.map.symbolizer.Stroke;
import com.revolsys.swing.map.util.GeometryShapeUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryRendererUtil {

  public static final void renderGeometry(Viewport2D viewport,
    Graphics2D graphics, Geometry geometry, Style style) {
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      Geometry part = geometry.getGeometryN(i);
      if (geometry instanceof Point) {
        Point point = (Point)geometry;
        // TODO
      } else if (part instanceof LineString) {
        LineString lineString = (LineString)part;
        renderLineString(viewport, graphics, lineString, style);
      } else if (part instanceof Polygon) {
        Polygon polygon = (Polygon)part;
        renderPolygon(viewport, graphics, polygon, style);
      }
    }

  }

  public static final void renderOutline(Viewport2D viewport,
    Graphics2D graphics, Geometry geometry, Style style) {
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      Geometry part = geometry.getGeometryN(i);
      if (geometry instanceof Point) {
        Point point = (Point)geometry;
        // TODO
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

  public static final void renderPolygon(Viewport2D viewport,
    Graphics2D graphics, Polygon polygon, Style style) {
    GeometryFactory geometryFactory = viewport.getGeometryFactory();
    if (geometryFactory != null) {
      final boolean savedUseModelUnits = viewport.isUseModelCoordinates();
      viewport.setUseModelCoordinates(true, graphics);
      Paint paint = graphics.getPaint();
      try {
        Polygon convertedPolygon = (Polygon)geometryFactory.createGeometry(polygon);
        // TODO clip
        Shape shape = GeometryShapeUtil.toShape(viewport, convertedPolygon);
        style.setLineStyle(viewport, graphics);
        style.setFillStyle(viewport, graphics);
        graphics.fill(shape);
        graphics.draw(shape);
      } finally {
        viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
        graphics.setPaint(paint);
      }
    }
  }

  public static final void renderLineString(Viewport2D viewport,
    Graphics2D graphics, LineString lineString, Style style) {
    GeometryFactory geometryFactory = viewport.getGeometryFactory();
    if (geometryFactory != null) {
      final boolean savedUseModelUnits = viewport.isUseModelCoordinates();
      viewport.setUseModelCoordinates(true, graphics);
      Paint paint = graphics.getPaint();
      try {
        LineString convertedLineString = (LineString)geometryFactory.createGeometry(lineString);
        // TODO clip
        Shape shape = GeometryShapeUtil.toShape(viewport, convertedLineString);

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
}
