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

import com.revolsys.geometry.algorithm.locate.PointOnGeometryLocator;
import com.revolsys.geometry.algorithm.locate.SimplePointInAreaLocator;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.util.Stopwatch;

/**
 * Creates a perturbed, buffered grid and tests a set
 * of points against using two PointInArea classes.
 *
 * @author mbdavis
 *
 */
public class PointInAreaStressTester {
  private final Geometry area;

  private final GeometryFactory geomFactory;

  private boolean ignoreBoundaryResults = true;

  private final int[] locationCount = new int[3];

  private int numPts = 10000;

  private PointOnGeometryLocator pia1;

  private PointOnGeometryLocator pia2;

  public PointInAreaStressTester(final GeometryFactory geomFactory, final Geometry area) {
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

    // default is to use the simple, non-indexed tester
    if (this.pia2 == null) {
      this.pia2 = new SimplePointInAreaLocator(this.area);
    }

    final int ptGridWidth = (int)Math.sqrt(this.numPts);

    final BoundingBox areaEnv = this.area.getBoundingBox();
    final double xStep = areaEnv.getWidth() / (ptGridWidth - 1);
    final double yStep = areaEnv.getHeight() / (ptGridWidth - 1);

    for (int i = 0; i < ptGridWidth; i++) {
      for (int j = 0; j < ptGridWidth; j++) {

        // compute test point
        final double x = this.geomFactory.makePrecise(0, areaEnv.getMinX() + i * xStep);
        final double y = this.geomFactory.makePrecise(1, areaEnv.getMinY() + j * yStep);
        final Point pt = new PointDouble(x, y);

        final boolean isEqual = testPIA(pt);
        if (!isEqual) {
          return false;
        }
      }
    }
    // System.out.println("Test completed in " + sw.getTimeString());
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
  private boolean testPIA(final Point p) {
    // System.out.println(WKTWriter.toPoint(p));

    final Location loc1 = this.pia1.locate(p);
    final Location loc2 = this.pia2.locate(p);

    this.locationCount[loc1.getIndex()]++;

    if ((loc1 == Location.BOUNDARY || loc2 == Location.BOUNDARY) && this.ignoreBoundaryResults) {
      return true;
    }

    return loc1 == loc2;
  }

}
