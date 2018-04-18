package com.revolsys.elevation.gridded;

import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.io.BaseCloseable;
import com.revolsys.properties.ObjectWithProperties;

public interface GriddedElevationModelReader
  extends BaseCloseable, BoundingBoxProxy, ObjectWithProperties {
  @Override
  default void close() {
    ObjectWithProperties.super.close();
  }

  double getGridCellHeight();

  double getGridCellWidth();

  GriddedElevationModel read();
}
