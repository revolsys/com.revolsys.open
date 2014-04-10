package com.revolsys.gis.algorithm.index;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.util.MathUtil;

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

  public boolean contains(final Coordinates point) {
    if (point.equals(this.x, this.y)) {
      return true;
    }
    final boolean xLess = isLessThanX(point.getX());
    final boolean yLess = isLessThanY(point.getY());
    if (southWest != null && xLess && yLess) {
      if (southWest.contains(point)) {
        return true;
      }
    }
    if (northWest != null && xLess && !yLess) {
      if (northWest.contains(point)) {
        return true;
      }
    }
    if (southEast != null && !xLess && yLess) {
      if (southEast.contains(point)) {
        return true;
      }
    }
    if (northEast != null && !xLess && !yLess) {
      if (northEast.contains(point)) {
        return true;
      }
    }
    return false;
  }

  public void findEntriesWithin(final List<Entry<Coordinates, T>> results,
    final Envelope envelope) {
    final double minX = envelope.getMinX();
    final double maxX = envelope.getMaxX();
    final double minY = envelope.getMinY();
    final double maxY = envelope.getMaxY();
    if (envelope.contains(x, y)) {
      final DoubleCoordinates coordinates = new DoubleCoordinates(x, y);
      results.add(new SimpleImmutableEntry<Coordinates, T>(coordinates, value));
    }
    final boolean minXLess = isLessThanX(minX);
    final boolean maxXLess = isLessThanX(maxX);
    final boolean minYLess = isLessThanY(minY);
    final boolean maxYLess = isLessThanY(maxY);
    if (southWest != null && minXLess && minYLess) {
      southWest.findEntriesWithin(results, envelope);
    }
    if (northWest != null && minXLess && !maxYLess) {
      northWest.findEntriesWithin(results, envelope);
    }
    if (southEast != null && !maxXLess && minYLess) {
      southEast.findEntriesWithin(results, envelope);
    }
    if (northEast != null && !maxXLess && !maxYLess) {
      northEast.findEntriesWithin(results, envelope);
    }
  }

  public void findWithin(final List<T> results, final double x, final double y,
    final double maxDistance, final Envelope envelope) {
    final double minX = envelope.getMinX();
    final double maxX = envelope.getMaxX();
    final double minY = envelope.getMinY();
    final double maxY = envelope.getMaxY();
    final double distance = MathUtil.distance(x, y, this.x, this.y);
    if (distance < maxDistance) {
      results.add(value);
    }
    final boolean minXLess = isLessThanX(minX);
    final boolean maxXLess = isLessThanX(maxX);
    final boolean minYLess = isLessThanY(minY);
    final boolean maxYLess = isLessThanY(maxY);
    if (southWest != null && minXLess && minYLess) {
      southWest.findWithin(results, x, y, maxDistance, envelope);
    }
    if (northWest != null && minXLess && !maxYLess) {
      northWest.findWithin(results, x, y, maxDistance, envelope);
    }
    if (southEast != null && !maxXLess && minYLess) {
      southEast.findWithin(results, x, y, maxDistance, envelope);
    }
    if (northEast != null && !maxXLess && !maxYLess) {
      northEast.findWithin(results, x, y, maxDistance, envelope);
    }
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

  public boolean isLessThanX(final double x) {
    return x < this.x;
  }

  public boolean isLessThanY(final double y) {
    return y < this.y;
  }

  public void put(final double x, final double y,
    final PointQuadTreeNode<T> node) {
    final boolean xLess = isLessThanX(x);
    final boolean yLess = isLessThanY(y);
    if (xLess && yLess) {
      if (southWest == null) {
        southWest = node;
      } else {
        southWest.put(x, y, node);
      }
    } else if (xLess && !yLess) {
      if (northWest == null) {
        northWest = node;
      } else {
        northWest.put(x, y, node);
      }
    } else if (!xLess && yLess) {
      if (southEast == null) {
        southEast = node;
      } else {
        southEast.put(x, y, node);
      }
    } else if (!xLess && !yLess) {
      if (northEast == null) {
        northEast = node;
      } else {
        northEast.put(x, y, node);
      }
    }
  }

  public PointQuadTreeNode<T> remove(final double x, final double y,
    final T value) {
    final boolean xLess = isLessThanX(x);
    final boolean yLess = isLessThanY(y);
    if (this.x == x && this.y == y && this.value == value) {
      final List<PointQuadTreeNode<T>> nodes = new ArrayList<PointQuadTreeNode<T>>();
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
        final PointQuadTreeNode<T> node = nodes.remove(0);
        for (final PointQuadTreeNode<T> subNode : nodes) {
          node.put(subNode.x, subNode.y, subNode);
        }
        return node;
      }
    } else if (xLess && yLess) {
      if (southWest != null) {
        southWest = southWest.remove(x, y, value);
      }
    } else if (xLess && !yLess) {
      if (northWest != null) {
        northWest = northWest.remove(x, y, value);
      }
    } else if (!xLess && yLess) {
      if (southEast != null) {
        southEast = southEast.remove(x, y, value);
      }
    } else if (!xLess && !yLess) {
      if (northEast != null) {
        northEast = northEast.remove(x, y, value);
      }
    }
    return this;
  }

  public void setValue(final int index, final double value) {
    throw new UnsupportedOperationException(
      "Cannot change the coordinates on a quad tree");
  }

  public boolean visit(final Envelope envelope, final Visitor<T> visitor) {
    final double minX = envelope.getMinX();
    final double maxX = envelope.getMaxX();
    final double minY = envelope.getMinY();
    final double maxY = envelope.getMaxY();
    if (envelope.contains(x, y)) {
      if (!visitor.visit(value)) {
        return false;
      }
    }
    final boolean minXLess = isLessThanX(minX);
    final boolean maxXLess = isLessThanX(maxX);
    final boolean minYLess = isLessThanY(minY);
    final boolean maxYLess = isLessThanY(maxY);
    if (southWest != null && minXLess && minYLess) {
      if (!southWest.visit(envelope, visitor)) {
        return false;
      }
    }
    if (northWest != null && minXLess && !maxYLess) {
      if (!northWest.visit(envelope, visitor)) {
        return false;
      }
    }
    if (southEast != null && !maxXLess && minYLess) {
      if (!southEast.visit(envelope, visitor)) {
        return false;
      }
    }
    if (northEast != null && !maxXLess && !maxYLess) {
      if (!northEast.visit(envelope, visitor)) {
        return false;
      }
    }
    return true;
  }

  public boolean visit(final Visitor<T> visitor) {
    if (!visitor.visit(value)) {
      return false;
    }
    if (southWest != null) {
      if (!southWest.visit(visitor)) {
        return false;
      }
    }
    if (northWest != null) {
      if (!northWest.visit(visitor)) {
        return false;
      }
    }
    if (southEast != null) {
      if (!southEast.visit(visitor)) {
        return false;
      }
    }
    if (northEast != null) {
      if (!northEast.visit(visitor)) {
        return false;
      }
    }
    return true;
  }

}
