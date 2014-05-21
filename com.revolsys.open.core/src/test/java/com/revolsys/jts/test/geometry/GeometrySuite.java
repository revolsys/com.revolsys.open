package com.revolsys.jts.test.geometry;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.revolsys.gis.model.LineSegmentTest;

@RunWith(Suite.class)
@SuiteClasses({
  CoordinateTest.class, BoundingBoxTest.class, PointTest.class,
  LineStringTest.class, PolygonTest.class, MultiPointTest.class,
  MultiLineStringTest.class, MultiPolygonTest.class, LineSegmentTest.class
})
public class GeometrySuite {
}
