package com.revolsys.core.test.geometry.test.model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.revolsys.core.test.gis.model.LineSegmentTest;

@RunWith(Suite.class)
@SuiteClasses({
  CoordinatesTest.class, //
  BoundingBoxTest.class, //
  PointTest.class, //
  LineStringTest.class, //
  PolygonTest.class, //
  MultiPointTest.class, //
  MultiLineStringTest.class, //
  MultiPolygonTest.class, //
  LineSegmentTest.class, //
  RectangleTest.class
})
public class GeometrySuite {
}
