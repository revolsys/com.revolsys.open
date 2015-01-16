


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
package com.revolsys.jts.geomgraph.index;

/**
 * @version 1.7
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.revolsys.jts.geomgraph.Edge;

/**
 * Finds all intersections in one or two sets of edges,
 * using an x-axis sweepline algorithm in conjunction with Monotone Chains.
 * While still O(n^2) in the worst case, this algorithm
 * drastically improves the average-case time.
 * The use of MonotoneChains as the items in the index
 * seems to offer an improvement in performance over a sweep-line alone.
 *
 * @version 1.7
 */
public class SimpleMCSweepLineIntersector
extends EdgeSetIntersector
{

  List events = new ArrayList();
  // statistics information
  int nOverlaps;

  /**
   * A SimpleMCSweepLineIntersector creates monotone chains from the edges
   * and compares them using a simple sweep-line along the x-axis.
   */
  public SimpleMCSweepLineIntersector() {
  }

  private void add(final Edge edge, final Object edgeSet)
  {
    final MonotoneChainEdge mce = edge.getMonotoneChainEdge();
    final int[] startIndex = mce.getStartIndexes();
    for (int i = 0; i < startIndex.length - 1; i++) {
      final MonotoneChain mc = new MonotoneChain(mce, i);
      final SweepLineEvent insertEvent = new SweepLineEvent(edgeSet, mce.getMinX(i), mc);
      this.events.add(insertEvent);
      this.events.add(new SweepLineEvent(mce.getMaxX(i), insertEvent));
    }
  }

  private void add(final List edges)
  {
    for (final Iterator i = edges.iterator(); i.hasNext(); ) {
      final Edge edge = (Edge) i.next();
      // edge is its own group
      add(edge, edge);
    }
  }

  private void add(final List edges, final Object edgeSet)
  {
    for (final Iterator i = edges.iterator(); i.hasNext(); ) {
      final Edge edge = (Edge) i.next();
      add(edge, edgeSet);
    }
  }
  @Override
  public void computeIntersections(final List edges0, final List edges1, final SegmentIntersector si)
  {
    add(edges0, edges0);
    add(edges1, edges1);
    computeIntersections(si);
  }

  @Override
  public void computeIntersections(final List edges, final SegmentIntersector si, final boolean testAllSegments)
  {
    if (testAllSegments) {
      add(edges, null);
    } else {
      add(edges);
    }
    computeIntersections(si);
  }

  private void computeIntersections(final SegmentIntersector si)
  {
    this.nOverlaps = 0;
    prepareEvents();

    for (int i = 0; i < this.events.size(); i++ )
    {
      final SweepLineEvent ev = (SweepLineEvent) this.events.get(i);
      if (ev.isInsert()) {
        processOverlaps(i, ev.getDeleteEventIndex(), ev, si);
      }
    }
  }

  /**
   * Because Delete Events have a link to their corresponding Insert event,
   * it is possible to compute exactly the range of events which must be
   * compared to a given Insert event object.
   */
  private void prepareEvents()
  {
    Collections.sort(this.events);
    // set DELETE event indexes
    for (int i = 0; i < this.events.size(); i++ )
    {
      final SweepLineEvent ev = (SweepLineEvent) this.events.get(i);
      if (ev.isDelete()) {
        ev.getInsertEvent().setDeleteEventIndex(i);
      }
    }
  }

  private void processOverlaps(final int start, final int end, final SweepLineEvent ev0, final SegmentIntersector si)
  {
    final MonotoneChain mc0 = (MonotoneChain) ev0.getObject();
    /**
     * Since we might need to test for self-intersections,
     * include current INSERT event object in list of event objects to test.
     * Last index can be skipped, because it must be a Delete event.
     */
    for (int i = start; i < end; i++ ) {
      final SweepLineEvent ev1 = (SweepLineEvent) this.events.get(i);
      if (ev1.isInsert()) {
        final MonotoneChain mc1 = (MonotoneChain) ev1.getObject();
        // don't compare edges in same group, if labels are present
        if (! ev0.isSameLabel(ev1)) {
          mc0.computeIntersections(mc1, si);
          this.nOverlaps++;
        }
      }
    }
  }
}
