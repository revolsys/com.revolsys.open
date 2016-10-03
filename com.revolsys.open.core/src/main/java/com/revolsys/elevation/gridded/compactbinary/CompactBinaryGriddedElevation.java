package com.revolsys.elevation.gridded.compactbinary;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.function.Function;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReadFactory;
import com.revolsys.elevation.gridded.GriddedElevationModelWriter;
import com.revolsys.elevation.gridded.GriddedElevationModelWriterFactory;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.editor.GeometryEditor;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.gis.grid.CustomRectangularMapGrid;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class CompactBinaryGriddedElevation extends AbstractIoFactoryWithCoordinateSystem
  implements GriddedElevationModelReadFactory, GriddedElevationModelWriterFactory {
  public static int HEADER_SIZE = 60;

  public static double getElevationInterpolated(final Resource baseResource,
    final int coordinateSystemId, final int gridCellSize, final int gridSize,
    final String fileExtension, final double x, final double y) {

    final int tileSize = gridSize * gridCellSize;
    final int tileX = CustomRectangularMapGrid.getGridFloor(0.0, tileSize, x);
    final int tileY = CustomRectangularMapGrid.getGridFloor(0.0, tileSize, y);

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

  public static double getElevationNearest(final Resource baseResource,
    final int coordinateSystemId, final int gridCellSize, final int gridSize,
    final String fileExtension, final double x, final double y) {

    final int tileSize = gridSize * gridCellSize;
    final int tileX = CustomRectangularMapGrid.getGridFloor(0.0, tileSize, x);
    final int tileY = CustomRectangularMapGrid.getGridFloor(0.0, tileSize, y);

    final Resource resource = RectangularMapGrid.getTileResource(baseResource, coordinateSystemId,
      gridCellSize, tileX, tileY, fileExtension);
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
          FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
          if (factory.isFloatingPoint()) {
            final ByteBuffer bytes = ByteBuffer.allocate(4);
            channel.read(bytes, offset);
            elevation = bytes.getFloat(0);
          } else {
            final ByteBuffer bytes = ByteBuffer.allocate(2);
            channel.read(bytes, offset);
            elevation = bytes.getShort(0);
          }
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
        }
      }
      return elevation;
    } catch (final NoSuchFileException e) {
      return Double.NaN;
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read: " + resource, e);
    } catch (final ClassCastException e) {
      throw new IllegalArgumentException(fileExtension + " not supported");
    }
  }

  public static void ioFactoryInit() {
    new CompactBinaryGriddedElevation("DEM Compact Short Values", "image/x-rs-compact-binary-dem",
      "demcs", false, ShortReader::new);
    new CompactBinaryGriddedElevation("DEM Compact Float Values", "image/x-rs-compact-binary-dem",
      "demcf", true, FloatReader::new);
  }

  @SuppressWarnings("unchecked")
  public static <G extends Geometry> G setElevationNearest(final PathResource baseResource,
    final int coordinateSystemId, final int gridCellSize, final int gridSize,
    final String fileExtension, final G geometry) {
    final GeometryEditor editor = geometry.newGeometryEditor();
    editor.setAxisCount(3);
    for (final Vertex vertex : geometry.vertices()) {
      final double x = vertex.getX();
      final double y = vertex.getY();
      final double elevation = getElevationNearest(baseResource, coordinateSystemId, gridCellSize,
        gridSize, fileExtension, x, y);
      if (!Double.isNaN(elevation)) {
        final int[] vertexId = vertex.getVertexId();
        editor.setZ(elevation, vertexId);
      }
    }
    return (G)editor.newGeometry();
  }

  private final Function<Resource, CompactBinaryGriddedElevationReader> readerFactory;

  private final boolean floatingPoint;

  private final String fileExtension;

  public CompactBinaryGriddedElevation(final String description, final String mimeType,
    final String fileExtension, final boolean floatingPoint,
    final Function<Resource, CompactBinaryGriddedElevationReader> readerFactory) {
    super(description);
    this.fileExtension = fileExtension;
    this.floatingPoint = floatingPoint;
    this.readerFactory = readerFactory;
    addMediaTypeAndFileExtension(mimeType, fileExtension);
    IoFactoryRegistry.addFactory(this);
  }

  public String getFileExtension() {
    return this.fileExtension;
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
      CompactBinaryGriddedElevationReader reader = this.readerFactory.apply(resource)) {
      reader.setProperties(properties);
      return reader.read();
    }
  }

  @Override
  public GriddedElevationModelWriter newGriddedElevationModelWriter(final Resource resource) {
    return new CompactBinaryGriddedElevationModelWriter(resource, this.floatingPoint);
  }
}
