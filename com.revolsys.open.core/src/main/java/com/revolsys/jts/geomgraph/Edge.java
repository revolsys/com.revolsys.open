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

import java.io.PrintStream;

import com.revolsys.jts.algorithm.LineIntersector;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.IntersectionMatrix;
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

  Coordinates[] pts;

  private Envelope env;

  EdgeIntersectionList eiList = new EdgeIntersectionList(this);

  private String name;

  private MonotoneChainEdge mce;

  private boolean isIsolated = true;

  private final Depth depth = new Depth();

  private int depthDelta = 0; // the change in area depth from the R to L side
                              // of this edge

  public Edge(final Coordinates[] pts) {
    this(pts, null);
  }

  public Edge(final Coordinates[] pts, final Label label) {
    this.pts = pts;
    this.label = label;
  }

  /**
   * Add an EdgeIntersection for intersection intIndex.
   * An intersection that falls exactly on a vertex of the edge is normalized
   * to use the higher of the two possible segmentIndexes
   */
  public void addIntersection(final LineIntersector li, final int segmentIndex,
    final int geomIndex, final int intIndex) {
    final Coordinates intPt = new Coordinate(li.getIntersection(intIndex));
    int normalizedSegmentIndex = segmentIndex;
    double dist = li.getEdgeDistance(geomIndex, intIndex);
    // Debug.println("edge intpt: " + intPt + " dist: " + dist);
    // normalize the intersection point location
    final int nextSegIndex = normalizedSegmentIndex + 1;
    if (nextSegIndex < pts.length) {
      final Coordinates nextPt = pts[nextSegIndex];
      // Debug.println("next pt: " + nextPt);

      // Normalize segment index if intPt falls on vertex
      // The check for point equality is 2D only - Z values are ignored
      if (intPt.equals2d(nextPt)) {
        // Debug.println("normalized distance");
        normalizedSegmentIndex = nextSegIndex;
        dist = 0.0;
      }
    }
    /**
    * Add the intersection point to edge intersection list.
    */
    final EdgeIntersection ei = eiList.add(intPt, normalizedSegmentIndex, dist);
    // ei.print(System.out);

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
    updateIM(label, im);
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

    if (pts.length != e.pts.length) {
      return false;
    }

    boolean isEqualForward = true;
    boolean isEqualReverse = true;
    int iRev = pts.length;
    for (int i = 0; i < pts.length; i++) {
      if (!pts[i].equals2d(e.pts[i])) {
        isEqualForward = false;
      }
      if (!pts[i].equals2d(e.pts[--iRev])) {
        isEqualReverse = false;
      }
      if (!isEqualForward && !isEqualReverse) {
        return false;
      }
    }
    return true;
  }

  public Edge getCollapsedEdge() {
    final Coordinates newPts[] = new Coordinates[2];
    newPts[0] = pts[0];
    newPts[1] = pts[1];
    final Edge newe = new Edge(newPts, Label.toLineLabel(label));
    return newe;
  }

  @Override
  public Coordinates getCoordinate() {
    if (pts.length > 0) {
      return pts[0];
    }
    return null;
  }

  public Coordinates getCoordinate(final int i) {
    return pts[i];
  }

  public Coordinates[] getCoordinates() {
    return pts;
  }

  public Depth getDepth() {
    return depth;
  }

  /**
   * The depthDelta is the change in depth as an edge is crossed from R to L
   * @return the change in depth as the edge is crossed from R to L
   */
  public int getDepthDelta() {
    return depthDelta;
  }

  public EdgeIntersectionList getEdgeIntersectionList() {
    return eiList;
  }

  public BoundingBox getEnvelope() {
    if (env == null) {
      env = new Envelope(pts);
    }
    return env;
  }

  public int getMaximumSegmentIndex() {
    return pts.length - 1;
  }

  public MonotoneChainEdge getMonotoneChainEdge() {
    if (mce == null) {
      mce = new MonotoneChainEdge(this);
    }
    return mce;
  }

  public int getNumPoints() {
    return pts.length;
  }

  public boolean isClosed() {
    return pts[0].equals(pts[pts.length - 1]);
  }

  /**
   * An Edge is collapsed if it is an Area edge and it consists of
   * two segments which are equal and opposite (eg a zero-width V).
   */
  public boolean isCollapsed() {
    if (!label.isArea()) {
      return false;
    }
    if (pts.length != 3) {
      return false;
    }
    if (pts[0].equals(pts[2])) {
      return true;
    }
    return false;
  }

  @Override
  public boolean isIsolated() {
    return isIsolated;
  }

  /**
   * @return true if the coordinate sequences of the Edges are identical
   */
  public boolean isPointwiseEqual(final Edge e) {
    if (pts.length != e.pts.length) {
      return false;
    }

    for (int i = 0; i < pts.length; i++) {
      if (!pts[i].equals2d(e.pts[i])) {
        return false;
      }
    }
    return true;
  }

  public void print(final PrintStream out) {
    out.print("edge " + name + ": ");
    out.print("LINESTRING (");
    for (int i = 0; i < pts.length; i++) {
      if (i > 0) {
        out.print(",");
      }
      out.print(pts[i].getX() + " " + pts[i].getY());
    }
    out.print(")  " + label + " " + depthDelta);
  }

  public void printReverse(final PrintStream out) {
    out.print("edge " + name + ": ");
    for (int i = pts.length - 1; i >= 0; i--) {
      out.print(pts[i] + " ");
    }
    out.println("");
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
    final StringBuffer buf = new StringBuffer();
    buf.append("edge " + name + ": ");
    buf.append("LINESTRING (");
    for (int i = 0; i < pts.length; i++) {
      if (i > 0) {
        buf.append(",");
      }
      buf.append(pts[i].getX() + " " + pts[i].getY());
    }
    buf.append(")  " + label + " " + depthDelta);
    return buf.toString();
  }

}
