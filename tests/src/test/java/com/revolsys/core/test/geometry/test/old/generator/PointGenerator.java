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
package com.revolsys.core.test.geometry.test.old.generator;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;

/**
 *
 * Creates a point based on the bounding box.
 *
 * This implementation returns the centroid.
 *
 * @author David Zwiers, Vivid Solutions.
 */
public class PointGenerator extends GeometryGenerator {

  /**
   * @see com.revolsys.core.test.geometry.testold.generator.GeometryGenerator#newIterator()
   * @throws NullPointerException when either the Geometry Factory, or the Bounding Box are undefined.
   */
  @Override
  public Geometry newGeometry() {
    if (this.geometryFactory == null) {
      throw new NullPointerException("GeometryFactory is not declared");
    } else if (this.boundingBox == null || this.boundingBox.isEmpty()) {
      throw new NullPointerException("Bounding Box is not declared");
    } else {

      final Point point = this.boundingBox.toGeometry().getCentroid();
      return point.convertGeometry(this.geometryFactory);
    }
  }

}
