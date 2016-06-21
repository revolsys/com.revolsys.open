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
package com.revolsys.geometry.test.function;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollectionIterator;

public class BufferByUnionFunctions {

  public static Geometry bufferByChains(final Geometry g, final double distance,
    final int maxChainSize) {
    if (maxChainSize <= 0) {
      throw new IllegalArgumentException(
        "Maximum Chain Size must be specified as an input parameter");
    }
    final Geometry segs = LineHandlingFunctions.extractChains(g, maxChainSize);
    final double posDist = Math.abs(distance);
    final Geometry segBuf = bufferByComponents(segs, posDist);
    if (distance < 0.0) {
      return g.difference(segBuf);
    }
    return g.union(segBuf);
  }

  public static Geometry bufferByComponents(final Geometry g, final double distance) {
    return componentBuffers(g, distance).union();
  }

  /**
   * Buffer polygons by buffering the individual boundary segments and
   * either unioning or differencing them.
   *
   * @param g
   * @param distance
   * @return the buffer geometry
   */
  public static Geometry bufferBySegments(final Geometry g, final double distance) {
    final Geometry segs = LineHandlingFunctions.extractSegments(g);
    final double posDist = Math.abs(distance);
    final Geometry segBuf = bufferByComponents(segs, posDist);
    if (distance < 0.0) {
      return g.difference(segBuf);
    }
    return g.union(segBuf);
  }

  public static Geometry componentBuffers(final Geometry g, final double distance) {
    final List bufs = new ArrayList();
    for (final Iterator it = new GeometryCollectionIterator(g); it.hasNext();) {
      final Geometry part = (Geometry)it.next();
      if (part.isGeometryCollection()) {
        continue;
      }
      bufs.add(part.buffer(distance));
    }
    return FunctionsUtil.getFactoryOrDefault(g).geometryCollection(bufs);
  }
}
