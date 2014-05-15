package com.revolsys.gis.algorithm.index.quadtree.linesegment;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.index.IntervalSize;

public class Root extends NodeBase {
  private static final Point origin = new PointDouble(0.0, 0.0);

  public Root() {
  }

  public void insert(final BoundingBox envelope, final int[] item) {
    final int index = getSubnodeIndex(envelope, origin);
    if (index == -1) {
      add(envelope, item);
    } else {
      final Node node = getNode(index);
      if (node == null || !node.getEnvelope().covers(envelope)) {
        final Node largerNode = Node.createExpanded(node, envelope);
        setNode(index, largerNode);
      }
      insertContained(getNode(index), envelope, item);
    }
  }

  private void insertContained(final Node tree, final BoundingBox envelope,
    final int[] item) {
    final boolean isZeroX = IntervalSize.isZeroWidth(envelope.getMinX(),
      envelope.getMaxX());
    final boolean isZeroY = IntervalSize.isZeroWidth(envelope.getMinY(),
      envelope.getMaxY());
    NodeBase node;
    if (isZeroX || isZeroY) {
      node = tree.find(envelope);
    } else {
      node = tree.getNode(envelope);
    }
    node.add(envelope, item);
  }

  @Override
  protected boolean isSearchMatch(final BoundingBox searchEnv) {
    return true;
  }

}
