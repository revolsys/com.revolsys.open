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

package com.revolsys.geometry.linearref;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;

/**
 * Computes the {@link LinearLocation} for a given length
 * along a linear {@link Geometry}.
 * Negative lengths are measured in reverse from end of the linear geometry.
 * Out-of-range values are clamped.
 */
public class LengthLocationMap {
  // TODO: cache computed cumulative length for each vertex
  // TODO: support user-defined measures
  // TODO: support measure index for fast mapping to a location

  /**
   * Computes the length for a given {@link LinearLocation}
   * on a linear {@link Geometry}.
   *
   * @param linearGeom the linear geometry to use
   * @param loc the {@link LinearLocation} index of the location
   * @return the length for the {@link LinearLocation}
   */
  public static double getLength(final Geometry linearGeom, final LinearLocation loc) {
    final LengthLocationMap locater = new LengthLocationMap(linearGeom);
    return locater.getLength(loc);
  }

  /**
   * Computes the {@link LinearLocation} for a
   * given length along a linear {@link Geometry}.
   *
   * @param linearGeom the linear geometry to use
   * @param length the length index of the location
   * @return the {@link LinearLocation} for the length
   */
  public static LinearLocation getLocation(final Geometry linearGeom, final double length) {
    final LengthLocationMap locater = new LengthLocationMap(linearGeom);
    return locater.getLocation(length);
  }

  /**
   * Computes the {@link LinearLocation} for a
   * given length along a linear {@link Geometry},
   * with control over how the location
   * is resolved at component endpoints.
   *
   * @param linearGeom the linear geometry to use
   * @param length the length index of the location
   * @param resolveLower if true lengths are resolved to the lowest possible index
   * @return the {@link LinearLocation} for the length
   */
  public static LinearLocation getLocation(final Geometry linearGeom, final double length,
    final boolean resolveLower) {
    final LengthLocationMap locater = new LengthLocationMap(linearGeom);
    return locater.getLocation(length, resolveLower);
  }

  private final Geometry linearGeom;

  public LengthLocationMap(final Geometry linearGeom) {
    this.linearGeom = linearGeom;
  }

  public double getLength(final LinearLocation loc) {
    double totalLength = 0.0;

    final LinearIterator it = new LinearIterator(this.linearGeom);
    while (it.hasNext()) {
      if (!it.isEndOfLine()) {
        final Point p0 = it.getSegmentStart();
        final Point p1 = it.getSegmentEnd();
        final double segLen = p1.distance(p0);
        // length falls in this segment
        if (loc.getComponentIndex() == it.getComponentIndex()
          && loc.getSegmentIndex() == it.getVertexIndex()) {
          return totalLength + segLen * loc.getSegmentFraction();
        }
        totalLength += segLen;
      }
      it.next();
    }
    return totalLength;
  }

  /**
   * Compute the {@link LinearLocation} corresponding to a length.
   * Negative lengths are measured in reverse from end of the linear geometry.
   * Out-of-range values are clamped.
   * Ambiguous indexes are resolved to the lowest possible location value.
   *
   * @param length the length index
   * @return the corresponding LinearLocation
   */
  public LinearLocation getLocation(final double length) {
    return getLocation(length, true);
  }

  /**
   * Compute the {@link LinearLocation} corresponding to a length.
   * Negative lengths are measured in reverse from end of the linear geometry.
   * Out-of-range values are clamped.
   * Ambiguous indexes are resolved to the lowest or highest possible location value,
   * depending on the value of <tt>resolveLower</tt>
   *
   * @param length the length index
   * @return the corresponding LinearLocation
   */
  public LinearLocation getLocation(final double length, final boolean resolveLower) {
    double forwardLength = length;

    // negative values are measured from end of geometry
    if (length < 0.0) {
      final double lineLen = this.linearGeom.getLength();
      forwardLength = lineLen + length;
    }
    final LinearLocation loc = getLocationForward(forwardLength);
    if (resolveLower) {
      return loc;
    }
    return resolveHigher(loc);
  }

  private LinearLocation getLocationForward(final double length) {
    if (length <= 0.0) {
      return new LinearLocation();
    }

    double totalLength = 0.0;

    final LinearIterator it = new LinearIterator(this.linearGeom);
    while (it.hasNext()) {

      /**
       * Special handling is required for the situation when the
       * length references exactly to a component endpoint.
       * In this case, the endpoint location of the current component
       * is returned,
       * rather than the startpoint location of the next component.
       * This produces consistent behaviour with the project method.
       */
      if (it.isEndOfLine()) {
        if (totalLength == length) {
          final int compIndex = it.getComponentIndex();
          final int segIndex = it.getVertexIndex();
          return new LinearLocation(compIndex, segIndex, 0.0);
        }
      } else {
        final Point p0 = it.getSegmentStart();
        final Point p1 = it.getSegmentEnd();
        final double segLen = p1.distance(p0);
        // length falls in this segment
        if (totalLength + segLen > length) {
          final double frac = (length - totalLength) / segLen;
          final int compIndex = it.getComponentIndex();
          final int segIndex = it.getVertexIndex();
          return new LinearLocation(compIndex, segIndex, frac);
        }
        totalLength += segLen;
      }

      it.next();
    }
    // length is longer than line - return end location
    return LinearLocation.getEndLocation(this.linearGeom);
  }

  private LinearLocation resolveHigher(final LinearLocation loc) {
    if (!loc.isEndpoint(this.linearGeom)) {
      return loc;
    }
    int compIndex = loc.getComponentIndex();
    // if last component can't resolve any higher
    if (compIndex >= this.linearGeom.getGeometryCount() - 1) {
      return loc;
    }

    do {
      compIndex++;
    } while (compIndex < this.linearGeom.getGeometryCount() - 1
      && this.linearGeom.getGeometry(compIndex).getLength() == 0);
    // resolve to next higher location
    return new LinearLocation(compIndex, 0, 0.0);
  }
}
