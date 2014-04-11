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
package com.revolsys.jts.densify;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateList;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.geom.util.GeometryTransformer;

/**
 * Densifies a {@link Geometry} by inserting extra vertices along the line segments
 * contained in the geometry. 
 * All segments in the created densified geometry will be no longer than
 * than the given distance tolerance.
 * Densified polygonal geometries are guaranteed to be topologically correct.
 * The coordinates created during densification respect the input geometry's
 * {@link PrecisionModel}.
 * <p>
 * <b>Note:</b> At some future point this class will
 * offer a variety of densification strategies.
 * 
 * @author Martin Davis
 */
public class Densifier {
  class DensifyTransformer extends GeometryTransformer {
    /**
     * Creates a valid area geometry from one that possibly has bad topology
     * (i.e. self-intersections). Since buffer can handle invalid topology, but
     * always returns valid geometry, constructing a 0-width buffer "corrects"
     * the topology. Note this only works for area geometries, since buffer
     * always returns areas. This also may return empty geometries, if the input
     * has no actual area.
     * 
     * @param roughAreaGeom
     *          an area geometry possibly containing self-intersections
     * @return a valid area geometry
     */
    private Geometry createValidArea(final Geometry roughAreaGeom) {
      return roughAreaGeom.buffer(0.0);
    }

    @Override
    protected CoordinatesList transformCoordinates(
      final CoordinatesList coords, final Geometry parent) {
      final Coordinates[] inputPts = coords.toCoordinateArray();
      Coordinates[] newPts = Densifier.densifyPoints(inputPts,
        distanceTolerance, parent.getPrecisionModel());
      // prevent creation of invalid linestrings
      if (parent instanceof LineString && newPts.length == 1) {
        newPts = new Coordinates[0];
      }
      return factory.getCoordinateSequenceFactory().create(newPts);
    }

    @Override
    protected Geometry transformMultiPolygon(final MultiPolygon geom,
      final Geometry parent) {
      final Geometry roughGeom = super.transformMultiPolygon(geom, parent);
      return createValidArea(roughGeom);
    }

    @Override
    protected Geometry transformPolygon(final Polygon geom,
      final Geometry parent) {
      final Geometry roughGeom = super.transformPolygon(geom, parent);
      // don't try and correct if the parent is going to do this
      if (parent instanceof MultiPolygon) {
        return roughGeom;
      }
      return createValidArea(roughGeom);
    }
  }

  /**
   * Densifies a geometry using a given distance tolerance,
   * and respecting the input geometry's {@link PrecisionModel}.
   * 
   * @param geom the geometry to densify
   * @param distanceTolerance the distance tolerance to densify
   * @return the densified geometry
   */
  public static Geometry densify(final Geometry geom,
    final double distanceTolerance) {
    final Densifier densifier = new Densifier(geom);
    densifier.setDistanceTolerance(distanceTolerance);
    return densifier.getResultGeometry();
  }

  /**
   * Densifies a coordinate sequence.
   * 
   * @param pts
   * @param distanceTolerance
   * @return the densified coordinate sequence
   */
  private static Coordinates[] densifyPoints(final Coordinates[] pts,
    final double distanceTolerance, final PrecisionModel precModel) {
    final LineSegment seg = new LineSegment();
    final CoordinateList coordList = new CoordinateList();
    for (int i = 0; i < pts.length - 1; i++) {
      seg.p0 = pts[i];
      seg.p1 = pts[i + 1];
      coordList.add(seg.p0, false);
      final double len = seg.getLength();
      final int densifiedSegCount = (int)(len / distanceTolerance) + 1;
      if (densifiedSegCount > 1) {
        final double densifiedSegLen = len / densifiedSegCount;
        for (int j = 1; j < densifiedSegCount; j++) {
          final double segFract = (j * densifiedSegLen) / len;
          final Coordinates p = seg.pointAlong(segFract);
          precModel.makePrecise(p);
          coordList.add(p, false);
        }
      }
    }
    coordList.add(pts[pts.length - 1], false);
    return coordList.toCoordinateArray();
  }

  private final Geometry inputGeom;

  private double distanceTolerance;

  /**
   * Creates a new densifier instance.
   * 
   * @param inputGeom
   */
  public Densifier(final Geometry inputGeom) {
    this.inputGeom = inputGeom;
  }

  /**
   * Gets the densified geometry.
   * 
   * @return the densified geometry
   */
  public Geometry getResultGeometry() {
    return (new DensifyTransformer()).transform(inputGeom);
  }

  /**
   * Sets the distance tolerance for the densification. All line segments
   * in the densified geometry will be no longer than the distance tolereance.
   * simplified geometry will be within this distance of the original geometry.
   * The distance tolerance must be positive.
   * 
   * @param distanceTolerance
   *          the densification tolerance to use
   */
  public void setDistanceTolerance(final double distanceTolerance) {
    if (distanceTolerance <= 0.0) {
      throw new IllegalArgumentException("Tolerance must be positive");
    }
    this.distanceTolerance = distanceTolerance;
  }

}
