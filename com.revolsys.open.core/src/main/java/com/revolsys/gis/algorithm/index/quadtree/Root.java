package com.revolsys.gis.algorithm.index.quadtree;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.index.quadtree.IntervalSize;

public class Root<T> extends NodeBase<T> {
  private static final Coordinates origin = new DoubleCoordinates(0.0, 0.0);

  public Root() {
  }

  public void insert(final Envelope envelope, final T item) {
    final int index = getSubnodeIndex(envelope, origin);
    if (index == -1) {
      add(envelope, item);
    } else {
      final Node<T> node = getNode(index);
      if (node == null || !node.getEnvelope().contains(envelope)) {
        final Node<T> largerNode = Node.createExpanded(node, envelope);
        setNode(index, largerNode);
      }
      insertContained(getNode(index), envelope, item);
    }
  }

  private void insertContained(final Node<T> tree, final Envelope envelope,
    final T item) {
    final boolean isZeroX = IntervalSize.isZeroWidth(envelope.getMinX(),
      envelope.getMaxX());
    final boolean isZeroY = IntervalSize.isZeroWidth(envelope.getMinY(),
      envelope.getMaxY());
    NodeBase<T> node;
    if (isZeroX || isZeroY) {
      node = tree.find(envelope);
    } else {
      node = tree.getNode(envelope);
    }
    node.add(envelope, item);
  }

  @Override
  protected boolean isSearchMatch(final Envelope searchEnv) {
    return true;
  }

}
