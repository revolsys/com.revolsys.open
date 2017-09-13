package com.revolsys.elevation.gridded.rasterizer;

import java.awt.image.DataBuffer;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.properties.ObjectWithProperties;

public interface GriddedElevationModelRasterizer
  extends BoundingBoxProxy, PropertyChangeSupportProxy, ObjectWithProperties {

  GriddedElevationModel getElevationModel();

  int getHeight();

  String getName();

  default int getValue(final int index) {
    final int width = getWidth();
    final int height = getHeight();
    final int gridX = index % width;
    final int gridY = height - 1 - (index - gridX) / width;
    return getValue(gridX, gridY);
  }

  int getValue(final int gridX, int gridY);

  int getWidth();

  default void rasterize(final DataBuffer imageBuffer) {
    final int width = getWidth();
    final int height = getHeight();
    int index = 0;
    for (int gridY = height - 1; gridY >= 0; gridY--) {
      for (int gridX = 0; gridX < width; gridX++) {
        final int hillShade = getValue(gridX, gridY);
        imageBuffer.setElem(index, hillShade);
        index++;
      }
    }
  }

  void setElevationModel(GriddedElevationModel elevationModel);
}
