package com.revolsys.geometry.test.editor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  PointEditorTest.class, LineStringEditorTest.class, PolygonEditorTest.class,
  MultiPointEditorTest.class, MultiLineStringEditorTest.class, MultiPolygonEditorTest.class
})
public class GeometryEditorTest {

}
