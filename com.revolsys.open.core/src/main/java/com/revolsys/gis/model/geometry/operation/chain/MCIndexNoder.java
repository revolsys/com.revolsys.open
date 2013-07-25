package com.revolsys.gis.model.geometry.operation.chain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.jts.JtsGeometryUtil;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * Nodes a set of {@link SegmentString}s using a index based
 * on {@link MonotoneChain}s and a {@link SpatialIndex}.
 * The {@link SpatialIndex} used should be something that supports
 * envelope (range) queries efficiently (such as a {@link LineSegmentQuadTree}
 * or {@link STRtree} (which is the default index provided).
 *
 * @version 1.7
 */
public class MCIndexNoder extends SinglePassNoder {
  public class SegmentOverlapAction extends MonotoneChainOverlapAction {
    private SegmentIntersector si = null;

    public SegmentOverlapAction(final SegmentIntersector si) {
      this.si = si;
    }

    @Override
    public void overlap(final MonotoneChain mc1, final int start1,
      final MonotoneChain mc2, final int start2) {
      final SegmentString ss1 = (SegmentString)mc1.getContext();
      final SegmentString ss2 = (SegmentString)mc2.getContext();
      si.processIntersections(ss1, start1, ss2, start2);
    }

  }

  private final List monoChains = new ArrayList();

  private final SpatialIndex index = new STRtree();

  private int idCounter = 0;

  private Collection nodedSegStrings;

  // statistics
  private int nOverlaps = 0;

  public MCIndexNoder() {
  }

  private void add(final SegmentString segStr) {
    final List segChains = MonotoneChainBuilder.getChains(
      segStr.getCoordinates(), segStr);
    for (final Iterator i = segChains.iterator(); i.hasNext();) {
      final MonotoneChain mc = (MonotoneChain)i.next();
      mc.setId(idCounter++);
      index.insert(JtsGeometryUtil.getEnvelope(mc.getBoundingBox()), mc);
      monoChains.add(mc);
    }
  }

  @Override
  public void computeNodes(final Collection inputSegStrings) {
    this.nodedSegStrings = inputSegStrings;
    for (final Iterator i = inputSegStrings.iterator(); i.hasNext();) {
      add((SegmentString)i.next());
    }
    intersectChains();
    // System.out.println("MCIndexNoder: # chain overlaps = " + nOverlaps);
  }

  public SpatialIndex getIndex() {
    return index;
  }

  public List getMonotoneChains() {
    return monoChains;
  }

  @Override
  public Collection getNodedSubstrings() {
    return NodedSegmentString.getNodedSubstrings(nodedSegStrings);
  }

  private void intersectChains() {
    final MonotoneChainOverlapAction overlapAction = new SegmentOverlapAction(
      segInt);

    for (final Iterator i = monoChains.iterator(); i.hasNext();) {
      final MonotoneChain queryChain = (MonotoneChain)i.next();
      final List overlapChains = index.query(JtsGeometryUtil.getEnvelope(queryChain.getBoundingBox()));
      for (final Iterator j = overlapChains.iterator(); j.hasNext();) {
        final MonotoneChain testChain = (MonotoneChain)j.next();
        /**
         * following test makes sure we only compare each pair of chains once
         * and that we don't compare a chain to itself
         */
        if (testChain.getId() > queryChain.getId()) {
          queryChain.computeOverlaps(testChain, overlapAction);
          nOverlaps++;
        }
        // short-circuit if possible
        if (segInt.isDone()) {
          return;
        }
      }
    }
  }
}
