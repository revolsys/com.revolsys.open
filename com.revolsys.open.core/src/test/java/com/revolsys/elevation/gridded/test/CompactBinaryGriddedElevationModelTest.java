package com.revolsys.elevation.gridded.test;

import java.util.Arrays;
import java.util.List;

import com.revolsys.elevation.gridded.compactbinary.CompactBinaryGriddedElevation;

public class CompactBinaryGriddedElevationModelTest extends GriddedElevationModelTest {

  @Override
  public List<String> getFileExtensions() {
    return Arrays.asList(CompactBinaryGriddedElevation.FILE_EXTENSION,
      CompactBinaryGriddedElevation.FILE_EXTENSION_GZ,
      CompactBinaryGriddedElevation.FILE_EXTENSION_ZIP);
  }
}
