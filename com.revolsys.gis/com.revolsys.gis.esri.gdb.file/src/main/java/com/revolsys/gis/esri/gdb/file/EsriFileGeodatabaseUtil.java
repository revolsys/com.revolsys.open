package com.revolsys.gis.esri.gdb.file;

import com.revolsys.gis.esri.gdb.file.swig.EsriFileGdb;

public class EsriFileGeodatabaseUtil {
  public static final void check(int result) {
    if (result != 0) {
      throw new RuntimeException(EsriFileGdb.getErrorDescription(result));
    }
  }
}
