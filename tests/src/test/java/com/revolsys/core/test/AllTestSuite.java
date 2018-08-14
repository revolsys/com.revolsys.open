package com.revolsys.core.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.revolsys.core.test.elevation.test.ElevationTestSuite;
import com.revolsys.core.test.geometry.test.GeometryTestSuite;
import com.revolsys.core.test.record.io.test.RecordIoTestSuite;

@RunWith(Suite.class)
@SuiteClasses({
  GeometryTestSuite.class, //
  ElevationTestSuite.class, //
  RecordIoTestSuite.class
})
public class AllTestSuite {
}
