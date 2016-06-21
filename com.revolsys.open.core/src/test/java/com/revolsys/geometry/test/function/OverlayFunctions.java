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
import java.util.List;

import com.revolsys.geometry.model.Geometry;

public class OverlayFunctions {
  public static Geometry clip(final Geometry a, final Geometry mask) {
    final List geoms = new ArrayList();
    for (int i = 0; i < a.getGeometryCount(); i++) {
      final Geometry clip = a.getGeometry(i).intersection(mask);
      geoms.add(clip);
    }
    return FunctionsUtil.buildGeometry(geoms, a);
  }

  public static Geometry difference(final Geometry a, final Geometry b) {
    return a.difference(b);
  }

  public static Geometry differenceBA(final Geometry a, final Geometry b) {
    return b.difference(a);
  }

  public static Geometry intersection(final Geometry a, final Geometry b) {
    return a.intersection(b);
  }

  public static Geometry symDifference(final Geometry a, final Geometry b) {
    return a.symDifference(b);
  }

  public static Geometry unaryUnion(final Geometry a) {
    return a.union();
  }

  public static Geometry union(final Geometry a, final Geometry b) {
    return a.union(b);
  }

  public static Geometry unionUsingGeometryCollection(final Geometry a, final Geometry b) {
    final Geometry gc = a.getGeometryFactory().geometry(a, b);
    return gc.union();
  }

}
