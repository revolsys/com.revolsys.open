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

import com.revolsys.jts.algorithm.LineIntersector;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.GeometryFactory;

/**
 * Represents a list of contiguous line segments,
 * and supports noding the segments.
 * The line segments are represented by an array of {@link Coordinates}s.
 * Intended to optimize the noding of contiguous segments by
 * reducing the number of allocated objects.
 * SegmentStrings can carry a context object, which is useful
 * for preserving topological or parentage information.
 * All noded substrings are initialized with the same context object.
 *
 * @version 1.7
 */
public class NodedSegmentString implements NodableSegmentString {
  /**
   * Gets the {@link SegmentString}s which result from splitting this string at node points.
   * 
   * @param segStrings a Collection of NodedSegmentStrings
   * @return a Collection of NodedSegmentStrings representing the substrings
   */
  public static List<NodedSegmentString> getNodedSubstrings(
    final Collection<NodedSegmentString> segStrings) {
    final List<NodedSegmentString> resultEdgelist = new ArrayList<>();
    getNodedSubstrings(segStrings, resultEdgelist);
    return resultEdgelist;
  }

  /**
   * Adds the noded {@link SegmentString}s which result from splitting this string at node points.
   * 
   * @param segStrings a Collection of NodedSegmentStrings
   * @param resultEdgelist a List which will collect the NodedSegmentStrings representing the substrings
   */
  public static void getNodedSubstrings(
    final Collection<NodedSegmentString> segStrings,
    final Collection<NodedSegmentString> resultEdgelist) {
    for (final NodedSegmentString ss : segStrings) {
      ss.getNodeList().addSplitEdges(resultEdgelist);
    }
  }

  private final SegmentNodeList nodeList = new SegmentNodeList(this);

  private final Coordinates[] points;

  private Object data;

  /**
   * Creates a new segment string from a list of vertices.
   *
   * @param points the vertices of the segment string
   * @param data the user-defined data of this segment string (may be null)
   */
  public NodedSegmentString(final Coordinates[] pts, final Object data) {
    this.points = pts;
    this.data = data;
  }

  /**
   * Adds an intersection node for a given point and segment to this segment string.
   * 
   * @param intPt the location of the intersection
   * @param segmentIndex the index of the segment containing the intersection
   */
  @Override
  public void addIntersection(final Coordinates intPt, final int segmentIndex) {
    addIntersectionNode(intPt, segmentIndex);
  }

  /**
   * Add an SegmentNode for intersection intIndex.
   * An intersection that falls exactly on a vertex
   * of the SegmentString is normalized
   * to use the higher of the two possible segmentIndexes
   */
  public void addIntersection(final LineIntersector li, final int segmentIndex,
    final int geomIndex, final int intIndex) {
    final Coordinates intPt = new Coordinate(li.getIntersection(intIndex));
    addIntersection(intPt, segmentIndex);
  }

  /**
   * Adds an intersection node for a given point and segment to this segment string.
   * If an intersection already exists for this exact location, the existing
   * node will be returned.
   * 
   * @param intPt the location of the intersection
   * @param segmentIndex the index of the segment containing the intersection
   * @return the intersection node for the point
   */
  public SegmentNode addIntersectionNode(final Coordinates intPt,
    final int segmentIndex) {
    int normalizedSegmentIndex = segmentIndex;
    // Debug.println("edge intpt: " + intPt + " dist: " + dist);
    // normalize the intersection point location
    final int nextSegIndex = normalizedSegmentIndex + 1;
    if (nextSegIndex < points.length) {
      final Coordinates nextPt = points[nextSegIndex];
      // Debug.println("next pt: " + nextPt);

      // Normalize segment index if intPt falls on vertex
      // The check for point equality is 2D only - Z values are ignored
      if (intPt.equals2d(nextPt)) {
        // Debug.println("normalized distance");
        normalizedSegmentIndex = nextSegIndex;
      }
    }
    /**
     * Add the intersection point to edge intersection list.
     */
    final SegmentNode ei = nodeList.add(intPt, normalizedSegmentIndex);
    return ei;
  }

  /**
   * Adds EdgeIntersections for one or both
   * intersections found for a segment of an edge to the edge intersection list.
   */
  public void addIntersections(final LineIntersector li,
    final int segmentIndex, final int geomIndex) {
    for (int i = 0; i < li.getIntersectionNum(); i++) {
      addIntersection(li, segmentIndex, geomIndex, i);
    }
  }

  @Override
  public Coordinates getCoordinate(final int i) {
    return points[i];
  }

  @Override
  public Coordinates[] getCoordinates() {
    return points;
  }

  /**
   * Gets the user-defined data for this segment string.
   *
   * @return the user-defined data
   */
  @Override
  public Object getData() {
    return data;
  }

  public SegmentNodeList getNodeList() {
    return nodeList;
  }

  /**
   * Gets the octant of the segment starting at vertex <code>index</code>.
   *
   * @param index the index of the vertex starting the segment.  Must not be
   * the last index in the vertex list
   * @return the octant of the segment at the vertex
   */
  public int getSegmentOctant(final int index) {
    if (index == points.length - 1) {
      return -1;
    }
    return safeOctant(getCoordinate(index), getCoordinate(index + 1));
    // return Octant.octant(getCoordinate(index), getCoordinate(index + 1));
  }

  @Override
  public boolean isClosed() {
    return points[0].equals(points[points.length - 1]);
  }

  private int safeOctant(final Coordinates p0, final Coordinates p1) {
    if (p0.equals2d(p1)) {
      return 0;
    }
    return Octant.octant(p0, p1);
  }

  /**
   * Sets the user-defined data for this segment string.
   *
   * @param data an Object containing user-defined data
   */
  @Override
  public void setData(final Object data) {
    this.data = data;
  }

  @Override
  public int size() {
    return points.length;
  }

  @Override
  public String toString() {
    return GeometryFactory.getFactory().lineString(points).toString() + "\t"
      + data;
  }
}
