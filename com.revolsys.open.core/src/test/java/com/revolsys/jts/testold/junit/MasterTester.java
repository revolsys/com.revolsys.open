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

package com.revolsys.jts.testold.junit;

import junit.framework.TestCase;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.revolsys.jts.testold.algorithm.AngleTest;
import com.revolsys.jts.testold.algorithm.ConvexHullTest;
import com.revolsys.jts.testold.algorithm.InteriorPointTest;
import com.revolsys.jts.testold.algorithm.IsCounterClockWiseTest;
import com.revolsys.jts.testold.algorithm.NonRobustLineIntersectorTest;
import com.revolsys.jts.testold.algorithm.OrientationIndexTest;
import com.revolsys.jts.testold.algorithm.RobustLineIntersectionTest;
import com.revolsys.jts.testold.geom.AreaLengthTest;
import com.revolsys.jts.testold.geom.BasicCoordinateSequenceTest;
import com.revolsys.jts.testold.geom.CoordinateArraysTest;
import com.revolsys.jts.testold.geom.GeometryCollectionImplTest;
import com.revolsys.jts.testold.geom.GeometryImplTest;
import com.revolsys.jts.testold.geom.IntersectionMatrixTest;
import com.revolsys.jts.testold.geom.IsRectangleTest;
import com.revolsys.jts.testold.geom.LineStringImplTest;
import com.revolsys.jts.testold.geom.MultiPointImplTest;
import com.revolsys.jts.testold.geom.NormalizeTest;
import com.revolsys.jts.testold.geom.PointImplTest;
import com.revolsys.jts.testold.geom.PrecisionModelTest;
import com.revolsys.jts.testold.geom.PredicateShortCircuitTest;
import com.revolsys.jts.testold.geom.RectanglePredicateSyntheticTest;
import com.revolsys.jts.testold.geom.RectanglePredicateTest;
import com.revolsys.jts.testold.index.IntervalTest;
import com.revolsys.jts.testold.index.QuadtreeTest;
import com.revolsys.jts.testold.index.SIRtreeTest;
import com.revolsys.jts.testold.index.STRtreeTest;
import com.revolsys.jts.testold.io.WKBTest;
import com.revolsys.jts.testold.io.WKTReaderTest;
import com.revolsys.jts.testold.linearref.LengthIndexedLineTest;
import com.revolsys.jts.testold.linearref.LocationIndexedLineTest;
import com.revolsys.jts.testold.operation.CascadedPolygonUnionTest;
import com.revolsys.jts.testold.operation.DistanceTest;
import com.revolsys.jts.testold.operation.IsValidTest;
import com.revolsys.jts.testold.operation.LineMergerTest;
import com.revolsys.jts.testold.operation.PolygonizeTest;
import com.revolsys.jts.testold.operation.RelateBoundaryNodeRuleTest;
import com.revolsys.jts.testold.operation.UnaryUnionTest;
import com.revolsys.jts.testold.operation.ValidClosedRingTest;
import com.revolsys.jts.testold.operation.ValidSelfTouchingRingFormingHoleTest;
import com.revolsys.jts.testold.triangulate.ConformingDelaunayTest;
import com.revolsys.jts.testold.triangulate.DelaunayTest;

/**
 * A collection of all the tests.
 *
 * @version 1.7
 */
@RunWith(Suite.class)
@SuiteClasses({
  AngleTest.class, AreaLengthTest.class, BasicCoordinateSequenceTest.class,
  CascadedPolygonUnionTest.class, OrientationIndexTest.class,
  ConformingDelaunayTest.class, ConvexHullTest.class,
  CoordinateArraysTest.class, DelaunayTest.class, DistanceTest.class,
  GeometryCollectionImplTest.class, GeometryImplTest.class,
  IntersectionMatrixTest.class, IntervalTest.class,
  IsCounterClockWiseTest.class, IsRectangleTest.class, IsValidTest.class,
  LengthIndexedLineTest.class, LineMergerTest.class, LineStringImplTest.class,
  LocationIndexedLineTest.class, MiscellaneousTest.class,
  MiscellaneousTest2.class, MultiPointImplTest.class,
  NonRobustLineIntersectorTest.class, NormalizeTest.class, PointImplTest.class,
  PolygonizeTest.class, PredicateShortCircuitTest.class,
  PrecisionModelTest.class, QuadtreeTest.class,
  RectanglePredicateSyntheticTest.class, RectanglePredicateTest.class,
  RelateBoundaryNodeRuleTest.class, RobustLineIntersectionTest.class,
  SimpleTest.class, SIRtreeTest.class, STRtreeTest.class, WKTReaderTest.class,
  WKBTest.class, UnaryUnionTest.class, ValidClosedRingTest.class,
  ValidSelfTouchingRingFormingHoleTest.class, InteriorPointTest.class
})
public class MasterTester extends TestCase {
}
