package com.revolsys.elevation.gridded.compactbinary;

import java.io.DataInputStream;
import java.io.IOException;

import com.revolsys.elevation.gridded.FloatArrayGriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public final class ShortReader extends CompactBinaryGriddedElevationReader {
  public ShortReader(final Resource resource) {
    super(resource);
  }

  @Override
  public String getFileExtension() {
    return "demcs";
  }

  @Override
  public boolean isFloatingPoint() {
    return true;
  }

  @Override
  protected GriddedElevationModel newGriddedElevationModel(final DataInputStream in,
    final GeometryFactory geometryFactory, final double minX, final double minY,
    final int gridCellSize, final int gridWidth, final int gridHeight) {
    try {
      final FloatArrayGriddedElevationModel elevationModel = new FloatArrayGriddedElevationModel(
        geometryFactory, minX, minY, gridWidth, gridHeight, gridCellSize);
      elevationModel.setResource(this.resource);
      for (int gridY = 0; gridY < gridHeight; gridY++) {
        for (int gridX = 0; gridX < gridWidth; gridX++) {
          final short elevation = in.readShort();
          if (elevation == Short.MIN_VALUE) {
            elevationModel.setElevationNull(gridX, gridY);
          } else {
            elevationModel.setElevation(gridX, gridY, elevation);
          }
        }
      }
      return elevationModel;
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read DEM: " + this.resource, e);
    }
  }
}
