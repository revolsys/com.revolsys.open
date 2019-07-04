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
package com.revolsys.geometry.noding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.geometry.index.SpatialIndex;
import com.revolsys.geometry.index.chain.MonotoneChain;
import com.revolsys.geometry.index.chain.MonotoneChainBuilder;
import com.revolsys.geometry.index.chain.MonotoneChainOverlapAction;
import com.revolsys.geometry.index.strtree.StrTree;

/**
 * Intersects two sets of {@link SegmentString}s using a index based
 * on {@link MonotoneChain}s and a {@link SpatialIndex}.
 *
 * Thread-safe and immutable.
 *
 * @version 1.7
 */
public class MCIndexSegmentSetMutualIntersector implements SegmentSetMutualIntersector {
  public class SegmentOverlapAction extends MonotoneChainOverlapAction {
    private SegmentIntersector si = null;

    public SegmentOverlapAction(final SegmentIntersector si) {
      this.si = si;
    }

    @Override
    public void overlap(final MonotoneChain mc1, final int start1, final MonotoneChain mc2,
      final int start2) {
      final SegmentString ss1 = (SegmentString)mc1.getContext();
      final SegmentString ss2 = (SegmentString)mc2.getContext();
      this.si.processIntersections(ss1, start1, ss2, start2);
    }

  }

  /**
   * The {@link SpatialIndex} used should be something that supports
   * envelope (range) queries efficiently (such as a
   *  {@link StrTree}.
   */
  private final StrTree index = new StrTree();

  /**
   * Constructs a new intersector for a given set of {@link SegmentStrings}.
   *
   * @param baseSegStrings the base segment strings to intersect
   */
  public MCIndexSegmentSetMutualIntersector(final Collection baseSegStrings) {
    initBaseSegments(baseSegStrings);
  }

  private void addToIndex(final SegmentString segStr) {
    final List segChains = MonotoneChainBuilder.getChains(segStr.getLineString(), segStr);
    for (final Iterator i = segChains.iterator(); i.hasNext();) {
      final MonotoneChain mc = (MonotoneChain)i.next();
      this.index.insertItem(mc.getEnvelope(), mc);
    }
  }

  private void addToMonoChains(final SegmentString segStr, final List monoChains) {
    final List segChains = MonotoneChainBuilder.getChains(segStr.getLineString(), segStr);
    for (final Iterator i = segChains.iterator(); i.hasNext();) {
      final MonotoneChain mc = (MonotoneChain)i.next();
      monoChains.add(mc);
    }
  }

  /**
   * Gets the index constructed over the base segment strings.
   *
   * NOTE: To retain thread-safety, treat returned value as immutable!
   *
   * @return the constructed index
   */
  public SpatialIndex getIndex() {
    return this.index;
  }

  private void initBaseSegments(final Collection segStrings) {
    for (final Iterator i = segStrings.iterator(); i.hasNext();) {
      addToIndex((SegmentString)i.next());
    }
    // build index to ensure thread-safety
    this.index.build();
  }

  private void intersectChains(final List monoChains, final SegmentIntersector segInt) {
    final MonotoneChainOverlapAction overlapAction = new SegmentOverlapAction(segInt);

    for (final Iterator i = monoChains.iterator(); i.hasNext();) {
      final MonotoneChain queryChain = (MonotoneChain)i.next();
      final List overlapChains = this.index.getItems(queryChain.getEnvelope());
      for (final Iterator j = overlapChains.iterator(); j.hasNext();) {
        final MonotoneChain testChain = (MonotoneChain)j.next();
        queryChain.computeOverlaps(testChain, overlapAction);
        if (segInt.isDone()) {
          return;
        }
      }
    }
  }

  /**
   * Calls {@link SegmentIntersector#processIntersections(SegmentString, int, SegmentString, int)}
   * for all <i>candidate</i> intersections between
   * the given collection of SegmentStrings and the set of indexed segments.
   *
   * @param a set of segments to intersect
   * @param the segment intersector to use
   */
  @Override
  public void process(final Collection segStrings, final SegmentIntersector segInt) {
    final List monoChains = new ArrayList();
    for (final Iterator i = segStrings.iterator(); i.hasNext();) {
      addToMonoChains((SegmentString)i.next(), monoChains);
    }
    intersectChains(monoChains, segInt);
    // System.out.println("MCIndexBichromaticIntersector: # chain overlaps = " +
    // nOverlaps);
    // System.out.println("MCIndexBichromaticIntersector: # oct chain overlaps =
    // "
    // + nOctOverlaps);
  }
}
