package com.revolsys.elevation.gridded.compactbinary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;

import com.google.common.collect.Sets;
import com.revolsys.elevation.gridded.DirectFileElevationModel;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.revolsys.io.Buffers;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.file.Paths;
import com.revolsys.util.Exceptions;

public class CompactBinaryGriddedElevationModelFile extends DirectFileElevationModel {
  private static final int ELEVATION_BYTE_COUNT = 4;

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

  private FileChannel channel;

  private ChannelReader reader;

  private final Set<OpenOption> openOptions;

  private final FileAttribute<?>[] fileAttributes = Paths.FILE_ATTRIBUTES_NONE;

  private final ByteBuffer buffer = ByteBuffer.allocateDirect(ELEVATION_BYTE_COUNT);

  private ByteBuffer rowBuffer;

  private double scaleZ;

  private boolean createMissing = false;

  private boolean useLocks = false;

  public CompactBinaryGriddedElevationModelFile(final Path path) {
    super(CompactBinaryGriddedElevation.HEADER_SIZE, CompactBinaryGriddedElevation.RECORD_SIZE);
    this.path = path;
    this.openOptions = Paths.OPEN_OPTIONS_READ_SET;
    readHeader();
  }

  public CompactBinaryGriddedElevationModelFile(final Path path,
    final GeometryFactory geometryFactory, final int minX, final int minY, final int gridWidth,
    final int gridHeight, final int gridCellSize) {
    super(geometryFactory, minX, minY, gridWidth, gridHeight, gridCellSize,
      CompactBinaryGriddedElevation.HEADER_SIZE, 4);
    setZBoundsUpdateRequired(false);
    this.openOptions = Sets.newHashSet(StandardOpenOption.READ, StandardOpenOption.WRITE,
      StandardOpenOption.SYNC);
    this.createMissing = true;
    this.path = path;

    this.scaleZ = geometryFactory.getScaleZ();
    if (this.scaleZ <= 0) {
      this.scaleZ = 1000;
    }
  }

  @Override
  public void close() {
    super.close();
    final FileChannel fileChannel = this.channel;
    this.channel = null;
    if (fileChannel != null) {
      try {
        fileChannel.close();
      } catch (final IOException e) {
      }
    }
  }

  protected void createNewFile() throws IOException {
    Paths.createParentDirectories(this.path);
    this.channel = FileChannel.open(this.path, Paths.OPEN_OPTIONS_READ_WRITE_SET,
      this.fileAttributes);
    final ChannelWriter writer = new ChannelWriter(this.channel);
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();
    final int gridCellSize = getGridCellSize();
    final BoundingBox boundingBox = getBoundingBox();
    final GeometryFactory geometryFactory = getGeometryFactory();
    CompactBinaryGriddedElevationWriter.writeHeader(writer, boundingBox, geometryFactory, gridWidth,
      gridHeight, gridCellSize);
    final int count = gridWidth * gridHeight;
    for (int i = 0; i < count; i++) {
      writer.putInt(Integer.MIN_VALUE);
    }
  }

  private FileChannel getFileChannel() throws IOException {
    if (this.channel == null && isOpen()) {
      try {
        this.channel = FileChannel.open(this.path, this.openOptions, this.fileAttributes);
        this.reader = new ChannelReader(this.channel);
      } catch (final NoSuchFileException e) {
        if (this.createMissing) {
          createNewFile();
        } else {
          throw e;
        }

      }
      if (!isOpen()) {
        close();
        return null;
      }
    }
    return this.channel;
  }

  private ChannelReader getReader() throws IOException {
    getFileChannel();
    return this.reader;
  }

  public boolean isUseLocks() {
    return this.useLocks;
  }

  @Override
  protected synchronized double readElevation(final int offset) {
    try {
      final FileChannel fileChannel = getFileChannel();
      if (fileChannel == null) {
        return Double.NaN;
      } else {
        this.buffer.clear();
        while (this.buffer.hasRemaining()) {
          if (fileChannel.read(this.buffer, offset) == -1) {
            return Double.NaN;
          }
        }
        this.buffer.flip();
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

  private void readHeader() {
    try {
      final ChannelReader reader = getReader();

      final byte[] fileTypeBytes = new byte[6];
      this.reader.getBytes(fileTypeBytes);
      @SuppressWarnings("unused")
      final String fileType = new String(fileTypeBytes, StandardCharsets.UTF_8); // File
                                                                                 // type
      @SuppressWarnings("unused")
      final short version = this.reader.getShort();
      final GeometryFactory geometryFactory = CompactBinaryGriddedElevationReader
        .readGeometryFactory(this.reader, version);

      final double minX = reader.getDouble();
      final double minY = reader.getDouble();
      final double minZ = reader.getDouble();
      final double maxX = reader.getDouble();
      final double maxY = reader.getDouble();
      final double maxZ = reader.getDouble();
      final int gridCellSize = reader.getInt(); // Grid Cell Size
      final int gridWidth = reader.getInt(); // Grid Width
      final int gridHeight = reader.getInt(); // Grid Height

      setGeometryFactory(geometryFactory);
      final BoundingBox boundingBox = geometryFactory.newBoundingBox(3, minX, minY, minZ, maxX,
        maxY, maxZ);
      setBoundingBox(boundingBox);
      setGridCellSize(gridCellSize);
      setGridWidth(gridWidth);
      setGridHeight(gridHeight);
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read: " + this.path, e);
    }
  }

  public void setElevations(final double x, final double y, final double[] elevations) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    setElevations(gridX, gridY, elevations);
  }

  public void setElevations(final int gridX, final int gridY, final double[] elevations) {
    try {
      final FileChannel fileChannel = getFileChannel();

      if (fileChannel != null) {
        ByteBuffer buffer = this.rowBuffer;
        final int gridWidth2 = getGridWidth();
        if (buffer == null) {
          buffer = ByteBuffer.allocateDirect(4 * gridWidth2);
          this.rowBuffer = buffer;
        }
        final double scale = this.scaleZ;
        for (final double elevation : elevations) {
          final int elevationInt;
          if (Double.isFinite(elevation)) {
            elevationInt = (int)Math.round(elevation * scale);
          } else {
            elevationInt = Integer.MIN_VALUE;
          }
          buffer.putInt(elevationInt);
        }
        final int offset = this.headerSize + (gridY * gridWidth2 + gridX) * ELEVATION_BYTE_COUNT;
        if (this.useLocks) {
          try (
            FileLock lock = fileChannel.lock(offset, elevations.length * ELEVATION_BYTE_COUNT,
              false)) {
            Buffers.writeAll(fileChannel, buffer, offset);
          }
        } else {
          Buffers.writeAll(fileChannel, buffer, offset);
        }
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read: " + this.path, e);
    }
  }

  public void setUseLocks(final boolean useLocks) {
    this.useLocks = useLocks;
  }

  @Override
  protected synchronized void writeElevation(int offset, final double elevation) {
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
          offset += fileChannel.write(this.buffer, offset);
        }
        this.buffer.clear();
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read: " + this.path, e);
    }
  }
}
