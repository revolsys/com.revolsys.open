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
package com.revolsys.jts.testold.perf.algorithm;

import com.revolsys.jts.algorithm.locate.PointOnGeometryLocator;
import com.revolsys.jts.algorithm.locate.SimplePointInAreaLocator;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Location;
import com.revolsys.jts.util.Stopwatch;

/**
 * Creates a perturbed, buffered grid and tests a set
 * of points against using two PointInArea classes.
 * 
 * @author mbdavis
 *
 */
public class PointInAreaStressTester {
  private final GeometryFactory geomFactory;

  private final Geometry area;

  private boolean ignoreBoundaryResults = true;

  private int numPts = 10000;

  private PointOnGeometryLocator pia1;

  private PointOnGeometryLocator pia2;

  private final int[] locationCount = new int[3];

  public PointInAreaStressTester(final GeometryFactory geomFactory,
    final Geometry area) {
    this.geomFactory = geomFactory;
    this.area = area;

  }

  public void printStats() {
    System.out.println("Location counts: " + " Boundary = "
      + this.locationCount[Location.BOUNDARY] + " Interior = "
      + this.locationCount[Location.INTERIOR] + " Exterior = "
      + this.locationCount[Location.EXTERIOR]);
  }

  /**
   * 
   * @return true if all point locations were computed correctly
   */
  public boolean run() {
    final Stopwatch sw = new Stopwatch();

    // default is to use the simple, non-indexed tester
    if (this.pia2 == null) {
      this.pia2 = new SimplePointInAreaLocator(this.area);
    }

    final int ptGridWidth = (int)Math.sqrt(this.numPts);

    final Envelope areaEnv = this.area.getEnvelopeInternal();
    final double xStep = areaEnv.getWidth() / (ptGridWidth - 1);
    final double yStep = areaEnv.getHeight() / (ptGridWidth - 1);

    for (int i = 0; i < ptGridWidth; i++) {
      for (int j = 0; j < ptGridWidth; j++) {

        // compute test point
        final double x = areaEnv.getMinX() + i * xStep;
        final double y = areaEnv.getMinY() + j * yStep;
        final Coordinates pt = new Coordinate((double)x, y, Coordinates.NULL_ORDINATE);
        this.geomFactory.getPrecisionModel().makePrecise(pt);

        final boolean isEqual = testPIA(pt);
        if (!isEqual) {
          return false;
        }
      }
    }
    System.out.println("Test completed in " + sw.getTimeString());
    printStats();
    return true;
  }

  public void setExpected(final PointOnGeometryLocator pia) {
    this.pia2 = pia;
  }

  public void setIgnoreBoundaryResults(final boolean ignoreBoundaryResults) {
    this.ignoreBoundaryResults = ignoreBoundaryResults;
  }

  public void setNumPoints(final int numPoints) {
    this.numPts = numPoints;
  }

  public void setPIA(final PointOnGeometryLocator pia) {
    this.pia1 = pia;
  }

  /**
   * 
   * @param p
   * @return true if the point location is determined to be the same by both PIA locaters
   */
  private boolean testPIA(final Coordinates p) {
    // System.out.println(WKTWriter.toPoint(p));

    final int loc1 = this.pia1.locate(p);
    final int loc2 = this.pia2.locate(p);

    this.locationCount[loc1]++;

    if ((loc1 == Location.BOUNDARY || loc2 == Location.BOUNDARY)
      && this.ignoreBoundaryResults) {
      return true;
    }

    return loc1 == loc2;
  }

}
