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
package com.revolsys.jts.testold.perf.operation.buffer;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Polygonal;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.operation.buffer.BufferParameters;
import com.revolsys.jts.operation.buffer.OffsetCurveBuilder;

/**
 * Tests self-snapping issues
 * 
 * @version 1.7
 */
public class OffsetCurveCorrectnessTest {

  public static Geometry bufferOffsetCurve(final Geometry g,
    final double distance) {
    final OffsetCurveBuilder ocb = new OffsetCurveBuilder(
      g.getGeometryFactory().getPrecisionModel(), new BufferParameters());
    final Coordinates[] pts = g.getCoordinateArray();
    Coordinates[] curvePts = null;
    if (g instanceof Polygonal) {
      curvePts = ocb.getRingCurve(pts, 1, distance);
    } else {
      curvePts = ocb.getLineCurve(pts, distance);
    }
    final Geometry curve = g.getGeometryFactory().createLineString(curvePts);
    return curve;
  }

  public static void main(final String args[]) {
    try {
      new OffsetCurveCorrectnessTest().run7();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }

  }

  private final PrecisionModel precisionModel = new PrecisionModel();

  private final GeometryFactory geometryFactory = new GeometryFactory(
    this.precisionModel, 0);

  WKTReader rdr = new WKTReader(this.geometryFactory);

  public OffsetCurveCorrectnessTest() {
  }

  void run7() throws Exception {
    // buffer fails
    final String wkt = "MULTILINESTRING ((1335558.59524 631743.01449, 1335572.28215 631775.89056, 1335573.2578018496 631782.1915185435),  (1335573.2578018496 631782.1915185435, 1335576.62035 631803.90754),  (1335558.59524 631743.01449, 1335573.2578018496 631782.1915185435),  (1335573.2578018496 631782.1915185435, 1335580.70187 631802.08139))";
    final Geometry g = this.rdr.read(wkt);
    final Geometry curve = bufferOffsetCurve(g, 15);
    System.out.println(curve);
    // assert(curve.isValid());
  }

}
