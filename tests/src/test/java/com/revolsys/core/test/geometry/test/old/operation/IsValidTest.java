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
package com.revolsys.core.test.geometry.test.old.operation;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.operation.valid.CoordinateNaNError;
import com.revolsys.geometry.operation.valid.GeometryValidationError;
import com.revolsys.geometry.operation.valid.IsValidOp;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * @version 1.7
 */
public class IsValidTest extends TestCase {

  public static void main(final String args[]) {
    TestRunner.run(IsValidTest.class);
  }

  private final GeometryFactory geometryFactory = GeometryFactory.floating2d(0);

  public IsValidTest(final String name) {
    super(name);
  }

  public void testNaNCoordinate() throws Exception {
    final Point badCoord = new PointDoubleXY(1.0, Double.NaN);
    final Point[] pts = {
      new PointDoubleXY(0.0, 0.0), badCoord
    };
    final Geometry line = this.geometryFactory.lineString(pts);
    final IsValidOp isValidOp = new IsValidOp(line);
    final boolean valid = isValidOp.isValid();
    final GeometryValidationError err = isValidOp.getValidationError();
    final Point errCoord = err.getErrorPoint();

    assertTrue(err instanceof CoordinateNaNError);
    assertTrue(Double.isNaN(errCoord.getY()));
    assertEquals(false, valid);
  }

}
