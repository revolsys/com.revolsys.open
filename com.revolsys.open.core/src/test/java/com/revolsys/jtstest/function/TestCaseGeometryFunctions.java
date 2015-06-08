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
package com.revolsys.jtstest.function;

import com.revolsys.jts.densify.Densifier;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.operation.buffer.Buffer;
import com.revolsys.jts.operation.buffer.BufferParameters;
import com.revolsys.jts.precision.MinimumClearance;

/**
 * Geometry functions which
 * augment the existing methods on {@link Geometry},
 * for use in XML Test files.
 * This is the default used in the TestRunner,
 * and thus all the operations
 * in this class should be named differently to the Geometry methods
 * (otherwise they will shadow the real Geometry methods).
 * <p>
 * If replacing a Geometry method is desired, this
 * can be done via the -geomfunc argument to the TestRunner.
 *
 * @author Martin Davis
 *
 */
public class TestCaseGeometryFunctions {
  public static Geometry bufferMitredJoin(final Geometry g, final double distance) {
    final BufferParameters bufParams = new BufferParameters();
    bufParams.setJoinStyle(BufferParameters.JOIN_MITRE);

    return Buffer.buffer(g, distance, bufParams);
  }

  public static Geometry densify(final Geometry g, final double distance) {
    return Densifier.densify(g, distance);
  }

  public static double minClearance(final Geometry g) {
    return MinimumClearance.getDistance(g);
  }

  public static Geometry minClearanceLine(final Geometry g) {
    return MinimumClearance.getLine(g);
  }

}
