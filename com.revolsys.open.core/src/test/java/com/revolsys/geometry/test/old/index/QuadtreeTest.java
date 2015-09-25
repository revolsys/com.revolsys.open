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
package com.revolsys.geometry.test.old.index;

import com.revolsys.geometry.algorithm.index.quadtree.QuadTree;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.test.old.util.SerializationUtil;

import junit.framework.TestCase;

/**
 * @version 1.7
 */
public class QuadtreeTest extends TestCase {

  public QuadtreeTest(final String name) {
    super(name);
  }

  public void testSerialization() throws Exception {
    final SpatialIndexTester tester = new SpatialIndexTester();
    tester.setSpatialIndex(new QuadTree<BoundingBoxDoubleGf>());
    tester.init();
    QuadTree<BoundingBoxDoubleGf> tree = (QuadTree<BoundingBoxDoubleGf>)tester.getSpatialIndex();
    final byte[] data = SerializationUtil.serialize(tree);
    tree = (QuadTree<BoundingBoxDoubleGf>)SerializationUtil.deserialize(data);
    tester.setSpatialIndex(tree);
    tester.run();
    assertTrue(tester.isSuccess());
  }

  public void testSpatialIndex() throws Exception {
    final SpatialIndexTester tester = new SpatialIndexTester();
    tester.setSpatialIndex(new QuadTree<BoundingBoxDoubleGf>());
    tester.init();
    tester.run();
    assertTrue(tester.isSuccess());
  }

}
