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

package com.revolsys.geometry.test.old.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.revolsys.geometry.test.old.algorithm.AngleTest;
import com.revolsys.geometry.test.old.algorithm.ConvexHullTest;
import com.revolsys.geometry.test.old.algorithm.InteriorPointTest;
import com.revolsys.geometry.test.old.algorithm.IsCounterClockWiseTest;
import com.revolsys.geometry.test.old.algorithm.NonRobustLineIntersectorTest;
import com.revolsys.geometry.test.old.algorithm.OrientationIndexTest;
import com.revolsys.geometry.test.old.algorithm.RobustLineIntersectionTest;
import com.revolsys.geometry.test.old.geom.AreaLengthTest;
import com.revolsys.geometry.test.old.geom.CoordinateArraysTest;
import com.revolsys.geometry.test.old.geom.GeometryCollectionImplTest;
import com.revolsys.geometry.test.old.geom.GeometryImplTest;
import com.revolsys.geometry.test.old.geom.IntersectionMatrixTest;
import com.revolsys.geometry.test.old.geom.IsRectangleTest;
import com.revolsys.geometry.test.old.geom.LineStringImplTest;
import com.revolsys.geometry.test.old.geom.MultiPointImplTest;
import com.revolsys.geometry.test.old.geom.NormalizeTest;
import com.revolsys.geometry.test.old.geom.PointImplTest;
import com.revolsys.geometry.test.old.geom.PredicateShortCircuitTest;
import com.revolsys.geometry.test.old.geom.RectanglePredicateSyntheticTest;
import com.revolsys.geometry.test.old.geom.RectanglePredicateTest;
import com.revolsys.geometry.test.old.index.IntervalTest;
import com.revolsys.geometry.test.old.index.QuadtreeTest;
import com.revolsys.geometry.test.old.index.SIRtreeTest;
import com.revolsys.geometry.test.old.index.STRtreeTest;
import com.revolsys.geometry.test.old.io.WKBTest;
import com.revolsys.geometry.test.old.io.WKTReaderTest;
import com.revolsys.geometry.test.old.linearref.LengthIndexedLineTest;
import com.revolsys.geometry.test.old.linearref.LocationIndexedLineTest;
import com.revolsys.geometry.test.old.operation.CascadedPolygonUnionTest;
import com.revolsys.geometry.test.old.operation.DistanceTest;
import com.revolsys.geometry.test.old.operation.IsValidTest;
import com.revolsys.geometry.test.old.operation.LineMergerTest;
import com.revolsys.geometry.test.old.operation.PolygonizeTest;
import com.revolsys.geometry.test.old.operation.RelateBoundaryNodeRuleTest;
import com.revolsys.geometry.test.old.operation.UnaryUnionTest;
import com.revolsys.geometry.test.old.operation.ValidClosedRingTest;
import com.revolsys.geometry.test.old.operation.ValidSelfTouchingRingFormingHoleTest;
import com.revolsys.geometry.test.old.triangulate.ConformingDelaunayTest;
import com.revolsys.geometry.test.old.triangulate.DelaunayTest;

import junit.framework.TestCase;

/**
 * A collection of all the tests.
 *
 * @version 1.7
 */
@RunWith(Suite.class)
@SuiteClasses({
  AngleTest.class, AreaLengthTest.class, CascadedPolygonUnionTest.class, OrientationIndexTest.class,
  ConformingDelaunayTest.class, ConvexHullTest.class, CoordinateArraysTest.class,
  DelaunayTest.class, DistanceTest.class, GeometryCollectionImplTest.class, GeometryImplTest.class,
  IntersectionMatrixTest.class, IntervalTest.class, IsCounterClockWiseTest.class,
  IsRectangleTest.class, IsValidTest.class, LengthIndexedLineTest.class, LineMergerTest.class,
  LineStringImplTest.class, LocationIndexedLineTest.class, MiscellaneousTest.class,
  MiscellaneousTest2.class, MultiPointImplTest.class, NonRobustLineIntersectorTest.class,
  NormalizeTest.class, PointImplTest.class, PolygonizeTest.class, PredicateShortCircuitTest.class,
  QuadtreeTest.class, RectanglePredicateSyntheticTest.class, RectanglePredicateTest.class,
  RelateBoundaryNodeRuleTest.class, RobustLineIntersectionTest.class, SimpleTest.class,
  SIRtreeTest.class, STRtreeTest.class, WKTReaderTest.class, WKBTest.class, UnaryUnionTest.class,
  ValidClosedRingTest.class, ValidSelfTouchingRingFormingHoleTest.class, InteriorPointTest.class
})
public class MasterTester extends TestCase {
}
