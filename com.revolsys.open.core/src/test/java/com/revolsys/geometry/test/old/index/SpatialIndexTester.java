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
package com.revolsys.geometry.test.old.index;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.index.SpatialIndex;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;

/**
 * @version 1.7
 */
public class SpatialIndexTester {
  private static final double CELL_EXTENT = 20.31;

  private static final int CELLS_PER_GRID_SIDE = 10;

  private static final double FEATURE_EXTENT = 10.1;

  private static final double OFFSET = 5.03;

  private static final double QUERY_ENVELOPE_EXTENT_1 = 1.009;

  private static final double QUERY_ENVELOPE_EXTENT_2 = 11.7;

  private SpatialIndex<BoundingBox> index;

  private boolean isSuccess = true;

  private List<BoundingBox> sourceData;

  public SpatialIndexTester() {
  }

  private void addSourceData(final double offset, final List<BoundingBox> sourceData) {
    for (int i = 0; i < CELLS_PER_GRID_SIDE; i++) {
      final double minx = i * CELL_EXTENT + offset;
      final double maxx = minx + FEATURE_EXTENT;
      for (int j = 0; j < CELLS_PER_GRID_SIDE; j++) {
        final double miny = j * CELL_EXTENT + offset;
        final double maxy = miny + FEATURE_EXTENT;
        final BoundingBox e = new BoundingBoxDoubleXY(minx, miny, maxx, maxy);
        sourceData.add(e);
      }
    }
  }

  private void compare(final List<BoundingBox> expectedEnvelopes,
    final List<BoundingBox> actualEnvelopes) {
    // Don't use #containsAll because we want to check using
    // ==, not #equals. [Jon Aquino]
    for (final BoundingBox expected : expectedEnvelopes) {
      boolean found = false;
      for (final BoundingBox actual : actualEnvelopes) {
        if (actual.equals(expected)) {
          found = true;
          break;
        }
      }
      if (!found) {
        this.isSuccess = false;
      }
    }
  }

  private void doTest(final SpatialIndex<BoundingBox> index, final double queryEnvelopeExtent,
    final List<BoundingBox> sourceData) {
    int extraMatchCount = 0;
    int expectedMatchCount = 0;
    int actualMatchCount = 0;
    int queryCount = 0;
    for (int x = 0; x < CELL_EXTENT * CELLS_PER_GRID_SIDE; x += queryEnvelopeExtent) {
      for (int y = 0; y < CELL_EXTENT * CELLS_PER_GRID_SIDE; y += queryEnvelopeExtent) {
        final BoundingBox queryEnvelope = new BoundingBoxDoubleXY(x, y, x + queryEnvelopeExtent,
          y + queryEnvelopeExtent);
        final List<BoundingBox> expectedMatches = intersectingEnvelopes(queryEnvelope, sourceData);
        final List<BoundingBox> actualMatches = index.getItems(queryEnvelope);
        // since index returns candidates only, it may return more than the
        // expected value
        if (expectedMatches.size() > actualMatches.size()) {
          this.isSuccess = false;
        }
        extraMatchCount += actualMatches.size() - expectedMatches.size();
        expectedMatchCount += expectedMatches.size();
        actualMatchCount += actualMatches.size();
        compare(expectedMatches, actualMatches);
        queryCount++;
      }
    }
  }

  public SpatialIndex<BoundingBox> getSpatialIndex() {
    return this.index;
  }

  public void init() {
    this.sourceData = new ArrayList<>();
    addSourceData(0, this.sourceData);
    addSourceData(OFFSET, this.sourceData);
    insert(this.sourceData, this.index);
  }

  private void insert(final List<BoundingBox> sourceData, final SpatialIndex<BoundingBox> index) {
    for (final BoundingBox envelope : sourceData) {
      index.insertItem(envelope, envelope);
    }
  }

  private List<BoundingBox> intersectingEnvelopes(final BoundingBox queryEnvelope,
    final List<BoundingBox> envelopes) {
    final List<BoundingBox> intersectingEnvelopes = new ArrayList<>();
    for (final BoundingBox candidate : envelopes) {
      if (candidate.intersects(queryEnvelope)) {
        intersectingEnvelopes.add(candidate);
      }
    }
    return intersectingEnvelopes;
  }

  public boolean isSuccess() {
    return this.isSuccess;
  }

  public void run() {
    doTest(this.index, QUERY_ENVELOPE_EXTENT_1, this.sourceData);
    doTest(this.index, QUERY_ENVELOPE_EXTENT_2, this.sourceData);
  }

  public void setSpatialIndex(final SpatialIndex<BoundingBox> index) {
    this.index = index;
  }
}
