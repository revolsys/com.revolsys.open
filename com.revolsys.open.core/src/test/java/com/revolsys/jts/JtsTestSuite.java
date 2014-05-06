package com.revolsys.jts;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.revolsys.jts.test.geometry.GeometrySuite;
import com.revolsys.jts.test.geometry.operation.OperationTests;
import com.revolsys.jts.testold.junit.MasterTester;
import com.revolsys.jtstest.testrunner.TopologyTest;

@RunWith(Suite.class)
@SuiteClasses({
  GeometrySuite.class, OperationTests.class, MasterTester.class,
  TopologyTest.class
})
public class JtsTestSuite {

}
