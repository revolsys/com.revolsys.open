package com.revolsys.elevation.gridded.scaledint;

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
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class ScaledIntegerGriddedDigitalElevation extends AbstractIoFactoryWithCoordinateSystem
  implements GriddedElevationModelReadFactory, GriddedElevationModelWriterFactory {

  public static final String MEDIA_TYPE = "image/x-revolsys-sigdem";

  public static final String FILE_EXTENSION = "sigdem";

  public static final String FILE_EXTENSION_GZ = FILE_EXTENSION + ".gz";

  public static final String FILE_EXTENSION_ZIP = FILE_EXTENSION + ".zip";

  public static final String FILE_FORMAT = "SIGDEM";

  public static final byte[] FILE_FORMAT_BYTES = "SIGDEM".getBytes(StandardCharsets.UTF_8);

  public static final int HEADER_SIZE = 120;

  public static final int RECORD_SIZE = 4;

  public static final short VERSION = 1;

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

  public static double getElevationNearest(final Path baseDirectory, final int coordinateSystemId,
    final int gridCellSize, final int gridSize, final String fileExtension, final double x,
    final double y) {

    final int gridTileSize = gridSize * gridCellSize;
    final int tileX = CustomRectangularMapGrid.getGridFloor(0.0, gridTileSize, x);
    final int tileY = CustomRectangularMapGrid.getGridFloor(0.0, gridTileSize, y);

    final Path file = RectangularMapGrid.getTilePath(baseDirectory, "dem", coordinateSystemId,
      Integer.toString(gridTileSize), tileX, tileY, fileExtension);
    try {
      final int gridCellX = GriddedElevationModel.getGridCellX(tileX, gridCellSize, x);
      final int gridCellY = GriddedElevationModel.getGridCellY(tileY, gridCellSize, y);
      final int elevationByteSize = 4;
      final int offset = HEADER_SIZE + (gridCellY * gridSize + gridCellX) * elevationByteSize;
      try (
        FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
        final ByteBuffer bytes = ByteBuffer.allocate(4);
        channel.read(bytes, offset);
        return bytes.getInt(0);
      }
    } catch (final NoSuchFileException e) {
      return Double.NaN;
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read: " + file, e);
    } catch (final ClassCastException e) {
      throw new IllegalArgumentException(fileExtension + " not supported");
    }
  }

  @SuppressWarnings("unchecked")
  public static <G extends Geometry> G setElevationNearest(final Path baseDirectory,
    final int coordinateSystemId, final int gridCellSize, final int gridSize,
    final String fileExtension, final G geometry) {
    final GeometryEditor<?> editor = geometry.newGeometryEditor();
    editor.setAxisCount(3);
    for (final Vertex vertex : geometry.vertices()) {
      final double x = vertex.getX();
      final double y = vertex.getY();
      final double elevation = getElevationNearest(baseDirectory, coordinateSystemId, gridCellSize,
        gridSize, fileExtension, x, y);
      if (!Double.isNaN(elevation)) {
        final int[] vertexId = vertex.getVertexId();
        editor.setZ(vertexId, elevation);
      }
    }
    return (G)editor.newGeometry();
  }

  public ScaledIntegerGriddedDigitalElevation() {
    super("Scaled Integer Gridded Digital Elevation Model");

    addMediaTypeAndFileExtension(MEDIA_TYPE, FILE_EXTENSION);
    addFileExtension(FILE_EXTENSION_ZIP);
    addFileExtension(FILE_EXTENSION_GZ);
  }

  @Override
  public ScaledIntegerGriddedDigitalElevationModelReader newGriddedElevationModelReader(
    final Resource resource, final Map<String, ? extends Object> properties) {
    return new ScaledIntegerGriddedDigitalElevationModelReader(resource, properties);
  }

  @Override
  public GriddedElevationModelWriter newGriddedElevationModelWriter(final Resource resource) {
    return new ScaledIntegerGriddedDigitalElevationModelWriter(resource);
  }
}
