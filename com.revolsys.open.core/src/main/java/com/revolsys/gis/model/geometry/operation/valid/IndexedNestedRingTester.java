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
package com.revolsys.gis.model.geometry.operation.valid;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.geometry.LinearRing;
import com.revolsys.gis.model.geometry.impl.BoundingBox;
import com.revolsys.gis.model.geometry.operation.geomgraph.GeometryGraph;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * Tests whether any of a set of {@link LinearRing}s are
 * nested inside another ring in the set, using a spatial
 * index to speed up the comparisons.
 *
 * @version 1.7
 */
public class IndexedNestedRingTester
{
  private GeometryGraph graph;  // used to find non-node vertices
  private List rings = new ArrayList();
  private BoundingBox totalEnv = new BoundingBox();
  private SpatialIndex index;
  private Coordinates nestedPt;

  public IndexedNestedRingTester(GeometryGraph graph)
  {
    this.graph = graph;
  }

  public Coordinates getNestedPoint() { return nestedPt; }

  public void add(LinearRing ring)
  {
    rings.add(ring);
    totalEnv = totalEnv.expandToInclude(ring.getBoundingBox());
  }

  public boolean isNonNested()
  {
    buildIndex();

    for (int i = 0; i < rings.size(); i++) {
      LinearRing innerRing = (LinearRing) rings.get(i);
      CoordinatesList innerRingPts = innerRing;

      List results = index.query(JtsGeometryUtil.getEnvelope(innerRing.getBoundingBox()));
//System.out.println(results.size());
      for (int j = 0; j < results.size(); j++) {
        LinearRing searchRing = (LinearRing) results.get(j);
        CoordinatesList searchRingPts = searchRing;

        if (innerRing == searchRing)
          continue;

        if (! innerRing.getBoundingBox().intersects(searchRing.getBoundingBox()))
          continue;

        Coordinates innerRingPt = IsValidOp.findPtNotNode(innerRingPts, searchRing, graph);
        
        /**
         * If no non-node pts can be found, this means
         * that the searchRing touches ALL of the innerRing vertices.
         * This indicates an invalid polygon, since either
         * the two holes create a disconnected interior, 
         * or they touch in an infinite number of points 
         * (i.e. along a line segment).
         * Both of these cases are caught by other tests,
         * so it is safe to simply skip this situation here.
         */
        if (innerRingPt == null)
          continue;

        boolean isInside = CoordinatesListUtil.isPointInRing(innerRingPt, searchRingPts);
        if (isInside) {
          nestedPt = innerRingPt;
          return false;
        }
      }
    }
    return true;
  }

  private void buildIndex()
  {
    index = new STRtree();

    for (int i = 0; i < rings.size(); i++) {
      LinearRing ring = (LinearRing) rings.get(i);
      BoundingBox env = ring.getBoundingBox();
      index.insert(JtsGeometryUtil.getEnvelope(env), ring);
    }
  }
}
