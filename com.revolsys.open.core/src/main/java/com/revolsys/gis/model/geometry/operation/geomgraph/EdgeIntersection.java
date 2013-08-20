package com.revolsys.gis.model.geometry.operation.geomgraph;

import java.io.PrintStream;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;

/**
 * Represents a point on an edge which intersects with another edge.
 * <p>
 * The intersection may either be a single point, or a line segment (in which
 * case this point is the start of the line segment) The intersection point must
 * be precise.
 * 
 * @version 1.7
 */
public class EdgeIntersection implements Comparable {

  public Coordinates coord; // the point of intersection

  public int segmentIndex; // the index of the containing line segment in the
                           // parent edge

  public double dist; // the edge distance of this point along the containing
                      // line segment

  public EdgeIntersection(final Coordinates coord, final int segmentIndex,
    final double dist) {
    this.coord = new DoubleCoordinates(coord);
    this.segmentIndex = segmentIndex;
    this.dist = dist;
  }

  /**
   * @return -1 this EdgeIntersection is located before the argument location
   * @return 0 this EdgeIntersection is at the argument location
   * @return 1 this EdgeIntersection is located after the argument location
   */
  public int compare(final int segmentIndex, final double dist) {
    if (this.segmentIndex < segmentIndex) {
      return -1;
    }
    if (this.segmentIndex > segmentIndex) {
      return 1;
    }
    if (this.dist < dist) {
      return -1;
    }
    if (this.dist > dist) {
      return 1;
    }
    return 0;
  }

  @Override
  public int compareTo(final Object obj) {
    final EdgeIntersection other = (EdgeIntersection)obj;
    return compare(other.segmentIndex, other.dist);
  }

  public Coordinates getCoordinate() {
    return coord;
  }

  public double getDistance() {
    return dist;
  }

  public int getSegmentIndex() {
    return segmentIndex;
  }

  public boolean isEndPoint(final int maxSegmentIndex) {
    if (segmentIndex == 0 && dist == 0.0) {
      return true;
    }
    if (segmentIndex == maxSegmentIndex) {
      return true;
    }
    return false;
  }

  public void print(final PrintStream out) {
    out.print(coord);
    out.print(" seg # = " + segmentIndex);
    out.println(" dist = " + dist);
  }
}
