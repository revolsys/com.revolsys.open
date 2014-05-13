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

package com.revolsys.jts.awt;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateList;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Polygon;

/**
 * Converts a Java2D {@link Shape} 
 * or the more general {@link PathIterator} into a {@link Geometry}.
 * <p>
 * The coordinate system for Java2D is typically screen coordinates, 
 * which has the Y axis inverted
 * relative to the usual JTS coordinate system.
 * This is rectified during conversion. 
 * <p>
 * PathIterators to be converted are expected to be linear or flat.
 * That is, they should contain only <tt>SEG_MOVETO</tt>, <tt>SEG_LINETO</tt>, and <tt>SEG_CLOSE</tt> segment types.
 * Any other segment types will cause an exception.
 * 
 * @author Martin Davis
 *
 */
public class ShapeReader {
  private static final AffineTransform INVERT_Y = AffineTransform.getScaleInstance(
    1, -1);

  private static Point[] nextCoordinateArray(final PathIterator pathIt) {
    final double[] pathPt = new double[6];
    CoordinateList coordList = null;
    boolean isDone = false;
    while (!pathIt.isDone()) {
      final int segType = pathIt.currentSegment(pathPt);
      switch (segType) {
        case PathIterator.SEG_MOVETO:
          if (coordList != null) {
            // don't advance pathIt, to retain start of next path if any
            isDone = true;
          } else {
            coordList = new CoordinateList();
            coordList.add(new Coordinate(pathPt[0], pathPt[1],
              Point.NULL_ORDINATE));
            pathIt.next();
          }
        break;
        case PathIterator.SEG_LINETO:
          coordList.add(new Coordinate(pathPt[0], pathPt[1],
            Point.NULL_ORDINATE));
          pathIt.next();
        break;
        case PathIterator.SEG_CLOSE:
          coordList.closeRing();
          pathIt.next();
          isDone = true;
        break;
        default:
          throw new IllegalArgumentException(
            "unhandled (non-linear) segment type encountered");
      }
      if (isDone) {
        break;
      }
    }
    return coordList.toCoordinateArray();
  }

  /**
   * Converts a flat path to a {@link Geometry}.
   * 
   * @param pathIt the path to convert
   * @param geomFact the GeometryFactory to use
   * @return a Geometry representing the path
   */
  public static Geometry read(final PathIterator pathIt,
    final GeometryFactory geomFact) {
    final ShapeReader pc = new ShapeReader(geomFact);
    return pc.read(pathIt);
  }

  /**
   * Converts a Shape to a Geometry, flattening it first.
   * 
   * @param shp the Java2D shape
   * @param flatness the flatness parameter to use
   * @param geomFact the GeometryFactory to use
   * @return a Geometry representing the shape
   */
  public static Geometry read(final Shape shp, final double flatness,
    final GeometryFactory geomFact) {
    final PathIterator pathIt = shp.getPathIterator(INVERT_Y, flatness);
    return ShapeReader.read(pathIt, geomFact);
  }

  /**
   * Extracts the points of the paths in a flat {@link PathIterator} into
   * a list of Point arrays.
   * 
   * @param pathIt a path iterator
   * @return a List of Point arrays
   * @throws IllegalArgumentException if a non-linear segment type is encountered
   */
  public static List toCoordinates(final PathIterator pathIt) {
    final List coordArrays = new ArrayList();
    while (!pathIt.isDone()) {
      final Point[] pts = nextCoordinateArray(pathIt);
      if (pts == null) {
        break;
      }
      coordArrays.add(pts);
    }
    return coordArrays;
  }

  private final GeometryFactory geometryFactory;

  public ShapeReader(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  private boolean isHole(final Point[] pts) {
    return CGAlgorithms.isCCW(pts);
  }

  /**
   * Converts a flat path to a {@link Geometry}.
   * 
   * @param pathIt the path to convert
   * @return a Geometry representing the path
   */
  public Geometry read(final PathIterator pathIt) {
    final List pathPtSeq = toCoordinates(pathIt);

    final List<Polygon> polys = new ArrayList<>();
    int seqIndex = 0;
    while (seqIndex < pathPtSeq.size()) {
      // assume next seq is shell
      // TODO: test this
      final Point[] pts = (Point[])pathPtSeq.get(seqIndex);
      seqIndex++;

      final List<LinearRing> rings = new ArrayList<>();
      final LinearRing shell = geometryFactory.linearRing(pts);
      rings.add(shell);
      // add holes as long as rings are CCW
      while (seqIndex < pathPtSeq.size()
        && isHole((Point[])pathPtSeq.get(seqIndex))) {
        final Point[] holePts = (Point[])pathPtSeq.get(seqIndex);
        final LinearRing hole = geometryFactory.linearRing(holePts);
        rings.add(hole);
        seqIndex++;
      }
      final Polygon polygon = geometryFactory.polygon(rings);
      polys.add(polygon);
    }
    return geometryFactory.buildGeometry(polys);
  }

}
