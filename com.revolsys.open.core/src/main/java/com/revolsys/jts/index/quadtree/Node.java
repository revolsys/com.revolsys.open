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
package com.revolsys.jts.index.quadtree;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.util.Assert;

/**
 * Represents a node of a {@link Quadtree}.  Nodes contain
 * items which have a spatial extent corresponding to the node's position
 * in the quadtree.
 *
 * @version 1.7
 */
public class Node extends NodeBase {
  public static Node createExpanded(final Node node,
    final BoundingBox boundingBox) {
    BoundingBox newBoundingBox = boundingBox;
    if (node != null) {
      newBoundingBox = newBoundingBox.expandToInclude(node.boundingBox);
    }

    final Node largerNode = createNode(newBoundingBox);
    if (node != null) {
      largerNode.insertNode(node);
    }
    return largerNode;
  }

  public static Node createNode(final BoundingBox env) {
    final Key key = new Key(env);
    final Node node = new Node(key.getEnvelope(), key.getLevel());
    return node;
  }

  private final BoundingBox boundingBox;

  private final int level;

  public Node(final BoundingBox boundingBox, final int level) {
    this.boundingBox = boundingBox;
    this.level = level;
  }

  public boolean contains(final BoundingBox envelope) {
    return boundingBox.covers(envelope);
  }

  private Node createSubnode(final int index) {
    // create a new subquad in the appropriate quadrant

    double minX = 0.0;
    double maxX = 0.0;
    double minY = 0.0;
    double maxY = 0.0;

    switch (index) {
      case 0:
        minX = getMinX();
        maxX = getCentreX();
        minY = getMinY();
        maxY = getCentreY();
      break;
      case 1:
        minX = getCentreX();
        maxX = getMaxX();
        minY = getMinY();
        maxY = getCentreY();
      break;
      case 2:
        minX = getMinX();
        maxX = getCentreX();
        minY = getCentreY();
        maxY = getMaxY();
      break;
      case 3:
        minX = getCentreX();
        maxX = getMaxX();
        minY = getCentreY();
        maxY = getMaxY();
      break;
    }
    final BoundingBox newEnvelope = new Envelope(2, minX, minY, maxX, maxY);
    final Node node = new Node(newEnvelope, level - 1);
    return node;
  }

  /**
   * Returns the smallest <i>existing</i>
   * node containing the envelope.
   */
  public NodeBase find(final BoundingBox searchEnv) {
    final int subnodeIndex = getSubnodeIndex(searchEnv, getCentreX(),
      getCentreY());
    if (subnodeIndex == -1) {
      return this;
    }
    if (subnode[subnodeIndex] != null) {
      // query lies in subquad, so search it
      final Node node = subnode[subnodeIndex];
      return node.find(searchEnv);
    }
    // no existing subquad, so return this one anyway
    return this;
  }

  private double getCentreX() {
    return boundingBox.getCentreX();
  }

  private double getCentreY() {
    return boundingBox.getCentreY();
  }

  private double getMaxX() {
    return boundingBox.getMaxX();
  }

  private double getMaxY() {
    return boundingBox.getMaxY();
  }

  private double getMinX() {
    return boundingBox.getMinX();
  }

  private double getMinY() {
    return boundingBox.getMinY();
  }

  /**
   * Returns the subquad containing the envelope <tt>searchEnv</tt>.
   * Creates the subquad if
   * it does not already exist.
   * 
   * @return the subquad containing the search envelope
   */
  public Node getNode(final BoundingBox searchEnv) {
    final int subnodeIndex = getSubnodeIndex(searchEnv, getCentreX(),
      getCentreY());
    // if subquadIndex is -1 searchEnv is not contained in a subquad
    if (subnodeIndex != -1) {
      // create the quad if it does not exist
      final Node node = getSubnode(subnodeIndex);
      // recursively search the found/created quad
      return node.getNode(searchEnv);
    } else {
      return this;
    }
  }

  /**
   * get the subquad for the index.
   * If it doesn't exist, create it
   */
  private Node getSubnode(final int index) {
    if (subnode[index] == null) {
      subnode[index] = createSubnode(index);
    }
    return subnode[index];
  }

  void insertNode(final Node node) {
    Assert.isTrue(boundingBox == null || boundingBox.covers(node.boundingBox));
    // System.out.println(env);
    // System.out.println(quad.env);
    final int index = getSubnodeIndex(node.boundingBox, getCentreX(),
      getCentreY());
    // System.out.println(index);
    if (node.level == level - 1) {
      subnode[index] = node;
      // System.out.println("inserted");
    } else {
      // the quad is not a direct child, so make a new child quad to contain it
      // and recursively insert the quad
      final Node childNode = createSubnode(index);
      childNode.insertNode(node);
      subnode[index] = childNode;
    }
  }

  @Override
  protected boolean isSearchMatch(final BoundingBox searchEnv) {
    return boundingBox.intersects(searchEnv);
  }

}
