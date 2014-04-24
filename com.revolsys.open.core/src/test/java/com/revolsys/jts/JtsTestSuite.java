package com.revolsys.jts;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.revolsys.jts.test.geometry.GeometrySuite;
import com.revolsys.jts.testold.junit.MasterTester;

@RunWith(Suite.class)
@SuiteClasses({
  GeometrySuite.class, MasterTester.class
})
public class JtsTestSuite {

}
