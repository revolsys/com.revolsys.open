package com.revolsys.gis.model.geometry.operation.overlay;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.geometry.Geometry;

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
  private final Geometry g;

  private boolean doLeft = true;

  private boolean doRight = true;

  public OffsetPointGenerator(final Geometry g) {
    this.g = g;
  }

  /**
   * Generates the two points which are offset from the midpoint of the segment
   * <tt>(p0, p1)</tt> by the <tt>offsetDistance</tt>.
   * 
   * @param p0 the first point of the segment to offset from
   * @param p1 the second point of the segment to offset from
   */
  private void computeOffsetPoints(final Coordinates p0, final Coordinates p1,
    final double offsetDistance, final List<Coordinates> offsetPts) {
    final double dx = p1.getX() - p0.getX();
    final double dy = p1.getY() - p0.getY();
    final double len = Math.sqrt(dx * dx + dy * dy);
    // u is the vector that is the length of the offset, in the direction of the
    // segment
    final double ux = offsetDistance * dx / len;
    final double uy = offsetDistance * dy / len;

    final double midX = (p1.getX() + p0.getX()) / 2;
    final double midY = (p1.getY() + p0.getY()) / 2;

    if (doLeft) {
      final Coordinates offsetLeft = new DoubleCoordinates(midX - uy, midY + ux);
      offsetPts.add(offsetLeft);
    }

    if (doRight) {
      final Coordinates offsetRight = new DoubleCoordinates(midX + uy, midY
        - ux);
      offsetPts.add(offsetRight);
    }
  }

  private void extractPoints(final CoordinatesList pts,
    final double offsetDistance, final List<Coordinates> offsetPts) {
    for (int i = 0; i < pts.size() - 1; i++) {
      computeOffsetPoints(pts.get(i), pts.get(i + 1), offsetDistance, offsetPts);
    }
  }

  /**
   * Gets the computed offset points.
   * 
   * @return List<Coordinate>
   */
  public List<Coordinates> getPoints(final double offsetDistance) {
    final List<Coordinates> offsetPts = new ArrayList<Coordinates>();
    for (final CoordinatesList points : g.getCoordinatesLists()) {
      if (points.size() > 1) {
        extractPoints(points, offsetDistance, offsetPts);
      }
    }
    return offsetPts;
  }

  /**
   * Set the sides on which to generate offset points.
   * 
   * @param doLeft
   * @param doRight
   */
  public void setSidesToGenerate(final boolean doLeft, final boolean doRight) {
    this.doLeft = doLeft;
    this.doRight = doRight;
  }

}
