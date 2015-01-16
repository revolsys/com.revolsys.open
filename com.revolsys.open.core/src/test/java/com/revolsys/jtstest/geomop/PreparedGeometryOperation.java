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
package com.revolsys.jtstest.geomop;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jtstest.testrunner.BooleanResult;
import com.revolsys.jtstest.testrunner.Result;

/**
 * A {@link GeometryOperation} which uses {@link PreparedGeometry}s
 * for applicable operations.
 * This allows testing correctness of the <tt>PreparedGeometry</tt> implementation.
 * <p>
 * This class can be used via the <tt>-geomop</tt> command-line option
 * or by the <tt>&lt;geometryOperation&gt;</tt> XML test file setting.
 *
 * @author mbdavis
 *
 */
public class PreparedGeometryOperation implements GeometryOperation {
  static class PreparedGeometryOp {
    public static boolean contains(final Geometry g1, final Geometry g2) {
      final Geometry prepGeom = g1.prepare();
      return prepGeom.contains(g2);
    }

    public static boolean containsProperly(final Geometry g1, final Geometry g2) {
      final Geometry prepGeom = g1.prepare();
      return prepGeom.containsProperly(g2);
    }

    public static boolean covers(final Geometry g1, final Geometry g2) {
      final Geometry prepGeom = g1.prepare();
      return prepGeom.covers(g2);
    }

    public static boolean intersects(final Geometry g1, final Geometry g2) {
      final Geometry prepGeom = g1.prepare();
      return prepGeom.intersects(g2);
    }
  }

  private static boolean isPreparedOp(final String opName) {
    if (opName.equals("intersects")) {
      return true;
    }
    if (opName.equals("contains")) {
      return true;
    }
    if (opName.equals("containsProperly")) {
      return true;
    }
    if (opName.equals("covers")) {
      return true;
    }
    return false;
  }

  private GeometryMethodOperation chainOp = new GeometryMethodOperation();

  public PreparedGeometryOperation() {

  }

  /**
   * Creates a new operation which chains to the given {@link GeometryMethodOperation}
   * for non-intercepted methods.
   *
   * @param chainOp the operation to chain to
   */
  public PreparedGeometryOperation(final GeometryMethodOperation chainOp) {
    this.chainOp = chainOp;
  }

  @Override
  public Class getReturnType(final String opName) {
    if (isPreparedOp(opName)) {
      return boolean.class;
    }
    return this.chainOp.getReturnType(opName);
  }

  /**
   * Invokes the named operation
   *
   * @param opName
   * @param geometry
   * @param args
   * @return the result
   * @throws Exception
   * @see GeometryOperation#invoke
   */
  @Override
  public Result invoke(final String opName, final Geometry geometry,
    final Object[] args) throws Exception {
    if (!isPreparedOp(opName)) {
      return this.chainOp.invoke(opName, geometry, args);
    }
    return invokePreparedOp(opName, geometry, args);
  }

  private Result invokePreparedOp(final String opName, final Geometry geometry,
    final Object[] args) {
    final Geometry g2 = (Geometry)args[0];
    if (opName.equals("intersects")) {
      return new BooleanResult(PreparedGeometryOp.intersects(geometry, g2));
    }
    if (opName.equals("contains")) {
      return new BooleanResult(PreparedGeometryOp.contains(geometry, g2));
    }
    if (opName.equals("containsProperly")) {
      return new BooleanResult(
        PreparedGeometryOp.containsProperly(geometry, g2));
    }
    if (opName.equals("covers")) {
      return new BooleanResult(PreparedGeometryOp.covers(geometry, g2));
    }
    return null;
  }
}
