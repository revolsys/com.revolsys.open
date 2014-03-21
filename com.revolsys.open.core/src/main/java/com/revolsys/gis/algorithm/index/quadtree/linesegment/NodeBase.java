package com.revolsys.gis.algorithm.index.quadtree.linesegment;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.geometry.LineSegment;
import com.vividsolutions.jts.geom.Envelope;

public abstract class NodeBase {
  public static int getSubnodeIndex(final Envelope envelope,
    final Coordinates centre) {
    int subnodeIndex = -1;
    final double minX = envelope.getMinX();
    final double minY = envelope.getMinY();
    final double maxX = envelope.getMaxX();
    final double maxY = envelope.getMaxY();

    final double centreX = centre.getX();
    final double centreY = centre.getY();

    if (minX >= centreX) {
      if (minY >= centreY) {
        subnodeIndex = 3;
      }
      if (maxY <= centreY) {
        subnodeIndex = 1;
      }
    }
    if (maxX <= centreX) {
      if (minY >= centreY) {
        subnodeIndex = 2;
      }
      if (maxY <= centreY) {
        subnodeIndex = 0;
      }
    }
    return subnodeIndex;
  }

  private final List<int[]> segmentIndexes = new ArrayList<int[]>();

  private final List<Node> nodes = new ArrayList<Node>(4);

  public NodeBase() {
    for (int i = 0; i < 4; i++) {
      nodes.add(null);
    }
  }

  public void add(final Envelope envelope, final int[] item) {
    for (int i = 0; i < segmentIndexes.size(); i++) {
      final int[] oldItem = segmentIndexes.get(i);
      if (oldItem == item) {
        return;
      }
    }
    segmentIndexes.add(item);
  }

  public int depth() {
    int depth = 0;
    for (int i = 0; i < 4; i++) {
      final Node node = getNode(i);
      if (node != null) {
        final int nodeDepth = node.depth();
        if (nodeDepth > depth) {
          depth = nodeDepth;
        }
      }
    }
    return depth + 1;
  }

  protected Node getNode(final int i) {
    return nodes.get(i);
  }

  protected int getNodeCount() {
    int nodeCount = 0;
    for (int i = 0; i < 4; i++) {
      final Node node = getNode(i);
      if (node != null) {
        nodeCount += node.size();
      }
    }
    return nodeCount + 1;
  }

  public boolean hasChildren() {
    for (int i = 0; i < 4; i++) {
      if (getNode(i) != null) {
        return true;
      }
    }
    return false;
  }

  public boolean hasItems() {
    return !segmentIndexes.isEmpty();
  }

  public boolean isEmpty() {
    boolean isEmpty = true;
    if (!segmentIndexes.isEmpty()) {
      isEmpty = false;
    }
    for (int i = 0; i < 4; i++) {
      final Node node = getNode(i);
      if (node != null) {
        if (!node.isEmpty()) {
          isEmpty = false;
        }
      }
    }
    return isEmpty;
  }

  public boolean isPrunable() {
    return !(hasChildren() || hasItems());
  }

  protected abstract boolean isSearchMatch(Envelope searchEnv);

  protected void setNode(final int i, final Node node) {
    nodes.set(i, node);
  }

  protected int size() {
    int subSize = 0;
    for (int i = 0; i < 4; i++) {
      final Node node = getNode(i);
      if (node != null) {
        subSize += node.size();
      }
    }
    return subSize + segmentIndexes.size();
  }

  @Override
  public String toString() {
    return nodes + "=" + segmentIndexes.size();
  }

  public boolean visit(final LineSegmentQuadTree tree,
    final BoundingBox boundingBox, final Visitor<LineSegment> visitor) {
    if (isSearchMatch(boundingBox)) {
      for (final int[] segmentIndex : segmentIndexes) {
        final LineSegment segment = tree.getLineSegment(segmentIndex);
        if (segment.intersects(boundingBox)) {
          if (!visitor.visit(segment)) {
            return false;
          }
        }
      }

      for (int i = 0; i < 4; i++) {
        final Node node = getNode(i);
        if (node != null) {
          if (!node.visit(tree, boundingBox, visitor)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  public boolean visit(final LineSegmentQuadTree tree,
    final Visitor<LineSegment> visitor) {
    for (final int[] segmentIndex : segmentIndexes) {
      final LineSegment segment = tree.getLineSegment(segmentIndex);
      if (!visitor.visit(segment)) {
        return false;
      }
    }

    for (int i = 0; i < 4; i++) {
      final Node node = getNode(i);
      if (node != null) {
        if (!node.visit(tree, visitor)) {
          return false;
        }
      }
    }
    return true;
  }
}
