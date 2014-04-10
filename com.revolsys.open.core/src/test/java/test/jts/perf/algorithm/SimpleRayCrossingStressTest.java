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
package test.jts.perf.algorithm;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.algorithm.PerturbedGridPolygonBuilder;
import com.revolsys.jts.algorithm.RayCrossingCounter;
import com.revolsys.jts.algorithm.locate.PointOnGeometryLocator;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.CoordinateSequenceFilter;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.PrecisionModel;

public class SimpleRayCrossingStressTest extends TestCase {

  static class SimpleRayCrossingPointInAreaLocator implements
    PointOnGeometryLocator {
    static class RayCrossingSegmentFilter implements CoordinateSequenceFilter {
      private final RayCrossingCounter rcc;

      private final Coordinate p0 = new Coordinate();

      private final Coordinate p1 = new Coordinate();

      public RayCrossingSegmentFilter(final RayCrossingCounter rcc) {
        this.rcc = rcc;
      }

      @Override
      public void filter(final CoordinatesList seq, final int i) {
        if (i == 0) {
          return;
        }
        seq.getCoordinate(i - 1, this.p0);
        seq.getCoordinate(i, this.p1);
        this.rcc.countSegment(this.p0, this.p1);
      }

      @Override
      public boolean isDone() {
        return this.rcc.isOnSegment();
      }

      @Override
      public boolean isGeometryChanged() {
        return false;
      }
    }

    private final Geometry geom;

    public SimpleRayCrossingPointInAreaLocator(final Geometry geom) {
      this.geom = geom;
    }

    @Override
    public int locate(final Coordinate p) {
      final RayCrossingCounter rcc = new RayCrossingCounter(p);
      final RayCrossingSegmentFilter filter = new RayCrossingSegmentFilter(rcc);
      this.geom.apply(filter);
      return rcc.getLocation();
    }
  }

  public static void main(final String args[]) {
    TestRunner.run(SimpleRayCrossingStressTest.class);
  }

  PrecisionModel pmFixed_1 = new PrecisionModel(1.0);

  public SimpleRayCrossingStressTest(final String name) {
    super(name);
  }

  public void testGrid() {
    // Use fixed PM to try and get at least some points hitting the boundary
    final GeometryFactory geomFactory = new GeometryFactory(this.pmFixed_1);
    // GeometryFactoryI geomFactory = new GeometryFactoryI();

    final PerturbedGridPolygonBuilder gridBuilder = new PerturbedGridPolygonBuilder(
      geomFactory);
    gridBuilder.setNumLines(20);
    gridBuilder.setLineWidth(10.0);
    final Geometry area = gridBuilder.getGeometry();

    final SimpleRayCrossingPointInAreaLocator pia = new SimpleRayCrossingPointInAreaLocator(
      area);

    final PointInAreaStressTester gridTester = new PointInAreaStressTester(
      geomFactory, area);
    gridTester.setNumPoints(100000);
    gridTester.setPIA(pia);

    final boolean isCorrect = gridTester.run();
    assertTrue(isCorrect);
  }
}
