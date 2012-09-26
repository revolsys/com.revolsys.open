package com.revolsys.gis.model.geometry.operation.geomgraph;

import java.io.PrintStream;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.geometry.impl.BoundingBox;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.LineIntersector;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.MonotoneChainEdge;
import com.vividsolutions.jts.geom.IntersectionMatrix;

/**
 * @version 1.7
 */
public class Edge extends GraphComponent {

  /**
   * Updates an IM from the label for an edge. Handles edges from both L and A
   * geometries.
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

  CoordinatesList pts;

  private BoundingBox env;

  EdgeIntersectionList eiList = new EdgeIntersectionList(this);

  private String name;

  private MonotoneChainEdge mce;

  private boolean isIsolated = true;

  private final Depth depth = new Depth();

  private int depthDelta = 0; // the change in area depth from the R to L side
                              // of this edge

  public Edge(CoordinatesList pts, Label label) {
    this.pts = pts;
    this.label = label;
  }

  /**
   * Add an EdgeIntersection for intersection intIndex. An intersection that
   * falls exactly on a vertex of the edge is normalized to use the higher of
   * the two possible segmentIndexes
   */
  public void addIntersection(final LineIntersector li, final int segmentIndex,
    final int geomIndex, final int intIndex) {
    final Coordinates intersection = li.getIntersection(intIndex);
    final Coordinates intPt = new DoubleCoordinates(intersection);
    int normalizedSegmentIndex = segmentIndex;
    double dist = li.getEdgeDistance(geomIndex, intIndex);
    // Debug.println("edge intpt: " + intPt + " dist: " + dist);
    // normalize the intersection point location
    final int nextSegIndex = normalizedSegmentIndex + 1;
    if (nextSegIndex < pts.size()) {
      final Coordinates nextPt = pts.get(nextSegIndex);
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
   * Adds EdgeIntersections for one or both intersections found for a segment of
   * an edge to the edge intersection list.
   */
  public void addIntersections(final LineIntersector li,
    final int segmentIndex, final int geomIndex) {
    for (int i = 0; i < li.getIntersectionNum(); i++) {
      addIntersection(li, segmentIndex, geomIndex, i);
    }
  }

  /**
   * Update the IM with the contribution for this component. A component only
   * contributes if it has a labelling for both parent geometries
   */
  @Override
  public void computeIM(final IntersectionMatrix im) {
    updateIM(label, im);
  }

  /**
   * equals is defined to be:
   * <p>
   * e1 equals e2 <b>iff</b> the coordinates of e1 are the same or the reverse
   * of the coordinates in e2
   */
  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof Edge)) {
      return false;
    }
    final Edge e = (Edge)o;

    if (pts.size() != e.pts.size()) {
      return false;
    }

    boolean isEqualForward = true;
    boolean isEqualReverse = true;
    int iRev = pts.size();
    for (int i = 0; i < pts.size(); i++) {
      if (!pts.get(i).equals2d(e.pts.get(i))) {
        isEqualForward = false;
      }
      if (!pts.get(i).equals2d(e.pts.get(--iRev))) {
        isEqualReverse = false;
      }
      if (!isEqualForward && !isEqualReverse) {
        return false;
      }
    }
    return true;
  }

  public Edge getCollapsedEdge() {
    final CoordinatesList newPts = new DoubleCoordinatesList(pts.getNumAxis(),
      pts.get(0), pts.get(1));
    final Edge newe = new Edge(newPts, Label.toLineLabel(label));
    return newe;
  }

  @Override
  public Coordinates getCoordinate() {
    if (pts.size() > 0) {
      return pts.get(0);
    }
    return null;
  }

  public Coordinates getCoordinate(final int i) {
    return pts.get(i);
  }

  public CoordinatesList getCoordinates() {
    return pts;
  }

  public Depth getDepth() {
    return depth;
  }

  /**
   * The depthDelta is the change in depth as an edge is crossed from R to L
   * 
   * @return the change in depth as the edge is crossed from R to L
   */
  public int getDepthDelta() {
    return depthDelta;
  }

  public EdgeIntersectionList getEdgeIntersectionList() {
    return eiList;
  }

  public BoundingBox getEnvelope() {
    // compute envelope lazily
    if (env == null) {
      env = new BoundingBox();
      for (int i = 0; i < pts.size(); i++) {
        env = env.expandToInclude(pts.get(i));
      }
    }
    return env;
  }

  public int getMaximumSegmentIndex() {
    return pts.size() - 1;
  }

  public MonotoneChainEdge getMonotoneChainEdge() {
    if (mce == null) {
      mce = new MonotoneChainEdge(this);
    }
    return mce;
  }

  public int getNumPoints() {
    return pts.size();
  }

  public boolean isClosed() {
    return pts.get(0).equals(pts.get(pts.size() - 1));
  }

  /**
   * An Edge is collapsed if it is an Area edge and it consists of two segments
   * which are equal and opposite (eg a zero-width V).
   */
  public boolean isCollapsed() {
    if (!label.isArea()) {
      return false;
    }
    if (pts.size() != 3) {
      return false;
    }
    if (pts.get(0).equals(pts.get(2))) {
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
    if (pts.size() != e.pts.size()) {
      return false;
    }

    for (int i = 0; i < pts.size(); i++) {
      if (!pts.get(i).equals2d(e.pts.get(i))) {
        return false;
      }
    }
    return true;
  }

  public void print(final PrintStream out) {
    out.print("edge " + name + ": ");
    out.print("LINESTRING (");
    for (int i = 0; i < pts.size(); i++) {
      if (i > 0) {
        out.print(",");
      }
      out.print(pts.get(i).getX() + " " + pts.get(i).getY());
    }
    out.print(")  " + label + " " + depthDelta);
  }

  public void printReverse(final PrintStream out) {
    out.print("edge " + name + ": ");
    for (int i = pts.size() - 1; i >= 0; i--) {
      out.print(pts.get(i) + " ");
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

}
