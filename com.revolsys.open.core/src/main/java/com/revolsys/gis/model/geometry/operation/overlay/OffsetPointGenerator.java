package com.revolsys.gis.model.geometry.operation.overlay;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.LineString;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.util.LinearComponentExtracter;

/**
 * Generates points offset by a given distance from both sides of the midpoint
 * of all segments in a {@link Geometry}. Can be used to generate probe points
 * for determining whether a polygonal overlay result is incorrect. The input
 * geometry may have any orientation for its rings, but
 * {@link #setSidesToGenerate(boolean, boolean)} is only meaningful if the
 * orientation is known.
 * 
 * @author Martin Davis
 * @version 1.7
 */
public class OffsetPointGenerator {
  private Geometry g;

  private boolean doLeft = true;

  private boolean doRight = true;

  public OffsetPointGenerator(Geometry g) {
    this.g = g;
  }

  /**
   * Set the sides on which to generate offset points.
   * 
   * @param doLeft
   * @param doRight
   */
  public void setSidesToGenerate(boolean doLeft, boolean doRight) {
    this.doLeft = doLeft;
    this.doRight = doRight;
  }

  /**
   * Gets the computed offset points.
   * 
   * @return List<Coordinate>
   */
  public List<Coordinates> getPoints(double offsetDistance) {
    List<Coordinates> offsetPts = new ArrayList<Coordinates>();
    for (CoordinatesList points : g.getCoordinatesLists()) {
      if (points.size() > 1) {
        extractPoints(points, offsetDistance, offsetPts);
      }
    }
    return offsetPts;
  }

  private void extractPoints(CoordinatesList pts, double offsetDistance,
    List<Coordinates> offsetPts) {
    for (int i = 0; i < pts.size() - 1; i++) {
      computeOffsetPoints(pts.get(i), pts.get(i + 1), offsetDistance, offsetPts);
    }
  }

  /**
   * Generates the two points which are offset from the midpoint of the segment
   * <tt>(p0, p1)</tt> by the <tt>offsetDistance</tt>.
   * 
   * @param p0 the first point of the segment to offset from
   * @param p1 the second point of the segment to offset from
   */
  private void computeOffsetPoints(Coordinates p0, Coordinates p1,
    double offsetDistance, List<Coordinates> offsetPts) {
    double dx = p1.getX() - p0.getX();
    double dy = p1.getY() - p0.getY();
    double len = Math.sqrt(dx * dx + dy * dy);
    // u is the vector that is the length of the offset, in the direction of the
    // segment
    double ux = offsetDistance * dx / len;
    double uy = offsetDistance * dy / len;

    double midX = (p1.getX() + p0.getX()) / 2;
    double midY = (p1.getY() + p0.getY()) / 2;

    if (doLeft) {
      Coordinates offsetLeft = new DoubleCoordinates(midX - uy, midY + ux);
      offsetPts.add(offsetLeft);
    }

    if (doRight) {
      Coordinates offsetRight = new DoubleCoordinates(midX + uy, midY - ux);
      offsetPts.add(offsetRight);
    }
  }

}
