package com.revolsys.elevation.gridded.compactbinary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.revolsys.util.Exceptions;

public class ShortPathElevationModel extends DirectFileElevationModel {
  private final Path path;

  public ShortPathElevationModel(final Path basePath, final GeometryFactory geometryFactory,
    final int minX, final int minY, final int gridWidth, final int gridHeight,
    final int gridCellSize) {
    super(geometryFactory, minX, minY, gridWidth, gridHeight, gridCellSize, 2);

    final Path rowDirectory = basePath.resolve(Integer.toString(minX));
    final int coordinateSystemId = geometryFactory.getCoordinateSystemId();
    final String fileName = RectangularMapGrid.getTileFileName("dem", coordinateSystemId,
      Integer.toString(gridCellSize * gridWidth), minX, minY, "demcs");
    this.path = rowDirectory.resolve(fileName);
  }

  @Override
  protected double readElevation(final int offset) {
    try {
      try (
        SeekableByteChannel byteChannel = Files.newByteChannel(this.path,
          StandardOpenOption.READ)) {
        byteChannel.position(offset);
        final ByteBuffer bytes = ByteBuffer.allocate(2);
        byteChannel.read(bytes);
        return bytes.getShort(0);
      }
    } catch (final NoSuchFileException e) {
      return Double.NaN;
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read: " + this.path, e);
    }
  }
}
