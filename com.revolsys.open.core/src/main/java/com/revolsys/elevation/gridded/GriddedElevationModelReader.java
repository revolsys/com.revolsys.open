package com.revolsys.elevation.gridded;

import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.IoFactory;
import com.revolsys.properties.ObjectWithProperties;

public interface GriddedElevationModelReader
  extends BaseCloseable, BoundingBoxProxy, ObjectWithProperties {
  static boolean isReadable(final Object source) {
    return IoFactory.isAvailable(GriddedElevationModelReadFactory.class, source);
  }

  @Override
  default void close() {
    ObjectWithProperties.super.close();
  }

  double getGridCellHeight();

  double getGridCellWidth();

  GriddedElevationModel read();
}
