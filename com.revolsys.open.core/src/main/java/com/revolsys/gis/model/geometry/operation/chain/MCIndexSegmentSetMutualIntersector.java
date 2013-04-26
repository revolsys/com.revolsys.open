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
package com.revolsys.gis.model.geometry.operation.chain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.model.geometry.impl.BoundingBox;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * Intersects two sets of {@link SegmentString}s using a index based on
 * {@link MonotoneChain}s and a {@link SpatialIndex}.
 * 
 * @version 1.7
 */
public class MCIndexSegmentSetMutualIntersector extends
  SegmentSetMutualIntersector {
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

  private final List<MonotoneChain> monoChains = new ArrayList<MonotoneChain>();

  /*
   * The {@link SpatialIndex} used should be something that supports envelope
   * (range) queries efficiently (such as a {@link LineSegmentQuadTree} or {@link STRtree}.
   */
  private final SpatialIndex index = new STRtree();

  private int indexCounter = 0;

  private int processCounter = 0;

  // statistics
  private int nOverlaps = 0;

  public MCIndexSegmentSetMutualIntersector() {
  }

  private void addToIndex(final SegmentString segment) {
    final List<MonotoneChain> segChains = MonotoneChainBuilder.getChains(
      segment.getCoordinates(), segment);
    for (final MonotoneChain mc : segChains) {
      mc.setId(indexCounter++);
      index.insert(getEnvelope(mc.getBoundingBox()), mc);
    }
  }

  private void addToMonoChains(final SegmentString segStr) {
    final List segChains = MonotoneChainBuilder.getChains(
      segStr.getCoordinates(), segStr);
    for (final Iterator i = segChains.iterator(); i.hasNext();) {
      final MonotoneChain mc = (MonotoneChain)i.next();
      mc.setId(processCounter++);
      monoChains.add(mc);
    }
  }

  private Envelope getEnvelope(final BoundingBox boundingBox) {
    return new Envelope(boundingBox.getMinX(), boundingBox.getMaxX(),
      boundingBox.getMinY(), boundingBox.getMaxY());
  }

  public SpatialIndex getIndex() {
    return index;
  }

  public List<MonotoneChain> getMonotoneChains() {
    return monoChains;
  }

  private void intersectChains() {
    final MonotoneChainOverlapAction overlapAction = new SegmentOverlapAction(
      segInt);

    for (final Iterator i = monoChains.iterator(); i.hasNext();) {
      final MonotoneChain queryChain = (MonotoneChain)i.next();
      final List overlapChains = index.query(getEnvelope(queryChain.getBoundingBox()));
      for (final Iterator j = overlapChains.iterator(); j.hasNext();) {
        final MonotoneChain testChain = (MonotoneChain)j.next();
        queryChain.computeOverlaps(testChain, overlapAction);
        nOverlaps++;
        if (segInt.isDone()) {
          return;
        }
      }
    }
  }

  @Override
  public void process(final Collection segStrings) {
    processCounter = indexCounter + 1;
    nOverlaps = 0;
    monoChains.clear();
    for (final Iterator i = segStrings.iterator(); i.hasNext();) {
      addToMonoChains((SegmentString)i.next());
    }
    intersectChains();
    // System.out.println("MCIndexBichromaticIntersector: # chain overlaps = " +
    // nOverlaps);
    // System.out.println("MCIndexBichromaticIntersector: # oct chain overlaps = "
    // + nOctOverlaps);
  }

  @Override
  public void setBaseSegments(final Collection<SegmentString> segStrings) {
    for (final SegmentString segment : segStrings) {
      addToIndex(segment);
    }
  }
}
