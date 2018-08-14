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
package com.revolsys.core.test.geometry.test.geomop;

import com.revolsys.core.test.geometry.test.testrunner.Result;
import com.revolsys.geometry.model.Geometry;

/**
 * A {@link GeometryOperation} which executes the original operation
 * and returns that result,
 * but also executes a separate operation (which could be multiple operations).
 * The side operations can throw exceptions if they do not compute
 * correct results.  This relies on the availability of
 * another reliable implementation to provide the expected result.
 * <p>
 * This class can be used via the <tt>-geomop</tt> command-line option
 * or by the <tt>&lt;geometryOperation&gt;</tt> XML test file setting.
 *
 * @author mbdavis
 *
 */
public abstract class TeeGeometryOperation implements GeometryOperation {
  private GeometryMethodOperation chainOp = new GeometryMethodOperation();

  public TeeGeometryOperation() {

  }

  /**
   * Creates a new operation which chains to the given {@link GeometryMethodOperation}
   * for non-intercepted methods.
   *
   * @param chainOp the operation to chain to
   */
  public TeeGeometryOperation(final GeometryMethodOperation chainOp) {
    this.chainOp = chainOp;
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
    runTeeOp(opName, geometry, args);

    return this.chainOp.invoke(opName, geometry, args);
  }

  protected abstract void runTeeOp(String opName, Geometry geometry, Object[] args);

}
