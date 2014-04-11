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

package com.revolsys.jts.simplify;

import java.util.Iterator;
import java.util.List;

import com.revolsys.jts.algorithm.LineIntersector;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.LineSegment;

/**
 * Simplifies a TaggedLineString, preserving topology
 * (in the sense that no new intersections are introduced).
 * Uses the recursive Douglas-Peucker algorithm.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class TaggedLineStringSimplifier {
  /**
   * Tests whether a segment is in a section of a TaggedLineString
   * @param line
   * @param sectionIndex
   * @param seg
   * @return
   */
  private static boolean isInLineSection(final TaggedLineString line,
    final int[] sectionIndex, final TaggedLineSegment seg) {
    // not in this line
    if (seg.getParent() != line.getParent()) {
      return false;
    }
    final int segIndex = seg.getIndex();
    if (segIndex >= sectionIndex[0] && segIndex < sectionIndex[1]) {
      return true;
    }
    return false;
  }

  private final LineIntersector li = new RobustLineIntersector();

  private LineSegmentIndex inputIndex = new LineSegmentIndex();

  private LineSegmentIndex outputIndex = new LineSegmentIndex();

  private TaggedLineString line;

  private Coordinates[] linePts;

  private double distanceTolerance = 0.0;

  public TaggedLineStringSimplifier(final LineSegmentIndex inputIndex,
    final LineSegmentIndex outputIndex) {
    this.inputIndex = inputIndex;
    this.outputIndex = outputIndex;
  }

  private int findFurthestPoint(final Coordinates[] pts, final int i,
    final int j, final double[] maxDistance) {
    final LineSegment seg = new LineSegment();
    seg.p0 = pts[i];
    seg.p1 = pts[j];
    double maxDist = -1.0;
    int maxIndex = i;
    for (int k = i + 1; k < j; k++) {
      final Coordinates midPt = pts[k];
      final double distance = seg.distance(midPt);
      if (distance > maxDist) {
        maxDist = distance;
        maxIndex = k;
      }
    }
    maxDistance[0] = maxDist;
    return maxIndex;
  }

  /**
   * Flattens a section of the line between
   * indexes <code>start</code> and <code>end</code>,
   * replacing them with a line between the endpoints.
   * The input and output indexes are updated
   * to reflect this.
   * 
   * @param start the start index of the flattened section
   * @param end the end index of the flattened section
   * @return the new segment created
   */
  private LineSegment flatten(final int start, final int end) {
    // make a new segment for the simplified geometry
    final Coordinates p0 = linePts[start];
    final Coordinates p1 = linePts[end];
    final LineSegment newSeg = new LineSegment(p0, p1);
    // update the indexes
    remove(line, start, end);
    outputIndex.add(newSeg);
    return newSeg;
  }

  private boolean hasBadInputIntersection(final TaggedLineString parentLine,
    final int[] sectionIndex, final LineSegment candidateSeg) {
    final List querySegs = inputIndex.query(candidateSeg);
    for (final Iterator i = querySegs.iterator(); i.hasNext();) {
      final TaggedLineSegment querySeg = (TaggedLineSegment)i.next();
      if (hasInteriorIntersection(querySeg, candidateSeg)) {
        if (isInLineSection(parentLine, sectionIndex, querySeg)) {
          continue;
        }
        return true;
      }
    }
    return false;
  }

  private boolean hasBadIntersection(final TaggedLineString parentLine,
    final int[] sectionIndex, final LineSegment candidateSeg) {
    if (hasBadOutputIntersection(candidateSeg)) {
      return true;
    }
    if (hasBadInputIntersection(parentLine, sectionIndex, candidateSeg)) {
      return true;
    }
    return false;
  }

  private boolean hasBadOutputIntersection(final LineSegment candidateSeg) {
    final List querySegs = outputIndex.query(candidateSeg);
    for (final Iterator i = querySegs.iterator(); i.hasNext();) {
      final LineSegment querySeg = (LineSegment)i.next();
      if (hasInteriorIntersection(querySeg, candidateSeg)) {
        return true;
      }
    }
    return false;
  }

  private boolean hasInteriorIntersection(final LineSegment seg0,
    final LineSegment seg1) {
    li.computeIntersection(seg0.p0, seg0.p1, seg1.p0, seg1.p1);
    return li.isInteriorIntersection();
  }

  /**
   * Remove the segs in the section of the line
   * @param line
   * @param pts
   * @param sectionStartIndex
   * @param sectionEndIndex
   */
  private void remove(final TaggedLineString line, final int start,
    final int end) {
    for (int i = start; i < end; i++) {
      final TaggedLineSegment seg = line.getSegment(i);
      inputIndex.remove(seg);
    }
  }

  /**
   * Sets the distance tolerance for the simplification.
   * All vertices in the simplified geometry will be within this
   * distance of the original geometry.
   *
   * @param distanceTolerance the approximation tolerance to use
   */
  public void setDistanceTolerance(final double distanceTolerance) {
    this.distanceTolerance = distanceTolerance;
  }

  /**
   * Simplifies the given {@link TaggedLineString}
   * using the distance tolerance specified.
   * 
   * @param line the linestring to simplify
   */
  void simplify(final TaggedLineString line) {
    this.line = line;
    linePts = line.getParentCoordinates();
    simplifySection(0, linePts.length - 1, 0);
  }

  private void simplifySection(final int i, final int j, int depth) {
    depth += 1;
    final int[] sectionIndex = new int[2];
    if ((i + 1) == j) {
      final LineSegment newSeg = line.getSegment(i);
      line.addToResult(newSeg);
      // leave this segment in the input index, for efficiency
      return;
    }

    boolean isValidToSimplify = true;

    /**
     * Following logic ensures that there is enough points in the output line.
     * If there is already more points than the minimum, there's nothing to check.
     * Otherwise, if in the worst case there wouldn't be enough points,
     * don't flatten this segment (which avoids the worst case scenario)
     */
    if (line.getResultSize() < line.getMinimumSize()) {
      final int worstCaseSize = depth + 1;
      if (worstCaseSize < line.getMinimumSize()) {
        isValidToSimplify = false;
      }
    }

    final double[] distance = new double[1];
    final int furthestPtIndex = findFurthestPoint(linePts, i, j, distance);
    // flattening must be less than distanceTolerance
    if (distance[0] > distanceTolerance) {
      isValidToSimplify = false;
    }
    // test if flattened section would cause intersection
    final LineSegment candidateSeg = new LineSegment();
    candidateSeg.p0 = linePts[i];
    candidateSeg.p1 = linePts[j];
    sectionIndex[0] = i;
    sectionIndex[1] = j;
    if (hasBadIntersection(line, sectionIndex, candidateSeg)) {
      isValidToSimplify = false;
    }

    if (isValidToSimplify) {
      final LineSegment newSeg = flatten(i, j);
      line.addToResult(newSeg);
      return;
    }
    simplifySection(i, furthestPtIndex, depth);
    simplifySection(furthestPtIndex, j, depth);
  }
}
