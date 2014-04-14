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
package com.revolsys.jts.testold.operation;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.operation.valid.IsValidOp;
import com.revolsys.jts.operation.valid.TopologyValidationError;

/**
 * @version 1.7
 */
public class IsValidTest extends TestCase {

  public static void main(final String args[]) {
    TestRunner.run(IsValidTest.class);
  }

  private final PrecisionModel precisionModel = new PrecisionModel();

  private final GeometryFactory geometryFactory = new GeometryFactory(
    this.precisionModel, 0);

  WKTReader reader = new WKTReader(this.geometryFactory);

  public IsValidTest(final String name) {
    super(name);
  }

  public void testInvalidCoordinate() throws Exception {
    final Coordinates badCoord = new Coordinate((double)1.0, Double.NaN, Coordinates.NULL_ORDINATE);
    final Coordinates[] pts = {
      new Coordinate((double)0.0, 0.0, Coordinates.NULL_ORDINATE), badCoord
    };
    final Geometry line = this.geometryFactory.lineString(pts);
    final IsValidOp isValidOp = new IsValidOp(line);
    final boolean valid = isValidOp.isValid();
    final TopologyValidationError err = isValidOp.getValidationError();
    final Coordinates errCoord = err.getCoordinate();

    assertEquals(TopologyValidationError.INVALID_COORDINATE, err.getErrorType());
    assertTrue(Double.isNaN(errCoord.getY()));
    assertEquals(false, valid);
  }

}
