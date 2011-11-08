package com.revolsys.gis.algorithm.index;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Envelope;

public class PointQuadTreeNode<T> {
  private final double x;

  private final double y;

  private final T value;

  private PointQuadTreeNode<T> northWest;

  private PointQuadTreeNode<T> northEast;

  private PointQuadTreeNode<T> southWest;

  private PointQuadTreeNode<T> southEast;

  public PointQuadTreeNode(final T value, final double x, final double y) {
    this.value = value;
    this.x = x;
    this.y = y;
  }


  public void insert( final PointQuadTreeNode<T> node, final double x,
    final double y) {
    final boolean xLess = isLessThanX(x);
    final boolean yLess = isLessThanY(y);
    if (xLess && yLess) {
      if (southWest == null) {
        southWest = node;
      } else {
        southWest.insert(node, x, y);
      }
    } else if (xLess && !yLess) {
      if (northWest == null) {
        northWest = node;
      } else {
        northWest.insert(node, x, y);
      }
    } else if (!xLess && yLess) {
      if (southEast == null) {
        southEast = node;
      } else {
        southEast.insert(node, x, y);
      }
    } else if (!xLess && !yLess) {
      if (northEast == null) {
        northEast = node;
      } else {
        northEast.insert(node, x, y);
      }
    }
  }

  public PointQuadTreeNode<T> remove(final T value, final double x, final double y) {
    final boolean xLess = isLessThanX(x);
    final boolean yLess = isLessThanY(y);
    if (this.x == x && this.y == y && this.value == value) {
      List<PointQuadTreeNode<T>> nodes = new ArrayList<PointQuadTreeNode<T>>();
      if (northWest != null) {
        nodes.add(northWest);
      }
      if (northEast != null) {
        nodes.add(northEast);
      }
      if (southWest != null) {
        nodes.add(southWest);
      }
      if (southEast != null) {
        nodes.add(southEast);
      }
      if (nodes.isEmpty()) {
        return null;
      } else {
        PointQuadTreeNode<T> node = nodes.remove(0);
        for (PointQuadTreeNode<T> subNode : nodes) {
          node.insert(subNode, subNode.x, subNode.y);
        }
        return node;
      }
    } else if (xLess && yLess) {
      if (southWest != null) {
        southWest = southWest.remove(value, x, y);
      }
    } else if (xLess && !yLess) {
      if (northWest != null) {
        northWest = northWest.remove(value, x, y);
      }
    } else if (!xLess && yLess) {
      if (southEast != null) {
        southEast = southEast.remove(value, x, y);
      }
    } else if (!xLess && !yLess) {
      if (northEast != null) {
        northEast = northEast.remove(value, x, y);
      }
    }
    return this;
  }

  public boolean isLessThanX(final double x) {
    return x < this.x;
  }

  public boolean isLessThanY(final double y) {
    return y < this.y;
  }

  public void findWithin(final List<T> results, final Envelope envelope) {
    final double minX = envelope.getMinX();
    final double maxX = envelope.getMaxX();
    final double minY = envelope.getMinY();
    final double maxY = envelope.getMaxY();
    if (envelope.contains(x, y)) {
      results.add(value);
    }
    final boolean minXLess = isLessThanX(minX);
    final boolean maxXLess = isLessThanX(maxX);
    final boolean minYLess = isLessThanY(minY);
    final boolean maxYLess = isLessThanY(maxY);
    if (southWest != null && minXLess && minYLess) {
      southWest.findWithin(results, envelope);
    }
    if (northWest != null && minXLess && !maxYLess) {
      northWest.findWithin(results, envelope);
    }
    if (southEast != null && !maxXLess && minYLess) {
      southEast.findWithin(results, envelope);
    }
    if (northEast != null && !maxXLess && !maxYLess) {
      northEast.findWithin(results, envelope);
    }
  }

  public void findWithin(final List<T> results, double x, double y,
    double distance, final Envelope envelope) {
    final double minX = envelope.getMinX();
    final double maxX = envelope.getMaxX();
    final double minY = envelope.getMinY();
    final double maxY = envelope.getMaxY();
    if (MathUtil.distance(x, y, this.x, this.y) < distance) {
      results.add(value);
    }
    final boolean minXLess = isLessThanX(minX);
    final boolean maxXLess = isLessThanX(maxX);
    final boolean minYLess = isLessThanY(minY);
    final boolean maxYLess = isLessThanY(maxY);
    if (southWest != null && minXLess && minYLess) {
      southWest.findWithin(results, x, y, distance, envelope);
    }
    if (northWest != null && minXLess && !maxYLess) {
      northWest.findWithin(results, x, y, distance, envelope);
    }
    if (southEast != null && !maxXLess && minYLess) {
      southEast.findWithin(results, x, y, distance, envelope);
    }
    if (northEast != null && !maxXLess && !maxYLess) {
      northEast.findWithin(results, x, y, distance, envelope);
    }
  }

  public void setValue(final int index, final double value) {
    throw new UnsupportedOperationException(
      "Cannot change the coordinates on a quad tree");
  }

}
