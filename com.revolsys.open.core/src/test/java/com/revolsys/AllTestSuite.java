package com.revolsys;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.revolsys.elevation.test.ElevationTestSuite;
import com.revolsys.geometry.test.GeometryTestSuite;
import com.revolsys.record.io.test.RecordIoTestSuite;

@RunWith(Suite.class)
@SuiteClasses({
  GeometryTestSuite.class, //
  ElevationTestSuite.class, //
  RecordIoTestSuite.class
})
public class AllTestSuite {
}
