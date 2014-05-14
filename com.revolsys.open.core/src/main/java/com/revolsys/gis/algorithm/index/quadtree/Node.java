package com.revolsys.gis.algorithm.index.quadtree;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.index.DoubleBits;
import com.revolsys.jts.util.EnvelopeUtil;

public class Node<T> extends NodeBase<T> {
  public static int computeQuadLevel(final double... bounds) {
    final double dx = bounds[2] - bounds[0];
    final double dy = bounds[3] - bounds[1];
    final double dMax = dx > dy ? dx : dy;
    final int level = DoubleBits.exponent(dMax) + 1;
    return level;
  }

  public static <V> Node<V> createExpanded(final Node<V> node,
    final BoundingBox addEnv) {
    final double[] bounds = new double[] {
      addEnv.getMinX(), addEnv.getMinY(), addEnv.getMaxX(), addEnv.getMaxY()
    };
    if (node != null) {
      EnvelopeUtil.expand(bounds, 2, 0, node.minX);
      EnvelopeUtil.expand(bounds, 2, 0, node.maxX);
      EnvelopeUtil.expand(bounds, 2, 1, node.minY);
      EnvelopeUtil.expand(bounds, 2, 1, node.maxY);
    }

    final Node<V> largerNode = createNode(bounds);
    if (node != null) {
      largerNode.insertNode(node);
    }
    return largerNode;
  }

  private static <V> Node<V> createNode(final double[] bounds) {
    final double[] newBounds = new double[4];
    final double minX = bounds[0];
    final double minY = bounds[1];
    final double maxX = bounds[2];
    final double maxY = bounds[3];
    int level = computeQuadLevel(bounds);
    setBounds(minX, minY, newBounds, level);
    while (!EnvelopeUtil.covers(newBounds[0], newBounds[1], newBounds[2],
      newBounds[3], minX, minY, maxX, maxY)) {
      level++;
      setBounds(minX, minY, newBounds, level);
    }

    final Node<V> node = new Node<V>(level, newBounds);
    return node;
  }

  private static void setBounds(final double minX, final double minY,
    final double[] newBounds, final int level) {
    final double quadSize = DoubleBits.powerOf2(level);
    final double x1 = Math.floor(minX / quadSize) * quadSize;
    final double y1 = Math.floor(minY / quadSize) * quadSize;
    final double x2 = x1 + quadSize;
    final double y2 = y1 + quadSize;
    newBounds[0] = x1;
    newBounds[1] = y1;
    newBounds[2] = x2;
    newBounds[3] = y2;
  }

  private final double minX;

  private final double minY;

  private final double maxX;

  private final double maxY;

  private final int level;

  public Node(final int level, final double... bounds) {
    this.level = level;
    this.minX = bounds[0];
    this.minY = bounds[1];
    this.maxX = bounds[2];
    this.maxY = bounds[3];
  }

  private Node<T> createSubnode(final int index) {
    // create a new subquad in the appropriate quadrant

    double minX = 0.0;
    double maxX = 0.0;
    double minY = 0.0;
    double maxY = 0.0;

    final double centreX = getCentreX();
    final double centreY = getCentreY();
    switch (index) {
      case 0:
        minX = this.minX;
        maxX = centreX;
        minY = this.minY;
        maxY = centreY;
      break;
      case 1:
        minX = centreX;
        maxX = this.maxX;
        minY = this.minY;
        maxY = centreY;
      break;
      case 2:
        minX = this.minX;
        maxX = centreX;
        minY = centreY;
        maxY = this.maxY;
      break;
      case 3:
        minX = centreX;
        maxX = this.maxX;
        minY = centreY;
        maxY = this.maxY;
      break;
    }
    final Node<T> node = new Node<T>(level - 1, minX, minY, maxX, maxY);
    return node;
  }

  public NodeBase<T> find(final BoundingBox boundingBox) {
    final int subnodeIndex = getSubnodeIndex(boundingBox.getMinX(),
      boundingBox.getMinY(), boundingBox.getMaxX(), boundingBox.getMaxY(),
      getCentreX(), getCentreY());
    if (subnodeIndex == -1) {
      return this;
    }
    if (getNode(subnodeIndex) != null) {
      final Node<T> node = getNode(subnodeIndex);
      return node.find(boundingBox);
    }
    return this;
  }

  private double getCentreX() {
    return (minX + maxX) / 2;
  }

  private double getCentreY() {
    return (minY + maxY) / 2;
  }

  public BoundingBox getEnvelope() {
    return new Envelope(2, minX, minY, maxX, maxY);
  }

  public Node<T> getNode(final BoundingBox boundingBox) {
    final int subnodeIndex = getSubnodeIndex(boundingBox.getMinX(),
      boundingBox.getMinY(), boundingBox.getMaxX(), boundingBox.getMaxY(),
      getCentreX(), getCentreY());
    if (subnodeIndex != -1) {
      final Node<T> node = getSubnode(subnodeIndex);
      return node.getNode(boundingBox);
    } else {
      return this;
    }
  }

  private Node<T> getSubnode(final int index) {
    if (getNode(index) == null) {
      setNode(index, createSubnode(index));
    }
    return getNode(index);
  }

  void insertNode(final Node<T> node) {
    final double centreX = getCentreX();
    final double centreY = getCentreY();
    final int index = getSubnodeIndex(node.minX, node.minY, node.maxX,
      node.maxY, centreX, centreY);
    if (node.level == level - 1) {
      setNode(index, node);
    } else {
      final Node<T> childNode = createSubnode(index);
      childNode.insertNode(node);
      setNode(index, childNode);
    }
  }

  @Override
  protected boolean isSearchMatch(final BoundingBox boundingBox) {
    if (boundingBox.isEmpty()) {
      return false;
    } else {
      return EnvelopeUtil.intersects(this.minX, this.minY, this.maxX,
        this.maxY, boundingBox.getMinX(), boundingBox.getMinY(),
        boundingBox.getMaxX(), boundingBox.getMaxY());
    }
  }

}
