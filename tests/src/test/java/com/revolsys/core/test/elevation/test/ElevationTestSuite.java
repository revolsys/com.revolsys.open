package com.revolsys.core.test.elevation.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.revolsys.core.test.elevation.gridded.test.EsriAsciiGriddedElevationModelTest;
import com.revolsys.core.test.elevation.gridded.test.ScaledIntegerGriddedDigitalElevationModelTest;

@RunWith(Suite.class)
@SuiteClasses({
  ScaledIntegerGriddedDigitalElevationModelTest.class, //
  EsriAsciiGriddedElevationModelTest.class //
})
public class ElevationTestSuite {

}
