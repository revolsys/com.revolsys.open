package com.revolsys.elevation.gridded.compactbinary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;

import com.revolsys.elevation.gridded.DirectFileElevationModel;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.revolsys.io.file.Paths;
import com.revolsys.util.Exceptions;

public class CompactBinaryGriddedElevationModelFile extends DirectFileElevationModel {
  public static CompactBinaryGriddedElevationModelFile newModel(final Path basePath,
    final GeometryFactory geometryFactory, final int minX, final int minY, final int gridWidth,
    final int gridHeight, final int gridCellSize) {

    final Path rowDirectory = basePath.resolve(Integer.toString(minX));
    final int coordinateSystemId = geometryFactory.getCoordinateSystemId();
    final String fileName = RectangularMapGrid.getTileFileName("dem", coordinateSystemId,
      Integer.toString(gridCellSize * gridWidth), minX, minY, "demcs");
    final Path path = rowDirectory.resolve(fileName);
    return new CompactBinaryGriddedElevationModelFile(path, geometryFactory, minX, minY, gridWidth,
      gridHeight, gridCellSize);
  }

  private final Path path;

  private FileChannel fileChannel;

  private final Set<OpenOption> openOptions = Paths.OPEN_OPTIONS_WRITE_SET;

  private final FileAttribute<?>[] fileAttributes = Paths.FILE_ATTRIBUTES_NONE;

  private final ByteBuffer buffer = ByteBuffer.allocateDirect(4);

  private double scaleZ;

  public CompactBinaryGriddedElevationModelFile(final Path path,
    final GeometryFactory geometryFactory, final int minX, final int minY, final int gridWidth,
    final int gridHeight, final int gridCellSize) {
    super(geometryFactory, minX, minY, gridWidth, gridHeight, gridCellSize, 2);
    this.path = path;

    this.scaleZ = geometryFactory.getScaleZ();
    if (this.scaleZ <= 0) {
      this.scaleZ = 1000;
    }
  }

  @Override
  public void close() {
    super.close();
    final FileChannel fileChannel = this.fileChannel;
    this.fileChannel = null;
    if (fileChannel != null) {
      try {
        fileChannel.close();
      } catch (final IOException e) {
      }
    }
  }

  private FileChannel getFileChannel() throws IOException {

    if (this.fileChannel == null && isOpen()) {
      this.fileChannel = FileChannel.open(this.path, this.openOptions, this.fileAttributes);
      if (!isOpen()) {
        close();
        return null;
      }
    }
    return this.fileChannel;
  }

  @Override
  protected synchronized double readElevation(final int offset) {
    try {
      final FileChannel fileChannel = getFileChannel();
      if (fileChannel == null) {
        return Double.NaN;
      } else {
        while (this.buffer.hasRemaining()) {
          if (fileChannel.read(this.buffer, offset) == -1) {
            return Double.NaN;
          }
        }
        final int elevationInt = this.buffer.getInt();
        if (elevationInt == Integer.MIN_VALUE) {
          return Double.NaN;
        } else {
          return elevationInt / this.scaleZ;
        }
      }
    } catch (final NoSuchFileException e) {
      return Double.NaN;
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read: " + this.path, e);
    } finally {
      this.buffer.clear();
    }
  }

  @Override
  protected synchronized void writeElevation(final int offset, final double elevation) {
    try {
      final FileChannel fileChannel = getFileChannel();
      if (fileChannel != null) {
        final double scale = this.scaleZ;
        if (Double.isFinite(elevation)) {
          final int elevationInt = (int)Math.round(elevation * scale);
          this.buffer.putInt(elevationInt);
        } else {
          this.buffer.putInt(Integer.MIN_VALUE);
        }
        this.buffer.flip();
        while (this.buffer.hasRemaining()) {
          fileChannel.write(this.buffer);
        }
        this.buffer.clear();
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read: " + this.path, e);
    }
  }
}
