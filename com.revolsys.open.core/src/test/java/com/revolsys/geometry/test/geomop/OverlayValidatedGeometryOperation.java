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
package com.revolsys.geometry.test.geomop;

import com.revolsys.format.wkt.EWktWriter;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.operation.overlay.OverlayOp;
import com.revolsys.geometry.operation.overlay.validate.OverlayResultValidator;
import com.revolsys.geometry.test.testrunner.GeometryResult;
import com.revolsys.geometry.test.testrunner.Result;

/**
 * A {@link GeometryOperation} which validates the result of overlay operations.
 * If an invalid result is found, an exception is thrown (this is the most
 * convenient and noticeable way of flagging the problem when using the TestRunner).
 * All other Geometry methods are executed normally.
 * <p>
 * In order to eliminate the need to specify the precise result of an overlay,
 * this class forces the final return value to be <tt>GEOMETRYCOLLECTION EMPTY</tt>.
 * <p>
 * This class can be used via the <tt>-geomop</tt> command-line option
 * or by the <tt>&lt;geometryOperation&gt;</tt> XML test file setting.
 *
 * @author Martin Davis
 *
 */
public class OverlayValidatedGeometryOperation implements GeometryOperation {
  private static final double AREA_DIFF_TOL = 5.0;

  public static double areaDiff(final Geometry g0, final Geometry g1) {
    final double areaA = g0.getArea();
    final double areaAdiffB = g0.difference(g1).getArea();
    final double areaAintB = g0.intersection(g1).getArea();
    return areaA - areaAdiffB - areaAintB;
  }

  public static Geometry invokeGeometryOverlayMethod(final int opCode, final Geometry g0,
    final Geometry g1) {
    switch (opCode) {
      case OverlayOp.INTERSECTION:
        return g0.intersection(g1);
      case OverlayOp.UNION:
        return g0.union(g1);
      case OverlayOp.DIFFERENCE:
        return g0.difference(g1);
      case OverlayOp.SYMDIFFERENCE:
        return g0.symDifference(g1);
    }
    throw new IllegalArgumentException("Unknown overlay op code");
  }

  public static int overlayOpCode(final String methodName) {
    if (methodName.equals("intersection")) {
      return OverlayOp.INTERSECTION;
    }
    if (methodName.equals("union")) {
      return OverlayOp.UNION;
    }
    if (methodName.equals("difference")) {
      return OverlayOp.DIFFERENCE;
    }
    if (methodName.equals("symDifference")) {
      return OverlayOp.SYMDIFFERENCE;
    }
    return -1;
  }

  private GeometryMethodOperation chainOp = new GeometryMethodOperation();

  private final boolean returnEmptyGC = true;

  public OverlayValidatedGeometryOperation() {

  }

  /**
   * Creates a new operation which chains to the given {@link GeometryMethodOperation}
   * for non-intercepted methods.
   *
   * @param chainOp the operation to chain to
   */
  public OverlayValidatedGeometryOperation(final GeometryMethodOperation chainOp) {
    this.chainOp = chainOp;
  }

  private void areaValidate(final Geometry g0, final Geometry g1) {
    final double areaDiff = areaDiff(g0, g1);
    // System.out.println("Area diff = " + areaDiff);
    if (Math.abs(areaDiff) > AREA_DIFF_TOL) {
      final String msg = "Operation result is invalid [AreaTest] (" + areaDiff + ")";
      reportError(msg);
    }
  }

  @Override
  public Class getReturnType(final String opName) {
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
  public Result invoke(final String opName, final Geometry geometry, final Object[] args)
    throws Exception {
    final int opCode = overlayOpCode(opName);

    // if not an overlay op, do the default
    if (opCode < 0) {
      return this.chainOp.invoke(opName, geometry, args);
    }
    return invokeValidatedOverlayOp(opCode, geometry, args);
  }

  /**
   * Invokes an overlay op, optionally using snapping,
   * and optionally validating the result.
   *
   * @param opCode
   * @param g0
   * @param args
   * @return the result
   * @throws Exception
   */
  public Result invokeValidatedOverlayOp(final int opCode, final Geometry g0, final Object[] args)
    throws Exception {
    Geometry result = null;
    final Geometry g1 = (Geometry)args[0];

    result = invokeGeometryOverlayMethod(opCode, g0, g1);

    // validate
    validate(opCode, g0, g1, result);
    areaValidate(g0, g1);

    /**
     * Return an empty GeometryCollection as the result.
     * This allows the test case to avoid specifying an exact result
     */
    if (this.returnEmptyGC) {
      result = result.getGeometryFactory().geometryCollection();
    }

    return new GeometryResult(result);
  }

  private void reportError(final String msg) {
    // System.out.println(msg);
    throw new RuntimeException(msg);
  }

  private void validate(final int opCode, final Geometry g0, final Geometry g1,
    final Geometry result) {
    final OverlayResultValidator validator = new OverlayResultValidator(g0, g1, result);
    // check if computed result is valid
    if (!validator.isValid(opCode)) {
      final Point invalidLoc = validator.getInvalidLocation();
      final String msg = "Operation result is invalid [OverlayResultValidator] ( "
        + EWktWriter.point(invalidLoc) + " )";
      reportError(msg);
    }
  }

}
