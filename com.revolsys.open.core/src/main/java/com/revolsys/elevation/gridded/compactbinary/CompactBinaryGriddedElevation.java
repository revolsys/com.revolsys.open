package com.revolsys.elevation.gridded.compactbinary;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import com.revolsys.elevation.gridded.FloatArrayGriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReadFactory;
import com.revolsys.elevation.gridded.GriddedElevationModelWriter;
import com.revolsys.elevation.gridded.GriddedElevationModelWriterFactory;
import com.revolsys.elevation.gridded.ShortArrayGriddedElevationModel;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.grid.CustomRectangularMapGrid;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class CompactBinaryGriddedElevation extends AbstractIoFactoryWithCoordinateSystem
  implements GriddedElevationModelReadFactory, GriddedElevationModelWriterFactory {
  private static int HEADER_SIZE = 60;

  public static double getElevationNearest(final Resource baseResource,
    final int coordinateSystemId, final int gridCellSize, final int gridSize,
    final String fileExtension, final double x, final double y) {

    final int tileX = CustomRectangularMapGrid.getGridFloor(0.0, gridSize, x);
    final int tileY = CustomRectangularMapGrid.getGridFloor(0.0, gridSize, y);

    final Resource resource = RectangularMapGrid.getTileResource(baseResource, coordinateSystemId,
      gridCellSize, tileX, tileY, fileExtension);
    if (resource.exists()) {
      try {
        final CompactBinaryGriddedElevation factory = (CompactBinaryGriddedElevation)IoFactory
          .factoryByFileExtension(GriddedElevationModelReadFactory.class, fileExtension);
        final int gridCellX = GriddedElevationModel.getGridCellX(tileX, gridCellSize, x);
        final int gridCellY = GriddedElevationModel.getGridCellY(tileY, gridCellSize, y);
        int elevationByteSize;
        if (factory.isFloatingPoint()) {
          elevationByteSize = 4;
        } else {
          elevationByteSize = 2;
        }
        final int offset = HEADER_SIZE + (gridCellY * gridSize + gridCellX) * elevationByteSize;
        double elevation;
        if (resource.isFile()) {
          final Path path = resource.toPath();
          try (
            SeekableByteChannel byteChannel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            byteChannel.position(offset);
            if (factory.isFloatingPoint()) {
              final ByteBuffer bytes = ByteBuffer.allocate(4);
              byteChannel.read(bytes);
              elevation = bytes.getFloat(0);
            } else {
              final ByteBuffer bytes = ByteBuffer.allocate(2);
              byteChannel.read(bytes);
              elevation = bytes.getShort(0);
            }
          } catch (final IOException e) {
            throw Exceptions.wrap("Unable to read: " + resource, e);
          }
        } else {
          try (
            DataInputStream in = resource.newBufferedInputStream(DataInputStream::new)) {
            in.skip(offset);
            if (factory.isFloatingPoint()) {
              elevation = in.readFloat();
            } else {
              elevation = in.readShort();
            }
          } catch (final IOException e) {
            throw Exceptions.wrap("Unable to read: " + resource, e);
          }
        }
        return elevation;
      } catch (final ClassCastException e) {
        throw new IllegalArgumentException(fileExtension + " not supported");
      }
    }
    return Double.NaN;
  }

  public static void ioFactoryInit() {
    new CompactBinaryGriddedElevation("DEM Compact Short Values", "image/x-rs-compact-binary-dem",
      "demcs", false);
    new CompactBinaryGriddedElevation("DEM Compact Float Values", "image/x-rs-compact-binary-dem",
      "demcf", false);
  }

  private final boolean floatingPoint;

  public CompactBinaryGriddedElevation(final String description, final String mimeType,
    final String fileExtension, final boolean floatingPoint) {
    super(description);
    this.floatingPoint = floatingPoint;
    addMediaTypeAndFileExtension(mimeType, fileExtension);
    IoFactoryRegistry.addFactory(this);
  }

  public boolean isFloatingPoint() {
    return this.floatingPoint;
  }

  @Override
  public boolean isReadFromZipFileSupported() {
    return true;
  }

  @Override
  public GriddedElevationModel newGriddedElevationModel(final Resource resource,
    final Map<String, ? extends Object> properties) {
    try (
      DataInputStream in = resource.newBufferedInputStream(DataInputStream::new)) {
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
      if (this.floatingPoint) {
        final FloatArrayGriddedElevationModel elevationModel = new FloatArrayGriddedElevationModel(
          geometryFactory, minX, minY, gridWidth, gridHeight, gridCellSize);
        elevationModel.setResource(resource);
        for (int gridY = 0; gridY < gridHeight; gridY++) {
          for (int gridX = 0; gridX < gridWidth; gridX++) {
            final float elevation = in.readFloat();
            if (Float.isNaN(elevation)) {
              elevationModel.setElevationNull(gridX, gridY);
            } else {
              elevationModel.setElevation(gridX, gridY, elevation);
            }
          }
        }
        return elevationModel;
      } else {
        final ShortArrayGriddedElevationModel elevationModel = new ShortArrayGriddedElevationModel(
          geometryFactory, minX, minY, gridWidth, gridHeight, gridCellSize);
        elevationModel.setResource(resource);
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
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read: " + resource, e);
    }
  }

  @Override
  public GriddedElevationModelWriter newGriddedElevationModelWriter(final Resource resource) {
    return new CompactBinaryGriddedElevationModelWriter(resource, this.floatingPoint);
  }
}
