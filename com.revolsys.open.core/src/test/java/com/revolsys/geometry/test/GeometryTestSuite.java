package com.revolsys.geometry.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.revolsys.geometry.test.model.GeometrySuite;
import com.revolsys.geometry.test.model.operation.OperationTests;
import com.revolsys.geometry.test.old.junit.MasterTester;
import com.revolsys.geometry.test.testrunner.TopologyTest;

@RunWith(Suite.class)
@SuiteClasses({
  GeometrySuite.class, //
  OperationTests.class, //
  MasterTester.class, //
  TopologyTest.class
})
public class GeometryTestSuite {
}
