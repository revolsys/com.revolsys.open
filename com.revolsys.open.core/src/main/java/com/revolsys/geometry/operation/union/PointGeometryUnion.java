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

package com.revolsys.geometry.operation.union;

import java.util.Set;
import java.util.TreeSet;

import com.revolsys.geometry.algorithm.PointLocator;
import com.revolsys.geometry.model.CoordinateArrays;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.model.util.GeometryCombiner;

/**
 * Computes the union of a {@link Punctual} geometry with
 * another arbitrary {@link Geometry}.
 * Does not copy any component geometries.
 *
 * @author mbdavis
 *
 */
public class PointGeometryUnion {
  public static Geometry union(final Punctual pointGeom, final Geometry otherGeom) {
    final PointGeometryUnion unioner = new PointGeometryUnion(pointGeom, otherGeom);
    return unioner.union();
  }

  private final GeometryFactory geomFact;

  private final Geometry otherGeom;

  private final Geometry pointGeom;

  public PointGeometryUnion(final Punctual pointGeom, final Geometry otherGeom) {
    this.pointGeom = pointGeom;
    this.otherGeom = otherGeom;
    this.geomFact = otherGeom.getGeometryFactory();
  }

  public Geometry union() {
    final PointLocator locater = new PointLocator();
    // use a set to eliminate duplicates, as required for union
    final Set exteriorCoords = new TreeSet();

    for (int i = 0; i < this.pointGeom.getGeometryCount(); i++) {
      final Point point = (Point)this.pointGeom.getGeometry(i);
      final Point coord = point.getPoint();
      final Location loc = locater.locate(coord, this.otherGeom);
      if (loc == Location.EXTERIOR) {
        exteriorCoords.add(coord);
      }
    }

    // if no points are in exterior, return the other geom
    if (exteriorCoords.size() == 0) {
      return this.otherGeom;
    }

    // make a puntal geometry of appropriate size
    Geometry ptComp = null;
    final Point[] coords = CoordinateArrays.toCoordinateArray(exteriorCoords);
    if (coords.length == 1) {
      ptComp = this.geomFact.point(coords[0]);
    } else {
      ptComp = this.geomFact.punctual(coords);
    }

    // add point component to the other geometry
    return GeometryCombiner.combine(ptComp, this.otherGeom);
  }
}
