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

package com.revolsys.geometry.triangulate.quadedge;

import java.util.Collection;
import java.util.LinkedList;

/**
 * A framework to visit sets of edge-connected {@link QuadEdgeTriangle}s in breadth-first order
 *
 * @author Martin Davis
 * @version 1.0
 */
public class EdgeConnectedTriangleTraversal {
  private final LinkedList triQueue = new LinkedList();

  public EdgeConnectedTriangleTraversal() {
  }

  /**
   * Called to initialize the traversal queue with a given set of {@link QuadEdgeTriangle}s
   *
   * @param tris a collection of QuadEdgeTriangle
   */
  public void init(final Collection tris) {
    this.triQueue.addAll(tris);
  }

  public void init(final QuadEdgeTriangle tri) {
    this.triQueue.addLast(tri);
  }

  /**
   * Subclasses can call this method to add a triangle to the end of the queue. This is useful for
   * initializing the queue to a chosen set of triangles.
   *
   * @param tri a triangle
   */
  /*
   * protected void addLast(QuadEdgeTriangle tri) { triQueue.addLast(tri); }
   */

  private void process(final QuadEdgeTriangle currTri, final TraversalVisitor visitor) {
    currTri.getNeighbours();
    for (int i = 0; i < 3; i++) {
      final QuadEdgeTriangle neighTri = (QuadEdgeTriangle)currTri.getEdge(i).sym().getData();
      if (neighTri == null) {
        continue;
      }
      if (visitor.visit(currTri, i, neighTri)) {
        this.triQueue.addLast(neighTri);
      }
    }
  }

  /**
   * Subclasses call this method to perform the visiting process.
   */
  public void visitAll(final TraversalVisitor visitor) {
    while (!this.triQueue.isEmpty()) {
      final QuadEdgeTriangle tri = (QuadEdgeTriangle)this.triQueue.removeFirst();
      process(tri, visitor);
    }
  }

}
