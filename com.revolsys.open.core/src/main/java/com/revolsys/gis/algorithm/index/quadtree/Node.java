package com.revolsys.gis.algorithm.index.quadtree;

import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;

public class Node<T> extends NodeBase<T> {
  public static <V> Node<V> createExpanded(final Node<V> node,
    final BoundingBox addEnv) {
    final Envelope expandEnv = new Envelope(addEnv);
    if (node != null) {
      expandEnv.expandToInclude(node.env);
    }

    final Node<V> largerNode = createNode(expandEnv);
    if (node != null) {
      largerNode.insertNode(node);
    }
    return largerNode;
  }

  public static <V> Node<V> createNode(final BoundingBox env) {
    final Key key = new Key(env);
    final Node<V> node = new Node<V>(key.getEnvelope(), key.getLevel());
    return node;
  }

  private final BoundingBox env;

  private final Coordinates centre;

  private final int level;

  public Node(final BoundingBox env, final int level) {
    this.env = env;
    this.level = level;
    final double x = (env.getMinX() + env.getMaxX()) / 2;
    final double y = (env.getMinY() + env.getMaxY()) / 2;
    centre = new DoubleCoordinates(x, y);
  }

  private Node<T> createSubnode(final int index) {
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
    final Envelope envelope = new Envelope(minX, minY, maxX, maxY);
    final Node<T> node = new Node<T>(envelope, level - 1);
    return node;
  }

  public NodeBase<T> find(final BoundingBox searchEnv) {
    final int subnodeIndex = getSubnodeIndex(searchEnv, centre);
    if (subnodeIndex == -1) {
      return this;
    }
    if (getNode(subnodeIndex) != null) {
      final Node<T> node = getNode(subnodeIndex);
      return node.find(searchEnv);
    }
    return this;
  }

  public BoundingBox getEnvelope() {
    return env;
  }

  public Node<T> getNode(final BoundingBox searchEnv) {
    final int subnodeIndex = getSubnodeIndex(searchEnv, centre);
    if (subnodeIndex != -1) {
      final Node<T> node = getSubnode(subnodeIndex);
      return node.getNode(searchEnv);
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
    final int index = getSubnodeIndex(node.env, centre);
    if (node.level == level - 1) {
      setNode(index, node);
    } else {
      final Node<T> childNode = createSubnode(index);
      childNode.insertNode(node);
      setNode(index, childNode);
    }
  }

  @Override
  protected boolean isSearchMatch(final BoundingBox searchEnv) {
    return env.intersects(searchEnv);
  }

}
