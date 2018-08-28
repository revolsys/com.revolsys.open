package com.revolsys.core.test.geometry.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.revolsys.core.test.geometry.cs.CoordinateSystemsTest;
import com.revolsys.core.test.geometry.test.editor.GeometryEditorSuite;
import com.revolsys.core.test.geometry.test.model.GeometrySuite;
import com.revolsys.core.test.geometry.test.model.operation.OperationTests;
import com.revolsys.core.test.geometry.test.old.index.TreeTestSuite;
import com.revolsys.core.test.geometry.test.old.junit.MasterTester;
import com.revolsys.core.test.geometry.test.testrunner.TopologyTest;

@RunWith(Suite.class)
@SuiteClasses({
  CoordinateSystemsTest.class, //
  GeometrySuite.class, //
  GeometryEditorSuite.class, //
  OperationTests.class, //
  MasterTester.class, //
  TopologyTest.class, //
  TreeTestSuite.class
})
public class GeometryTestSuite {
}
