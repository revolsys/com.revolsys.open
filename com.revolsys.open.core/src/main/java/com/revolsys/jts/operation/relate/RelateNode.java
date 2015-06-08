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
package com.revolsys.jts.operation.relate;

/**
 * A RelateNode is a Node that maintains a list of EdgeStubs
 * for the edges that are incident on it.
 *
 * @version 1.7
 */

import com.revolsys.jts.geom.IntersectionMatrix;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geomgraph.EdgeEndStar;
import com.revolsys.jts.geomgraph.Node;

/**
 * Represents a node in the topological graph used to compute spatial relationships.
 *
 * @version 1.7
 */
public class RelateNode extends Node {

  public RelateNode(final Point coord, final EdgeEndStar edges) {
    super(coord, edges);
  }

  /**
   * Update the IM with the contribution for this component.
   * A component only contributes if it has a labelling for both parent geometries
   */
  @Override
  protected void computeIM(final IntersectionMatrix im) {
    im.setAtLeastIfValid(this.label.getLocation(0), this.label.getLocation(1), 0);
  }

  /**
   * Update the IM with the contribution for the EdgeEnds incident on this node.
   */
  void updateIMFromEdges(final IntersectionMatrix im) {
    ((EdgeEndBundleStar)this.edges).updateIM(im);
  }

}
