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

package com.revolsys.geometry.triangulate;

import java.util.Collection;

import com.revolsys.geometry.model.Side;
import com.revolsys.geometry.triangulate.quadedge.LocateFailureException;
import com.revolsys.geometry.triangulate.quadedge.QuadEdge;
import com.revolsys.geometry.triangulate.quadedge.QuadEdgeSubdivision;
import com.revolsys.geometry.triangulate.quadedge.QuadEdgeVertex;

/**
 * Computes a Delauanay Triangulation of a set of {@link QuadEdgeVertex}es, using an
 * incrementatal insertion algorithm.
 *
 * @author Martin Davis
 * @version 1.0
 */
public class IncrementalDelaunayTriangulator {

  private final QuadEdgeSubdivision subdiv;

  /**
   * Creates a new triangulator using the given {@link QuadEdgeSubdivision}.
   * The triangulator uses the tolerance of the supplied subdivision.
   *
   * @param subdiv
   *          a subdivision in which to build the TIN
   */
  public IncrementalDelaunayTriangulator(final QuadEdgeSubdivision subdiv) {
    this.subdiv = subdiv;
  }

  /**
   * Inserts a new point into a subdivision representing a Delaunay
   * triangulation, and fixes the affected edges so that the result is still a
   * Delaunay triangulation.
   * <p>
   *
   * @return a quadedge containing the inserted vertex
   */
  public QuadEdge insertSite(final QuadEdgeVertex vertex) {
    final double x = vertex.getX();
    final double y = vertex.getY();
    /**
     * This code is based on Guibas and Stolfi (1985), with minor modifications
     * and a bug fix from Dani Lischinski (Graphic Gems 1993). (The modification
     * I believe is the test for the inserted site falling exactly on an
     * existing edge. Without this test zero-width triangles have been observed
     * to be created)
     */
    QuadEdge edge = this.subdiv.locate(x, y);

    if (edge.equalsVertex(0, x, y) || edge.equalsVertex(1, x, y)) {
      // point is already in subdivision.
      return edge;
    } else if (edge.isOn(x, y)) {
      // the point lies exactly on an edge, so delete the edge
      // (it will be replaced by a pair of edges which have the point as a
      // vertex)
      edge = edge.oPrev();
      this.subdiv.delete(edge.getFromNextEdge());
    }

    /**
     * Connect the new point to the vertices of the containing triangle
     * (or quadrilateral, if the new point fell on an existing edge.)
     */
    QuadEdge base = this.subdiv.makeEdge(edge.getFromPoint(), vertex);
    QuadEdge.splice(base, edge);
    final QuadEdge startEdge = base;
    do {
      base = this.subdiv.connect(edge, base.sym());
      edge = base.oPrev();
    } while (edge.lNext() != startEdge);

    // Examine suspect edges to ensure that the Delaunay condition
    // is satisfied.
    do {
      final QuadEdge previousEdge = edge.oPrev();
      final QuadEdgeVertex previousToPoint = previousEdge.getToPoint();
      final double previousX = previousToPoint.getX();
      final double previousY = previousToPoint.getY();
      if (edge.getSide(previousX, previousY) == Side.RIGHT
        && vertex.isInCircle(edge.getFromPoint(), previousToPoint, edge.getToPoint())) {
        QuadEdge.swap(edge);
        edge = edge.oPrev();
      } else if (edge.getFromNextEdge() == startEdge) {
        return base; // no more suspect edges.
      } else {
        edge = edge.getFromNextEdge().lPrev();
      }
    } while (true);
  }

  /**
   * Inserts all sites in a collection. The inserted vertices <b>MUST</b> be
   * unique up to the provided tolerance value. (i.e. no two vertices should be
   * closer than the provided tolerance value). They do not have to be rounded
   * to the tolerance grid, however.
   *
   * @param vertices a Collection of QuadEdgeVertex
   *
   * @throws LocateFailureException if the location algorithm fails to converge in a reasonable number of iterations
   */
  public void insertSites(final Collection<QuadEdgeVertex> vertices) {
    double lastX = Double.NaN;
    double lastY = Double.NaN;
    for (final QuadEdgeVertex vertex : vertices) {
      final double x = vertex.getX();
      final double y = vertex.getY();
      if (x != lastX || y != lastY) {
        insertSite(vertex);
      }
      lastX = x;
      lastY = y;

    }
  }

}
