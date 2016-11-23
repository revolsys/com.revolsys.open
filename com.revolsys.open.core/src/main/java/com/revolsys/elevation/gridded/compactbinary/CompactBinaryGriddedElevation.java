package com.revolsys.elevation.gridded.compactbinary;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

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

  public static final String FILE_FORMAT = "DEMGCB";

  public static final byte[] FILE_FORMAT_BYTES = "DEMGCB".getBytes(StandardCharsets.UTF_8);

  public static final short VERSION = 1;

  public static final int HEADER_SIZE = 88;

  public static double getElevationInterpolated(final Resource baseResource,
    final int coordinateSystemId, final int gridCellSize, final int gridSize,
    final String fileExtension, final double x, final double y) {

    final int gridTileSize = gridSize * gridCellSize;
    final int tileX = CustomRectangularMapGrid.getGridFloor(0.0, gridTileSize, x);
    final int tileY = CustomRectangularMapGrid.getGridFloor(0.0, gridTileSize, y);

    final Resource resource = RectangularMapGrid.getTileResource(baseResource, "dem",
      coordinateSystemId, Integer.toString(gridTileSize), tileX, tileY, fileExtension);
    if (resource.exists()) {
      try {
        final CompactBinaryGriddedElevation factory = (CompactBinaryGriddedElevation)IoFactory
          .factoryByFileExtension(GriddedElevationModelReadFactory.class, fileExtension);
        final int gridCellX = GriddedElevationModel.getGridCellX(tileX, gridCellSize, x);
        final int gridCellY = GriddedElevationModel.getGridCellY(tileY, gridCellSize, y);
        final int elevationByteSize = 4;
        final int offset = HEADER_SIZE + (gridCellY * gridSize + gridCellX) * elevationByteSize;
        int elevation;
        if (resource.isFile()) {
          final Path path = resource.toPath();
          try (
            SeekableByteChannel byteChannel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            byteChannel.position(offset);
            final ByteBuffer bytes = ByteBuffer.allocate(4);
            byteChannel.read(bytes);
            elevation = bytes.getInt(0);
          } catch (final IOException e) {
            throw Exceptions.wrap("Unable to read: " + resource, e);
          }
        } else {
          try (
            DataInputStream in = resource.newBufferedInputStream(DataInputStream::new)) {
            in.skip(offset);
            elevation = in.readInt();
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

    final int gridTileSize = gridSize * gridCellSize;
    final int tileX = CustomRectangularMapGrid.getGridFloor(0.0, gridTileSize, x);
    final int tileY = CustomRectangularMapGrid.getGridFloor(0.0, gridTileSize, y);

    final Resource resource = RectangularMapGrid.getTileResource(baseResource, "dem",
      coordinateSystemId, Integer.toString(gridTileSize), tileX, tileY, fileExtension);
    try {
      final CompactBinaryGriddedElevation factory = (CompactBinaryGriddedElevation)IoFactory
        .factoryByFileExtension(GriddedElevationModelReadFactory.class, fileExtension);
      final int gridCellX = GriddedElevationModel.getGridCellX(tileX, gridCellSize, x);
      final int gridCellY = GriddedElevationModel.getGridCellY(tileY, gridCellSize, y);
      final int elevationByteSize = 4;
      final int offset = HEADER_SIZE + (gridCellY * gridSize + gridCellX) * elevationByteSize;
      int elevation;
      if (resource.isFile()) {
        final Path path = resource.toPath();
        try (
          FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
          final ByteBuffer bytes = ByteBuffer.allocate(4);
          channel.read(bytes, offset);
          elevation = bytes.getInt(0);
        }
      } else {
        try (
          DataInputStream in = resource.newBufferedInputStream(DataInputStream::new)) {
          in.skip(offset);
          elevation = in.readInt();
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
    new CompactBinaryGriddedElevation();
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

  public CompactBinaryGriddedElevation() {
    super("DEM Compact Binary");

    addMediaTypeAndFileExtension("image/x-rs-compact-binary-dem", "demcb");
    IoFactoryRegistry.addFactory(this);
  }

  @Override
  public boolean isReadFromZipFileSupported() {
    return true;
  }

  @Override
  public GriddedElevationModel newGriddedElevationModel(final Resource resource,
    final Map<String, ? extends Object> properties) {
    try (
      CompactBinaryGriddedElevationReader reader = new CompactBinaryGriddedElevationReader(
        resource)) {
      reader.setProperties(properties);
      return reader.read();
    }
  }

  @Override
  public GriddedElevationModelWriter newGriddedElevationModelWriter(final Resource resource) {
    return new CompactBinaryGriddedElevationWriter(resource);
  }
}
