package com.revolsys.gis.model.geometry.operation.chain;

import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.gis.model.geometry.impl.BoundingBox;

/**
 * The action for the internal iterator for performing overlap queries on a
 * MonotoneChain
 * 
 * @version 1.7
 */
public class MonotoneChainOverlapAction {
  // these envelopes are used during the MonotoneChain search process
  BoundingBox tempEnv1 = new BoundingBox();

  BoundingBox tempEnv2 = new BoundingBox();

  protected LineSegment overlapSeg1 = new LineSegment();

  protected LineSegment overlapSeg2 = new LineSegment();

  /**
   * This function can be overridden if the original chains are needed
   * 
   * @param start1 the index of the start of the overlapping segment from mc1
   * @param start2 the index of the start of the overlapping segment from mc2
   */
  public void overlap(MonotoneChain mc1, int start1, MonotoneChain mc2,
    int start2) {
    mc1.getLineSegment(start1, overlapSeg1);
    mc2.getLineSegment(start2, overlapSeg2);
    overlap(overlapSeg1, overlapSeg2);
  }

  /**
   * This is a convenience function which can be overridden to obtain the actual
   * line segments which overlap
   * 
   * @param seg1
   * @param seg2
   */
  public void overlap(LineSegment seg1, LineSegment seg2) {
  }
}
