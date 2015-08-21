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
package com.revolsys.jts.testold.algorithm;

import java.util.List;

import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.Reader;
import com.revolsys.spring.resource.ClassPathResource;

import junit.framework.TestCase;

public class InteriorPointTest extends TestCase {
  public static List<Geometry> getTestGeometries(final String file) {
    final ClassPathResource resource = new ClassPathResource("/com/revolsys/jts/test/data/" + file);
    try (
      Reader<Geometry> reader = GeometryReader.create(resource)) {
      return reader.read();
    }
  }

  public InteriorPointTest(final String name) {
    super(name);
  }

  private void checkInteriorPointFile(final String file) throws Exception {
    final List<Geometry> geometries = getTestGeometries(file);
    for (final Geometry g : geometries) {
      final Point ip = g.getInteriorPoint();
      assertTrue(g.contains(ip));
    }
  }

  public void testAll() throws Exception {
    checkInteriorPointFile("world.wkt");
    checkInteriorPointFile("africa.wkt");
  }

}
