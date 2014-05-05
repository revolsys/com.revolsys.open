package com.revolsys.gis.algorithm.index.quadtree.linesegment;

import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.index.quadtree.DoubleBits;

public class Node extends NodeBase {

  public static Envelope computeKey(final int level, final BoundingBox itemEnv) {
    final double quadSize = DoubleBits.powerOf2(level);
    final double x = Math.floor(itemEnv.getMinX() / quadSize) * quadSize;
    final double y = Math.floor(itemEnv.getMinY() / quadSize) * quadSize;
    return new Envelope(2, x, y, x + quadSize, y + quadSize);
  }

  public static int computeQuadLevel(final BoundingBox env) {
    final double dx = env.getWidth();
    final double dy = env.getHeight();
    final double dMax = dx > dy ? dx : dy;
    final int level = DoubleBits.exponent(dMax) + 1;
    return level;
  }

  public static Node createExpanded(final Node node, final Envelope addEnv) {
    BoundingBox expandEnv = addEnv;
    if (node != null) {
      expandEnv = expandEnv.expandToInclude(node.env);
    }

    final Node largerNode = createNode(expandEnv);
    if (node != null) {
      largerNode.insertNode(node);
    }
    return largerNode;
  }

  public static Node createNode(final BoundingBox itemEnv) {
    int level = computeQuadLevel(itemEnv);
    Envelope nodeEnvelope = computeKey(level, itemEnv);
    // MD - would be nice to have a non-iterative form of this algorithm
    while (!nodeEnvelope.covers(itemEnv)) {
      level += 1;
      nodeEnvelope = computeKey(level, itemEnv);
    }

    final Node node = new Node(nodeEnvelope, level);
    return node;
  }

  private final BoundingBox env;

  private final Coordinates centre;

  private final int level;

  public Node(final Envelope env, final int level) {
    this.env = env;
    this.level = level;
    final double x = (env.getMinX() + env.getMaxX()) / 2;
    final double y = (env.getMinY() + env.getMaxY()) / 2;
    centre = new DoubleCoordinates(x, y);
  }

  private Node createSubnode(final int index) {
    double minX = 0.0;
    double maxX = 0.0;
    double minY = 0.0;
    double maxY = 0.0;

    switch (index) {
      case 0:
        minX = env.getMinX();
        maxX = centre.getX();
        minY = env.getMinY();
        maxY = centre.getY();
      break;
      case 1:
        minX = centre.getX();
        maxX = env.getMaxX();
        minY = env.getMinY();
        maxY = centre.getY();
      break;
      case 2:
        minX = env.getMinX();
        maxX = centre.getX();
        minY = centre.getY();
        maxY = env.getMaxY();
      break;
      case 3:
        minX = centre.getX();
        maxX = env.getMaxX();
        minY = centre.getY();
        maxY = env.getMaxY();
      break;
    }
    final Envelope envelope = new Envelope(2, minX, minY, maxX, maxY);
    final Node node = new Node(envelope, level - 1);
    return node;
  }

  public NodeBase find(final BoundingBox searchEnv) {
    final int subnodeIndex = getSubnodeIndex(searchEnv, centre);
    if (subnodeIndex == -1) {
      return this;
    }
    if (getNode(subnodeIndex) != null) {
      final Node node = getNode(subnodeIndex);
      return node.find(searchEnv);
    }
    return this;
  }

  public BoundingBox getEnvelope() {
    return env;
  }

  public Node getNode(final BoundingBox searchEnv) {
    final int subnodeIndex = getSubnodeIndex(searchEnv, centre);
    if (subnodeIndex != -1) {
      final Node node = getSubnode(subnodeIndex);
      return node.getNode(searchEnv);
    } else {
      return this;
    }
  }

  private Node getSubnode(final int index) {
    if (getNode(index) == null) {
      setNode(index, createSubnode(index));
    }
    return getNode(index);
  }

  void insertNode(final Node node) {
    final int index = getSubnodeIndex(node.env, centre);
    if (node.level == level - 1) {
      setNode(index, node);
    } else {
      final Node childNode = createSubnode(index);
      childNode.insertNode(node);
      setNode(index, childNode);
    }
  }

  @Override
  protected boolean isSearchMatch(final BoundingBox searchEnv) {
    return env.intersects(searchEnv);
  }

}
