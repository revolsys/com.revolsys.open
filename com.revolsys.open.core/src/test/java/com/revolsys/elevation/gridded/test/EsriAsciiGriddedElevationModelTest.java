package com.revolsys.elevation.gridded.test;

import java.util.Arrays;
import java.util.List;

import com.revolsys.elevation.gridded.esriascii.EsriAsciiGriddedElevation;

public class EsriAsciiGriddedElevationModelTest extends GriddedElevationModelTest {

  @Override
  public List<String> getFileExtensions() {
    return Arrays.asList(EsriAsciiGriddedElevation.FILE_EXTENSION,
      EsriAsciiGriddedElevation.FILE_EXTENSION_GZ, EsriAsciiGriddedElevation.FILE_EXTENSION_ZIP);
  }

}
