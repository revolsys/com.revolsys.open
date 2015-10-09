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
package com.revolsys.geometry.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.geometry.algorithm.BoundaryNodeRule;
import com.revolsys.geometry.algorithm.index.LineSegmentIndex;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.MultiPoint;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.operation.simple.DuplicateVertexError;
import com.revolsys.geometry.operation.simple.SelfIntersectionPointError;
import com.revolsys.geometry.operation.simple.SelfIntersectionVertexError;
import com.revolsys.geometry.operation.simple.SelfOverlapLineSegmentError;
import com.revolsys.geometry.operation.simple.SelfOverlapSegmentError;
import com.revolsys.geometry.operation.valid.GeometryValidationError;

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

  private final List<GeometryValidationError> errors = new ArrayList<>();

  private final Geometry geometry;

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

  public List<GeometryValidationError> getErrors() {
    return this.errors;
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
    if (this.errors.isEmpty()) {
      return null;
    } else {
      return this.errors.get(0).getErrorPoint();
    }
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
    return this.shortCircuit && !this.errors.isEmpty();
  }

  /**
   * Tests whether the geometry is simple.
   *
   * @return true if the geometry is simple
   */
  public boolean isSimple() {
    this.errors.clear();
    return isSimple(this.geometry);
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
    final LineSegmentIndex index = new LineSegmentIndex(lineal);
    for (final Segment segment : lineal.segments()) {
      final int segmentIndex = segment.getSegmentIndex();
      final int partIndex = segment.getPartIndex();
      if (segment.getLength() == 0) {
        this.errors.add(new DuplicateVertexError(segment.getGeometryVertex(0)));
      } else {
        final List<LineSegment> segments = index.queryBoundingBox(segment);
        for (final LineSegment lineSegment : segments) {
          final Segment segment2 = (Segment)lineSegment;
          final int partIndex2 = segment2.getPartIndex();
          final int segmentIndex2 = segment2.getSegmentIndex();
          if (partIndex2 > partIndex || partIndex == partIndex2 && segmentIndex2 > segmentIndex) {
            if (segment.equals(lineSegment)) {
              final SelfOverlapSegmentError error = new SelfOverlapSegmentError(segment);
              this.errors.add(error);
              if (this.shortCircuit) {
                return false;
              }
            } else {
              final Geometry intersection = segment.getIntersection(lineSegment);
              if (intersection instanceof Point) {
                final Point pointIntersection = (Point)intersection;
                boolean isIntersection = true;

                // Process segments on the same linestring part
                if (partIndex == partIndex2) {
                  // The end of the current segment can touch the start of the
                  // next
                  // segment
                  if (segmentIndex + 1 == segmentIndex2) {
                    if (lineSegment.equalsVertex(2, 0, pointIntersection)) {
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
                    final boolean segment1EndIntersection = isEndIntersection(segment,
                      pointIntersection);
                    final boolean segment2EndIntersection = isEndIntersection(segment2,
                      pointIntersection);

                    if (segment1EndIntersection && segment2EndIntersection) {
                      isIntersection = false;
                    }
                  }
                }
                if (isIntersection) {
                  GeometryValidationError error;
                  if (segment.equalsVertex(2, 0, pointIntersection)) {
                    final Vertex vertex = segment.getGeometryVertex(0);
                    error = new SelfIntersectionVertexError(vertex);
                  } else if (segment.equalsVertex(2, 1, pointIntersection)) {
                    final Vertex vertex = segment.getGeometryVertex(1);
                    error = new SelfIntersectionVertexError(vertex);
                  } else {
                    error = new SelfIntersectionPointError(this.geometry,
                      pointIntersection.newPointDouble());
                  }
                  this.errors.add(error);
                  if (this.shortCircuit) {
                    return false;
                  }
                }
              } else if (intersection instanceof LineSegment) {
                final LineSegment lineIntersection = (LineSegment)intersection;
                GeometryValidationError error;
                if (segment.equals(lineIntersection)) {
                  error = new SelfOverlapSegmentError(segment);
                } else if (lineSegment.equals(lineIntersection)) {
                  error = new SelfOverlapSegmentError(segment2);
                } else {
                  error = new SelfOverlapLineSegmentError(this.geometry, lineIntersection);
                }
                this.errors.add(error);
                if (this.shortCircuit) {
                  return false;
                }
              }
            }
          }
        }
      }
    }
    return this.errors.isEmpty();
  }

  private boolean isSimple(final MultiPoint mulitPoint) {
    boolean simple = true;
    final Set<Point> points = new TreeSet<>();
    for (final Vertex vertex : mulitPoint.vertices()) {
      final Point point = new PointDouble(vertex, 2);
      if (points.contains(point)) {
        simple = false;
        final DuplicateVertexError error = new DuplicateVertexError(vertex);
        this.errors.add(error);
        if (!isErrorReturn()) {
          return false;
        }
      } else {
        points.add(point);
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
