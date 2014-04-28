/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.jts.noding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.jts.index.SpatialIndex;
import com.revolsys.jts.index.chain.MonotoneChain;
import com.revolsys.jts.index.chain.MonotoneChainBuilder;
import com.revolsys.jts.index.chain.MonotoneChainOverlapAction;
import com.revolsys.jts.index.strtree.STRtree;

/**
 * Nodes a set of {@link SegmentString}s using a index based
 * on {@link MonotoneChain}s and a {@link SpatialIndex}.
 * The {@link SpatialIndex} used should be something that supports
 * envelope (range) queries efficiently (such as a <code>Quadtree</code>}
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

  private final List<MonotoneChain> monoChains = new ArrayList<>();

  private final SpatialIndex index = new STRtree();

  private int idCounter = 0;

  private Collection nodedSegStrings;

  public MCIndexNoder() {
  }

  public MCIndexNoder(final SegmentIntersector si) {
    super(si);
  }

  private void add(final SegmentString segStr) {
    final List<MonotoneChain> segChains = MonotoneChainBuilder.getChains(
      segStr.getCoordinates(), segStr);
    for (final MonotoneChain mc : segChains) {
      mc.setId(idCounter++);
      index.insert(mc.getEnvelope(), mc);
      monoChains.add(mc);
    }
  }

  @Override
  public void computeNodes(final Collection<? extends SegmentString> segments) {
    this.nodedSegStrings = segments;
    for (final SegmentString segment : segments) {
      add(segment);
    }
    intersectChains();
    // System.out.println("MCIndexNoder: # chain overlaps = " + nOverlaps);
  }

  public SpatialIndex getIndex() {
    return index;
  }

  public List<MonotoneChain> getMonotoneChains() {
    return monoChains;
  }

  @Override
  public Collection getNodedSubstrings() {
    return NodedSegmentString.getNodedSubstrings(nodedSegStrings);
  }

  private void intersectChains() {
    final MonotoneChainOverlapAction overlapAction = new SegmentOverlapAction(
      segInt);

    for (final MonotoneChain queryChain : monoChains) {
      final List<MonotoneChain> overlapChains = index.query(queryChain.getEnvelope());
      for (final MonotoneChain testChain : overlapChains) {
        /**
         * following test makes sure we only compare each pair of chains once
         * and that we don't compare a chain to itself
         */
        if (testChain.getId() > queryChain.getId()) {
          queryChain.computeOverlaps(testChain, overlapAction);
        }
        // short-circuit if possible
        if (segInt.isDone()) {
          return;
        }
      }
    }
  }
}
