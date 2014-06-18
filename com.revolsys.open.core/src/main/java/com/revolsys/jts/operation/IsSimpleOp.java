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
package com.revolsys.jts.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.jts.algorithm.BoundaryNodeRule;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Lineal;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.geom.segment.LineSegment;
import com.revolsys.jts.geom.segment.Segment;

/**
 * Tests whether a <code>Geometry</code> is simple.
 * In general, the SFS specification of simplicity
 * follows the rule:
 * <ul>
 *    <li> A Geometry is simple if and only if the only self-intersections are at
 *    boundary points.
 * </ul>
 * <p>
 * Simplicity is defined for each {@link Geometry} type as follows:
 * <ul>
 * <li><b>Polygonal</b> geometries are simple by definition, so
 * <code>isSimple</code> trivially returns true.
 * (Note: this means that <tt>isSimple</tt> cannot be used to test 
 * for (invalid) self-intersections in <tt>Polygon</tt>s.  
 * In order to check if a <tt>Polygonal</tt> geometry has self-intersections,
 * use {@link Geometry#isValid()}).
 * <li><b>Linear</b> geometries are simple iff they do <i>not</i> self-intersect at interior points
 * (i.e. points other than boundary points).
 * This is equivalent to saying that no two linear components satisfy the SFS {@link Geometry#touches(Geometry)}
 * predicate. 
 * <li><b>Zero-dimensional (point)</b> geometries are simple if and only if they have no
 * repeated points.
 * <li><b>Empty</b> geometries are <i>always</i> simple, by definition
 * </ul>
 * For {@link Lineal} geometries the evaluation of simplicity  
 * can be customized by supplying a {@link BoundaryNodeRule} 
 * to define how boundary points are determined.
 * The default is the SFS-standard {@link BoundaryNodeRule#MOD2_BOUNDARY_RULE}.
 * Note that under the <tt>Mod-2</tt> rule, closed <tt>LineString</tt>s (rings)
 * will never satisfy the <tt>touches</tt> predicate at their endpoints, since these are
 * interior points, not boundary points. 
 * If it is required to test whether a set of <code>LineString</code>s touch
 * only at their endpoints, use <code>IsSimpleOp</code> with {@link BoundaryNodeRule#ENDPOINT_BOUNDARY_RULE}.
 * For example, this can be used to validate that a set of lines form a topologically valid
 * linear network.
 * 
 * @see BoundaryNodeRule
 *
 * @version 1.7
 */
public class IsSimpleOp {

  private final Geometry geometry;

  private final List<Point> nonSimplePoints = new ArrayList<Point>();

  private boolean shortCircuit = true;

  /**
   * Creates a simplicity checker using the default SFS Mod-2 Boundary Node Rule
   *
   * @param geometry the geometry to test
   */
  public IsSimpleOp(final Geometry geometry) {
    this.geometry = geometry;
  }

  public IsSimpleOp(final Geometry geometry, final boolean shortCircuit) {
    this.geometry = geometry;
    this.shortCircuit = shortCircuit;
  }

  /**
   * Gets a coordinate for the location where the geometry
   * fails to be simple. 
   * (i.e. where it has a non-boundary self-intersection).
   * {@link #isSimple} must be called before this method is called.
   *
   * @return a coordinate for the location of the non-boundary self-intersection
   * or null if the geometry is simple
   */
  public Point getNonSimpleLocation() {
    if (nonSimplePoints.isEmpty()) {
      return null;
    } else {
      return nonSimplePoints.get(0);
    }
  }

  public List<Point> getNonSimplePoints() {
    return nonSimplePoints;
  }

  private boolean isEndIntersection(final Segment segment, final Point point) {
    if (segment.isLineStart()) {
      return segment.equalsVertex(2, 0, point);
    } else if (segment.isLineEnd()) {
      return segment.equalsVertex(2, 1, point);
    } else {
      return false;
    }
  }

  private boolean isErrorReturn() {
    return shortCircuit && !nonSimplePoints.isEmpty();
  }

  /**
   * Tests whether the geometry is simple.
   *
   * @return true if the geometry is simple
   */
  public boolean isSimple() {
    nonSimplePoints.clear();
    return isSimple(geometry);
  }

  private boolean isSimple(final Geometry geometry) {
    if (geometry.isEmpty()) {
      return true;
    } else if (geometry instanceof Lineal) {
      return isSimple((Lineal)geometry);
    } else if (geometry instanceof Polygon) {
      return isSimple((Polygon)geometry);
    } else if (geometry instanceof MultiPoint) {
      return isSimple((MultiPoint)geometry);
    } else if (geometry instanceof GeometryCollection) {
      return isSimple((GeometryCollection)geometry);
    } else {
      return true;
    }
  }

  /**
   * Semantics for GeometryCollection is 
   * simple if all components are simple.
   * 
   * @param geom
   * @return true if the geometry is simple
   */
  private boolean isSimple(final GeometryCollection geom) {
    boolean simple = true;
    for (final Geometry part : geom.geometries()) {
      simple &= isSimple(part);
      if (isErrorReturn()) {
        return false;
      }
    }
    return simple;
  }

  private boolean isSimple(final Lineal lineal) {
    final Segment segment2 = (Segment)lineal.segments().iterator();
    for (final Segment segment : lineal.segments()) {
      final int partIndex = segment.getPartIndex();
      final int segmentIndex = segment.getSegmentIndex();
      segment2.setSegmentId(segment.getSegmentId());
      boolean nextSegment = true;
      while (segment2.hasNext()) {
        segment2.next();
        final Geometry intersection = segment.getIntersection(segment2);
        if (intersection instanceof Point) {
          final Point pointIntersection = (Point)intersection;
          boolean isIntersection = true;

          final int partIndex2 = segment2.getPartIndex();
          // Process segments on the same linestring part
          if (partIndex == partIndex2) {
            final int segmentIndex2 = segment2.getSegmentIndex();
            // The end of the current segment can touch the start of the next
            // segment
            if (segmentIndex + 1 == segmentIndex2) {
              if (segment2.equalsVertex(2, 0, pointIntersection)) {
                isIntersection = false;
              }
              // A loop can touch itself at the start/end
            } else if (segment.isLineClosed()) {
              if (segment.isLineStart() && segment2.isLineEnd()) {
                if (segment.equalsVertex(2, 0, pointIntersection)) {
                  isIntersection = false;
                }
              }
            }
          } else {
            if (!segment.isLineClosed() && !segment2.isLineClosed()) {
              final boolean segment1EndIntersection = isEndIntersection(
                segment, pointIntersection);
              final boolean segment2EndIntersection = isEndIntersection(
                segment2, pointIntersection);

              if (segment1EndIntersection && segment2EndIntersection) {
                isIntersection = false;
              }
            }
          }
          if (isIntersection) {
            nonSimplePoints.add(pointIntersection);
            if (shortCircuit) {
              return false;
            }
          }
        } else if (intersection instanceof LineSegment) {
          final LineSegment lineIntersection = (LineSegment)intersection;
          nonSimplePoints.add(lineIntersection.getPoint(0));
          nonSimplePoints.add(lineIntersection.getPoint(1));
          if (shortCircuit) {
            return false;
          }
        }
        nextSegment = false;
      }
    }
    return nonSimplePoints.isEmpty();
  }

  private boolean isSimple(final MultiPoint mulitPoint) {
    boolean simple = true;
    final Set<Point> points = new TreeSet<>();
    for (final Point point : mulitPoint.getPoints()) {
      final Point coordinates = new PointDouble(point, 2);
      if (points.contains(coordinates)) {
        simple = false;
        nonSimplePoints.add(coordinates);
        if (!isErrorReturn()) {
          return false;
        }
      } else {
        points.add(coordinates);
      }
    }
    return simple;
  }

  /**
   * Computes simplicity for polygonal geometries.
   * Polygonal geometries are simple if and only if
   * all of their component rings are simple.
   * 
   * @param geom a Polygonal geometry
   * @return true if the geometry is simple
   */
  private boolean isSimple(final Polygon polygon) {
    boolean simple = true;
    for (final LineString ring : polygon.rings()) {
      simple &= isSimple(ring);
      if (isErrorReturn()) {
        return false;
      }
    }
    return simple;
  }
}
