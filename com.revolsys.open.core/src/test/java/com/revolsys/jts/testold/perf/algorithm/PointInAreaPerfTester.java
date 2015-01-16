package com.revolsys.jts.testold.perf.algorithm;

import com.revolsys.jts.algorithm.locate.PointOnGeometryLocator;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Location;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.util.Stopwatch;

/**
 * Creates a perturbed, buffered grid and tests a set
 * of points against using two PointInArea classes.
 *
 * @author mbdavis
 *
 */
public class PointInAreaPerfTester {
  private final GeometryFactory geomFactory;

  private final Geometry area;

  private int numPts = 10000;

  private PointOnGeometryLocator pia1;

  private final int[] locationCount = new int[3];

  public PointInAreaPerfTester(final GeometryFactory geomFactory,
    final Geometry area) {
    this.geomFactory = geomFactory;
    this.area = area;
  }

  public void printStats() {
    // System.out.println("Location counts: " + " Boundary = "
    // + this.locationCount[Location.BOUNDARY.getIndex()] + " Interior = "
    // + this.locationCount[Location.INTERIOR.getIndex()] + " Exterior = "
    // + this.locationCount[Location.EXTERIOR.getIndex()]);
  }

  /**
   *
   * @return true if all point locations were computed correctly
   */
  public boolean run() {
    final Stopwatch sw = new Stopwatch();

    final int ptGridWidth = (int)Math.sqrt(this.numPts);

    final BoundingBox areaEnv = this.area.getBoundingBox();
    final double xStep = areaEnv.getWidth() / (ptGridWidth - 1);
    final double yStep = areaEnv.getHeight() / (ptGridWidth - 1);

    for (int i = 0; i < ptGridWidth; i++) {
      for (int j = 0; j < ptGridWidth; j++) {

        // compute test point
        final double x = this.geomFactory.makePrecise(0, areaEnv.getMinX() + i
          * xStep);
        final double y = this.geomFactory.makePrecise(1, areaEnv.getMinY() + j
          * yStep);
        final Point pt = new PointDouble(x, y);

        final Location loc = this.pia1.locate(pt);
        this.locationCount[loc.getIndex()]++;
      }
    }
    // System.out.println("Test completed in " + sw.getTimeString());
    printStats();
    return true;
  }

  public void setNumPoints(final int numPoints) {
    this.numPts = numPoints;
  }

  public void setPIA(final PointOnGeometryLocator pia) {
    this.pia1 = pia;
  }

}
