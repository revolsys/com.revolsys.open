
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
package com.revolsys.jtstest.test;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.IntersectionMatrix;
import com.revolsys.jts.io.ParseException;

/**
 * @version 1.7
 */
public interface Testable {

  String getDescription();

  Geometry getExpectedBoundary();

  Geometry getExpectedConvexHull();

  Geometry getExpectedDifference();

  Geometry getExpectedIntersection();

  String getExpectedIntersectionMatrix();

  Geometry getExpectedSymDifference();

  Geometry getExpectedUnion();

  String getFailedMsg();

  Geometry getGeometry(int index);

  IntersectionMatrix getIntersectionMatrix();

  String getName();

  String getWellKnownText(int i);

  void initGeometry() throws ParseException;

  boolean isFailed();

  boolean isPassed();

  void runTest() throws ParseException;

  void setExpectedBoundary(Geometry boundary);

  void setExpectedCentroid(Geometry expectedCentroid);

  // void setExpectedInteriorPoint(Geometry expectedCentroid);

  void setExpectedConvexHull(Geometry expectedConvexHull);

  void setExpectedDifference(Geometry expectedDifference);

  void setExpectedIntersection(Geometry expectedIntersection);

  void setExpectedIntersectionMatrix(String expectedIntersectionMatrix);

  void setExpectedSymDifference(Geometry expectedSymDifference);

  void setExpectedUnion(Geometry expectedUnion);

  void setGeometry(int index, Geometry g);

  void setIntersectionMatrix(IntersectionMatrix im);

  void setName(String name);
}
