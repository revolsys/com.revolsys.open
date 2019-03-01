package com.revolsys.elevation.cloud.las.zip.compress;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;

public interface LasWriteItem {
  void write(LasPoint point, int context);
}
