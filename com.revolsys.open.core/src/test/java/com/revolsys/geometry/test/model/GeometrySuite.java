package com.revolsys.geometry.test.model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.revolsys.geometry.test.model.operation.ClipRectangleTest;
import com.revolsys.gis.model.LineSegmentTest;

@RunWith(Suite.class)
@SuiteClasses({
  CoordinatesTest.class, BoundingBoxTest.class, PointTest.class, LineStringTest.class,
  PolygonTest.class, MultiPointTest.class, MultiLineStringTest.class, MultiPolygonTest.class,
  LineSegmentTest.class, ClipRectangleTest.class, RectangleTest.class
})
public class GeometrySuite {
}
