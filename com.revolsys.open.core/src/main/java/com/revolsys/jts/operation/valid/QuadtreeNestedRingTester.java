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
package com.revolsys.jts.operation.valid;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geomgraph.GeometryGraph;
import com.revolsys.jts.index.quadtree.Quadtree;
import com.revolsys.jts.util.Assert;

/**
 * Tests whether any of a set of {@link LinearRing}s are
 * nested inside another ring in the set, using a {@link Quadtree}
 * index to speed up the comparisons.
 *
 * @version 1.7
 */
public class QuadtreeNestedRingTester {

  private final GeometryGraph graph; // used to find non-node vertices

  private final List rings = new ArrayList();

  private final Envelope totalEnv = new Envelope();

  private Quadtree quadtree;

  private Coordinates nestedPt;

  public QuadtreeNestedRingTester(final GeometryGraph graph) {
    this.graph = graph;
  }

  public void add(final LinearRing ring) {
    rings.add(ring);
    totalEnv.expandToInclude(ring.getBoundingBox());
  }

  private void buildQuadtree() {
    quadtree = new Quadtree();

    for (int i = 0; i < rings.size(); i++) {
      final LinearRing ring = (LinearRing)rings.get(i);
      final BoundingBox env = ring.getBoundingBox();
      quadtree.insert(env, ring);
    }
  }

  public Coordinates getNestedPoint() {
    return nestedPt;
  }

  public boolean isNonNested() {
    buildQuadtree();

    for (int i = 0; i < rings.size(); i++) {
      final LinearRing innerRing = (LinearRing)rings.get(i);
      final Coordinates[] innerRingPts = innerRing.getCoordinateArray();

      final List results = quadtree.query(innerRing.getBoundingBox());
      // System.out.println(results.size());
      for (int j = 0; j < results.size(); j++) {
        final LinearRing searchRing = (LinearRing)results.get(j);

        if (innerRing == searchRing) {
          continue;
        }

        if (!innerRing.getBoundingBox().intersects(searchRing.getBoundingBox())) {
          continue;
        }

        final Coordinates innerRingPt = IsValidOp.findPtNotNode(innerRingPts,
          searchRing, graph);
        Assert.isTrue(innerRingPt != null,
          "Unable to find a ring point not a node of the search ring");
        // Coordinates innerRingPt = innerRingPts[0];

        final boolean isInside = CGAlgorithms.isPointInRing(innerRingPt,
          searchRing);
        if (isInside) {
          nestedPt = innerRingPt;
          return false;
        }
      }
    }
    return true;
  }
}
