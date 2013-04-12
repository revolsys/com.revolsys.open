package com.revolsys.gis.model.geometry.operation.geomgraph.index;

/**
 * @version 1.7
 */
public class MonotoneChain {

  MonotoneChainEdge mce;

  int chainIndex;

  public MonotoneChain(final MonotoneChainEdge mce, final int chainIndex) {
    this.mce = mce;
    this.chainIndex = chainIndex;
  }

  public void computeIntersections(final MonotoneChain mc,
    final SegmentIntersector si) {
    this.mce.computeIntersectsForChain(chainIndex, mc.mce, mc.chainIndex, si);
  }
}
