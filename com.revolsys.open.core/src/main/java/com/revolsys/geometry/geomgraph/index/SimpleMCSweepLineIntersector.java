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
package com.revolsys.geometry.geomgraph.index;

/**
 * @version 1.7
 */
import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.geomgraph.Edge;

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
public class SimpleMCSweepLineIntersector extends EdgeSetIntersector {

  private final List<SweepLineEvent<MonotoneChain>> events = new ArrayList<>();

  private boolean changed = true;

  /**
   * A SimpleMCSweepLineIntersector creates monotone chains from the edges
   * and compares them using a simple sweep-line along the x-axis.
   */
  public SimpleMCSweepLineIntersector() {
  }

  private void add(final Edge edge, final Object edgeSet) {
    final List<SweepLineEvent<MonotoneChain>> events = this.events;
    final MonotoneChainEdge chainEdge = edge.getMonotoneChainEdge();
    final int[] startIndex = chainEdge.getStartIndexes();
    for (int i = 0; i < startIndex.length - 1; i++) {
      final MonotoneChain chain = new MonotoneChain(chainEdge, i);

      double minX = edge.getX(startIndex[i]);
      double maxX = edge.getX(startIndex[i + 1]);
      if (minX > maxX) {
        final double t = minX;
        minX = maxX;
        maxX = t;
      }
      final SweepLineEvent<MonotoneChain> minEvent = new SweepLineEvent<>(edgeSet, minX, chain);
      events.add(minEvent);
      final SweepLineEvent<MonotoneChain> maxEvent = new SweepLineEvent<>(maxX, minEvent);
      events.add(maxEvent);
    }
    this.changed = true;
  }

  private void add(final List<Edge> edges) {
    for (final Edge edge : edges) {
      // edge is its own group
      add(edge, edge);
    }
  }

  private void add(final List<Edge> edges, final Object edgeSet) {
    for (final Edge edge : edges) {
      add(edge, edgeSet);
    }
  }

  @Override
  public void computeIntersections(final List<Edge> edges0, final List<Edge> edges1,
    final SegmentIntersector si) {
    add(edges0, edges0);
    add(edges1, edges1);
    computeIntersections(si);
  }

  @Override
  public void computeIntersections(final List<Edge> edges, final SegmentIntersector si,
    final boolean testAllSegments) {
    if (testAllSegments) {
      add(edges, null);
    } else {
      add(edges);
    }
    computeIntersections(si);
  }

  private void computeIntersections(final SegmentIntersector si) {
    prepareEvents();
    int i = 0;
    for (final SweepLineEvent<MonotoneChain> event : this.events) {
      if (event.isInsert()) {
        final int deleteEventIndex = event.getDeleteEventIndex();
        processOverlaps(i, deleteEventIndex, event, si);
      }
      i++;
    }
  }

  /**
   * Because Delete Events have a link to their corresponding Insert event,
   * it is possible to compute exactly the range of events which must be
   * compared to a given Insert event object.
   */
  private void prepareEvents() {
    if (this.changed) {
      this.changed = false;
      final List<SweepLineEvent<MonotoneChain>> events = this.events;
      events.sort(null);
      // set DELETE event indexes
      int i = 0;
      for (final SweepLineEvent<MonotoneChain> event : events) {
        if (event.isDelete()) {
          final SweepLineEvent<MonotoneChain> insertEvent = event.getInsertEvent();
          insertEvent.setDeleteEventIndex(i);
        }
        i++;
      }
    }
  }

  private void processOverlaps(final int start, final int end,
    final SweepLineEvent<MonotoneChain> event1, final SegmentIntersector intersector) {
    final MonotoneChain chain1 = event1.getObject();
    final int chain1Index = chain1.getChainIndex();
    final MonotoneChainEdge chain1Edge = chain1.getEdge();
    final List<SweepLineEvent<MonotoneChain>> events = this.events;
    /**
    * Since we might need to test for self-intersections,
    * include current INSERT event object in list of event objects to test.
    * Last index can be skipped, because it must be a Delete event.
    */
    for (int i = start; i < end; i++) {
      final SweepLineEvent<MonotoneChain> event = events.get(i);
      if (event.isInsert()) {
        final MonotoneChain chain2 = event.getObject();
        // don't compare edges in same group, if labels are present
        if (!event1.isSameLabel(event)) {
          final MonotoneChainEdge chain2Edge = chain2.getEdge();
          final int chain2Index = chain2.getChainIndex();
          chain1Edge.computeIntersectsForChain(chain1Index, chain2Edge, chain2Index, intersector);
        }
      }
    }
  }
}
