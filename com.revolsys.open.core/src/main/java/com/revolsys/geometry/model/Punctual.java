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

package com.revolsys.geometry.model;

import java.util.List;

import com.revolsys.datatype.DataTypes;

/**
 * Identifies {@link Geometry} subclasses which
 * are 0-dimensional and with components which are {@link Point}s.
 *
 * @author Martin Davis
 *
 */
public interface Punctual extends Geometry {
  @SuppressWarnings("unchecked")
  static <G extends Geometry> G newPunctual(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Punctual) {
      final Punctual punctual = (Punctual)value;
      if (punctual.getGeometryCount() == 1) {
        return punctual.getGeometry(0);
      } else {
        return (G)value;
      }
    } else if (value instanceof GeometryCollection) {
      final GeometryCollection geometryCollection = (GeometryCollection)value;
      if (geometryCollection.isEmpty()) {
        final GeometryFactory geometryFactory = geometryCollection.getGeometryFactory();
        return (G)geometryFactory.polygon();
      } else if (geometryCollection.getGeometryCount() == 1) {
        final Geometry part = geometryCollection.getGeometry(0);
        if (part instanceof Punctual) {
          final Punctual punctual = (Punctual)part;
          return (G)punctual;
        }
      }
      throw new IllegalArgumentException("Expecting a Punctual geometry not "
        + geometryCollection.getGeometryType() + "\n" + geometryCollection);
    } else if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      throw new IllegalArgumentException(
        "Expecting a Punctual geometry not " + geometry.getGeometryType() + "\n" + geometry);
    } else {
      final String string = DataTypes.toString(value);
      final Geometry geometry = GeometryFactory.DEFAULT.geometry(string, false);
      return (G)newPunctual(geometry);
    }
  }

  double getCoordinate(int partIndex, int axisIndex);

  Point getPoint(int i);

  default <V extends Point> List<V> getPoints() {
    return getGeometries();
  }

  default Iterable<Point> points() {
    return getGeometries();
  }
}
