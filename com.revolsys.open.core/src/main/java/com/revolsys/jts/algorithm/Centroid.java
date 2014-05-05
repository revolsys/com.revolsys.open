/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.jts.algorithm;

import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.segment.Segment;

/**
 * Computes the centroid of a {@link Geometry} of any dimension.
 * If the geometry is nominally of higher dimension, 
 * but has lower <i>effective</i> dimension 
 * (i.e. contains only components
 * having zero length or area), 
 * the centroid will be computed as for the equivalent lower-dimension geometry.
 * If the input geometry is empty, a
 * <code>null</code> Coordinates is returned.
 * 
 * <h2>Algorithm</h2>
 * <ul>
 * <li><b>Dimension 2</b> - the centroid is computed 
 * as the weighted sum of the centroids
 * of a decomposition of the area into (possibly overlapping) triangles.
 * Holes and multipolygons are handled correctly.
 * See <code>http://www.faqs.org/faqs/graphics/algorithms-faq/</code>
 * for further details of the basic approach.
 * 
 * <li><b>Dimension 1</b> - Computes the average of the midpoints
 * of all line segments weighted by the segment length.
 * Zero-length lines are treated as points.
 * 
 * <li><b>Dimension 0</b> - Compute the average coordinate for all points.
 * Repeated points are all included in the average.
 * </ul>
 * 
 * @version 1.7
 */
public class Centroid {
  /**
   * Returns twice the signed area of the triangle p1-p2-p3.
   * The area is positive if the triangle is oriented CCW, and negative if CW.
   */
  private static double area2(final Coordinates p1, final Coordinates p2,
    final Coordinates p3) {
    return (p2.getX() - p1.getX()) * (p3.getY() - p1.getY())
      - (p3.getX() - p1.getX()) * (p2.getY() - p1.getY());
  }

  /**
   * Computes the centroid point of a geometry.
   * 
   * @param geom the geometry to use
   * @return the centroid point, or null if the geometry is empty
   */
  public static Coordinates getCentroid(final Geometry geom) {
    final Centroid cent = new Centroid(geom);
    return cent.getCentroid();
  }

  private Coordinates areaBasePt = null;// the point all triangles are based at

  private double areasum2 = 0; /* Partial area sum */

  private double cg3X;

  private double cg3Y;

  // data for linear centroid computation, if needed
  private double lineCenterX = 0;

  private double lineCenterY = 0;

  private double totalLength = 0.0;

  private int ptCount = 0;

  private double centSumX = 0;

  private double centSumY = 0;

  /**
   * Creates a new instance for computing the centroid of a geometry
   */
  public Centroid(final Geometry geom) {
    areaBasePt = null;
    add(geom);
  }

  /**
   * Adds a Geometry to the centroid total.
   *
   * @param geom the geometry to add
   */
  private void add(final Geometry geom) {
    if (geom.isEmpty()) {
      return;
    }
    if (geom instanceof Point) {
      addPoint(geom.getCoordinate());
    } else if (geom instanceof LineString) {
      addLineSegments((LineString)geom);
    } else if (geom instanceof Polygon) {
      final Polygon poly = (Polygon)geom;
      add(poly);
    } else if (geom instanceof GeometryCollection) {
      final GeometryCollection gc = (GeometryCollection)geom;
      for (int i = 0; i < gc.getGeometryCount(); i++) {
        add(gc.getGeometry(i));
      }
    }
  }

  private void add(final Polygon poly) {
    addShell(poly.getExteriorRing());
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      addHole(poly.getInteriorRing(i));
    }
  }

  private void addHole(final LineString line) {
    final boolean isPositiveArea = line.isCounterClockwise();
    for (final Segment segment : line.segments()) {
      final Coordinates point1 = segment.get(0);
      final Coordinates point2 = segment.get(1);
      addTriangle(areaBasePt, point1, point2, isPositiveArea);
    }
    addLineSegments(line);
  }

  /**
   * Adds the line segments defined by an array of coordinates
   * to the linear centroid accumulators.
   * 
   * @param pts an array of {@link Coordinates}s
   */
  private void addLineSegments(final LineString line) {
    double lineLen = 0.0;
    for (final Segment segment : line.segments()) {
      final double segmentLen = segment.getLength();
      if (segmentLen > 0.0) {
        lineLen += segmentLen;

        final double x1 = segment.getX(0);
        final double y1 = segment.getY(0);
        final double x2 = segment.getX(1);
        final double y2 = segment.getY(1);

        final double midx = (x1 + x2) / 2;
        final double midy = (y1 + y2) / 2;
        lineCenterX += segmentLen * midx;
        lineCenterY += segmentLen * midy;
      }
    }
    totalLength += lineLen;
    if (lineLen == 0.0 && line.getVertexCount() > 0) {
      addPoint(line.getVertex(0).cloneCoordinates());
    }
  }

  /**
   * Adds a point to the point centroid accumulator.
   * @param pt a {@link Coordinates}
   */
  private void addPoint(final Coordinates pt) {
    ptCount += 1;
    centSumX += pt.getX();
    centSumY += pt.getY();
  }

  private void addShell(final LineString line) {
    if (line.getVertexCount() > 0) {
      setBasePoint(line.getVertex(0).cloneCoordinates());
    }
    final boolean isPositiveArea = line.isClockwise();
    for (final Segment segment : line.segments()) {
      final Coordinates point1 = segment.get(0);
      final Coordinates point2 = segment.get(1);
      addTriangle(areaBasePt, point1, point2, isPositiveArea);
    }
    addLineSegments(line);
  }

  private void addTriangle(final Coordinates p0, final Coordinates p1,
    final Coordinates p2, final boolean isPositiveArea) {
    final double sign = (isPositiveArea) ? 1.0 : -1.0;

    final double triangleCent3X = p0.getX() + p1.getX() + p2.getX();
    final double triangleCent3Y = p0.getY() + p1.getY() + p2.getY();

    final double area2 = area2(p0, p1, p2);
    cg3X += sign * area2 * triangleCent3X;
    cg3Y += sign * area2 * triangleCent3Y;
    areasum2 += sign * area2;
  }

  /**
   * Gets the computed centroid.
   * 
   * @return the computed centroid, or null if the input is empty
   */
  public Coordinates getCentroid() {
    /**
     * The centroid is computed from the highest dimension components present in the input.
     * I.e. areas dominate lineal geometry, which dominates points.
     * Degenerate geometry are computed using their effective dimension
     * (e.g. areas may degenerate to lines or points)
     */
    if (Math.abs(areasum2) > 0.0) {
      /**
       * Input contains areal geometry
       */
      return new DoubleCoordinates(cg3X / 3 / areasum2, cg3Y / 3 / areasum2);
    } else if (totalLength > 0.0) {
      /**
       * Input contains lineal geometry
       */
      return new DoubleCoordinates(lineCenterX / totalLength, lineCenterY
        / totalLength);
    } else if (ptCount > 0) {
      /**
       * Input contains puntal geometry only
       */
      return new DoubleCoordinates(centSumX / ptCount, centSumY / ptCount);
    } else {
      return null;
    }
  }

  private void setBasePoint(final Coordinates basePt) {
    if (this.areaBasePt == null) {
      this.areaBasePt = basePt;
    }
  }

}
