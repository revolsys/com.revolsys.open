package com.revolsys.jtstest.testbuilder.ui.render;

//import java.awt.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;

import com.revolsys.jts.awt.PointShapeFactory;
import com.revolsys.jts.awt.ShapeWriter;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jtstest.testbuilder.AppConstants;
import com.revolsys.jtstest.testbuilder.Viewport;
import com.revolsys.jtstest.testbuilder.ui.style.Style;

public class GeometryPainter {
  private static Stroke GEOMETRY_STROKE = new BasicStroke();

  private static Stroke POINT_STROKE = new BasicStroke(AppConstants.POINT_SIZE);

  static Viewport viewportCache;

  static ShapeWriter converterCache;

  /**
   * Choose a fairly conservative decimation distance to avoid visual artifacts
   */
  private static final double DECIMATION_DISTANCE = 1.3;

  // TODO: does not work, has a race condition
  public static ShapeWriter BADgetConverter(final Viewport viewport) {
    if (viewportCache != viewport) {
      viewportCache = viewport;
      converterCache = new ShapeWriter(viewport, new PointShapeFactory.Point());
    }
    return converterCache;
  }

  // TODO: is this a performance problem?
  // probably not - only called once for each geom painted
  public static ShapeWriter getConverter(final Viewport viewport) {
    final ShapeWriter sw = new ShapeWriter(viewport,
      new PointShapeFactory.Point());
    // sw.setRemoveDuplicatePoints(true);
    sw.setDecimation(viewport.toModel(DECIMATION_DISTANCE));
    return sw;
  }

  private static void paint(final Geometry geometry,
    final ShapeWriter converter, final Graphics2D g, final Color lineColor,
    final Color fillColor) {
    paint(geometry, converter, g, lineColor, fillColor, null);
  }

  private static void paint(final Geometry geometry,
    final ShapeWriter converter, final Graphics2D g, final Color lineColor,
    final Color fillColor, final Stroke stroke) {
    if (geometry == null) {
      return;
    }

    if (geometry instanceof GeometryCollection) {
      final GeometryCollection gc = (GeometryCollection)geometry;
      /**
       * Render each element separately.
       * Otherwise it is not possible to render both filled and non-filled
       * (1D) elements correctly
       */
      for (int i = 0; i < gc.getGeometryCount(); i++) {
        paint(gc.getGeometry(i), converter, g, lineColor, fillColor, stroke);
      }
      return;
    }

    final Shape shape = converter.toShape(geometry);

    // handle points in a special way for appearance and speed
    if (geometry instanceof Point) {
      g.setStroke(POINT_STROKE);
      g.setColor(lineColor);
      g.draw(shape);
      return;
    }

    if (stroke == null) {
      g.setStroke(GEOMETRY_STROKE);
    } else {
      g.setStroke(stroke);
    }

    // Test for a polygonal shape and fill it if required
    if (geometry instanceof Polygon && fillColor != null) {
      // if (!(shape instanceof GeneralPath) && fillColor != null) {
      g.setPaint(fillColor);
      g.fill(shape);
    }

    if (lineColor != null) {
      g.setColor(lineColor);
      try {
        g.draw(shape);

        // draw polygon boundaries twice, to discriminate them
        // MD - this isn't very obvious. Perhaps a dashed line instead?
        /*
         * if (geometry instanceof Polygon) { Shape polyShell =
         * converter.toShape( ((Polygon)geometry).getExteriorRing());
         * g.setStroke(new BasicStroke(2)); g.draw(polyShell); }
         */
      } catch (final Throwable ex) {
        System.out.println(ex);
        // eat it!
      }
    }
  }

  /**
   * Paints a geometry onto a graphics context,
   * using a given Viewport.
   * 
   * @param geometry shape to paint
   * @param viewport
   * @param g the graphics context
   * @param lineColor line color (null if none)
   * @param fillColor fill color (null if none)
   */
  public static void paint(final Geometry geometry, final Viewport viewport,
    final Graphics2D g, final Color lineColor, final Color fillColor) {
    paint(geometry, viewport, g, lineColor, fillColor, null);
  }

  public static void paint(final Geometry geometry, final Viewport viewport,
    final Graphics2D g, final Color lineColor, final Color fillColor,
    final Stroke stroke) {
    final ShapeWriter converter = getConverter(viewport);
    // ShapeWriter converter = new ShapeWriter(viewport);
    paint(geometry, converter, g, lineColor, fillColor, stroke);
  }

  public static void paint(final Graphics2D g, final Viewport viewport,
    final Geometry geometry, final Style style) throws Exception {
    if (geometry == null) {
      return;
    }

    // cull non-visible geometries
    if (!viewport.intersectsInModel(geometry.getBoundingBox())) {
      return;
    }

    if (geometry instanceof GeometryCollection) {
      final GeometryCollection gc = (GeometryCollection)geometry;
      /**
       * Render each element separately.
       * Otherwise it is not possible to render both filled and non-filled
       * (1D) elements correctly
       */
      for (int i = 0; i < gc.getGeometryCount(); i++) {
        paint(g, viewport, gc.getGeometry(i), style);
      }
      return;
    }

    style.paint(geometry, viewport, g);
  }

  private static void paintGeometryCollection(final Graphics2D g,
    final Viewport viewport, final GeometryCollection gc, final Style style)
    throws Exception {
  }

}
