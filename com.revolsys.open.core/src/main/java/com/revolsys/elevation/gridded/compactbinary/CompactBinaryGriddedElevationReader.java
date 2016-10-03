package com.revolsys.elevation.gridded.compactbinary;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public abstract class CompactBinaryGriddedElevationReader extends BaseObjectWithProperties
  implements Closeable {

  public static int HEADER_SIZE = 60;

  protected Resource resource;

  CompactBinaryGriddedElevationReader(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void close() {
    super.close();
    this.resource = null;
  }

  public abstract String getFileExtension();

  public abstract boolean isFloatingPoint();

  protected abstract GriddedElevationModel newGriddedElevationModel(final DataInputStream in,
    final GeometryFactory geometryFactory, final double minX, final double minY,
    final int gridCellSize, final int gridWidth, final int gridHeight);

  public GriddedElevationModel read() {
    try (
      DataInputStream in = this.resource.newBufferedInputStream(DataInputStream::new)) {
      @SuppressWarnings("unused")
      final String fileType = FileUtil.readString(in, 6); // File type
      @SuppressWarnings("unused")
      final String version = FileUtil.readString(in, 8); // version
      final int coordinateSystemId = in.readInt(); // Coordinate System ID
      final double minX = in.readDouble(); // minX
      final double minY = in.readDouble(); // maxX
      final int gridCellSize = in.readInt(); // Grid Cell Size
      final int gridWidth = in.readInt(); // Grid Width
      final int gridHeight = in.readInt(); // Grid Height
      final GeometryFactory geometryFactory = GeometryFactory.floating(coordinateSystemId, 2);
      return newGriddedElevationModel(in, geometryFactory, minX, minY, gridCellSize, gridWidth,
        gridHeight);

    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read: " + this.resource, e);
    }
  }
}
