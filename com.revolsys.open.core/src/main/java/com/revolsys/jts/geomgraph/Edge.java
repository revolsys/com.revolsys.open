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
package com.revolsys.jts.geomgraph;

import com.revolsys.jts.algorithm.LineIntersector;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.IntersectionMatrix;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.jts.geom.impl.LineStringDouble;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.geomgraph.index.MonotoneChainEdge;

/**
 * @version 1.7
 */
public class Edge extends GraphComponent {

  /**
   * Updates an IM from the label for an edge.
   * Handles edges from both L and A geometries.
   */
  public static void updateIM(final Label label, final IntersectionMatrix im) {
    im.setAtLeastIfValid(label.getLocation(0, Position.ON),
      label.getLocation(1, Position.ON), 1);
    if (label.isArea()) {
      im.setAtLeastIfValid(label.getLocation(0, Position.LEFT),
        label.getLocation(1, Position.LEFT), 2);
      im.setAtLeastIfValid(label.getLocation(0, Position.RIGHT),
        label.getLocation(1, Position.RIGHT), 2);
    }
  }

  private final LineString points;

  private BoundingBoxDoubleGf env;

  private final EdgeIntersectionList eiList = new EdgeIntersectionList(this);

  private String name;

  private MonotoneChainEdge mce;

  private boolean isIsolated = true;

  private final Depth depth = new Depth();

  private int depthDelta = 0; // the change in area depth from the R to L side
  // of this edge

  public Edge(final LineString points) {
    this(points, null);
  }

  public Edge(final LineString points, final Label label) {
    this.points = points;
    this.label = label;
  }

  /**
   * Add an EdgeIntersection for intersection intIndex.
   * An intersection that falls exactly on a vertex of the edge is normalized
   * to use the higher of the two possible segmentIndexes
   */
  public void addIntersection(final LineIntersector li, final int segmentIndex,
    final int geomIndex, final int intIndex) {
    final Point intPt = new PointDouble(li.getIntersection(intIndex));
    int normalizedSegmentIndex = segmentIndex;
    double dist = li.getEdgeDistance(geomIndex, intIndex);
    // normalize the intersection point location
    final int nextSegIndex = normalizedSegmentIndex + 1;
    if (nextSegIndex < getNumPoints()) {
      final Point nextPt = getCoordinate(nextSegIndex);
      // Normalize segment index if intPt falls on vertex
      // The check for point equality is 2D only - Z values are ignored
      if (intPt.equals(2,nextPt)) {
        normalizedSegmentIndex = nextSegIndex;
        dist = 0.0;
      }
    }
    /**
     * Add the intersection point to edge intersection list.
     */
    this.eiList.add(intPt, normalizedSegmentIndex, dist);
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

  /**
   * Update the IM with the contribution for this component.
   * A component only contributes if it has a labelling for both parent geometries
   */
  @Override
  public void computeIM(final IntersectionMatrix im) {
    updateIM(this.label, im);
  }

  /**
   * equals is defined to be:
   * <p>
   * e1 equals e2
   * <b>iff</b>
   * the coordinates of e1 are the same or the reverse of the coordinates in e2
   */
  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof Edge)) {
      return false;
    }
    final Edge e = (Edge)o;

    if (getNumPoints() != e.getNumPoints()) {
      return false;
    }

    boolean isEqualForward = true;
    boolean isEqualReverse = true;
    int iRev = getNumPoints();
    for (int i = 0; i < getNumPoints(); i++) {
      if (!getCoordinate(i).equals(2,e.getCoordinate(i))) {
        isEqualForward = false;
      }
      if (!getCoordinate(i).equals(2,e.getCoordinate(--iRev))) {
        isEqualReverse = false;
      }
      if (!isEqualForward && !isEqualReverse) {
        return false;
      }
    }
    return true;
  }

  public Edge getCollapsedEdge() {
    final LineString points = new LineStringDouble(getCoordinate(0),
      getCoordinate(1));
    final Edge edge = new Edge(points, Label.toLineLabel(this.label));
    return edge;
  }

  @Override
  public Point getCoordinate() {
    if (getNumPoints() > 0) {
      return getCoordinate(0);
    }
    return null;
  }

  public Point getCoordinate(final int i) {
    return this.points.getPoint(i);
  }

  public Depth getDepth() {
    return this.depth;
  }

  /**
   * The depthDelta is the change in depth as an edge is crossed from R to L
   * @return the change in depth as the edge is crossed from R to L
   */
  public int getDepthDelta() {
    return this.depthDelta;
  }

  public EdgeIntersectionList getEdgeIntersectionList() {
    return this.eiList;
  }

  public BoundingBox getEnvelope() {
    if (this.env == null) {
      this.env = new BoundingBoxDoubleGf(this.points);
    }
    return this.env;
  }

  public int getMaximumSegmentIndex() {
    return getNumPoints() - 1;
  }

  public MonotoneChainEdge getMonotoneChainEdge() {
    if (this.mce == null) {
      this.mce = new MonotoneChainEdge(this);
    }
    return this.mce;
  }

  public int getNumPoints() {
    return this.points.getVertexCount();
  }

  public LineString getPoints() {
    return this.points;
  }

  public boolean isClosed() {
    return getCoordinate(0).equals(getCoordinate(getNumPoints() - 1));
  }

  /**
   * An Edge is collapsed if it is an Area edge and it consists of
   * two segments which are equal and opposite (eg a zero-width V).
   */
  public boolean isCollapsed() {
    if (!this.label.isArea()) {
      return false;
    }
    if (getNumPoints() != 3) {
      return false;
    }
    if (getCoordinate(0).equals(getCoordinate(2))) {
      return true;
    }
    return false;
  }

  @Override
  public boolean isIsolated() {
    return this.isIsolated;
  }

  /**
   * @return true if the coordinate sequences of the Edges are identical
   */
  public boolean isPointwiseEqual(final Edge e) {
    if (getNumPoints() != e.getNumPoints()) {
      return false;
    }

    for (int i = 0; i < getNumPoints(); i++) {
      if (!getCoordinate(i).equals(2,e.getCoordinate(i))) {
        return false;
      }
    }
    return true;
  }

  public void setDepthDelta(final int depthDelta) {
    this.depthDelta = depthDelta;
  }

  public void setIsolated(final boolean isIsolated) {
    this.isIsolated = isIsolated;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    final StringBuilder buf = new StringBuilder();
    buf.append("edge " + this.name + ": ");
    buf.append("LINESTRING (");
    for (int i = 0; i < getNumPoints(); i++) {
      if (i > 0) {
        buf.append(",");
      }
      buf.append(getCoordinate(i).getX() + " " + getCoordinate(i).getY());
    }
    buf.append(")  " + this.label + " " + this.depthDelta);
    return buf.toString();
  }

}
