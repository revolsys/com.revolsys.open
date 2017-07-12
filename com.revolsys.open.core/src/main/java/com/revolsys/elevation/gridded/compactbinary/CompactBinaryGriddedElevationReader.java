package com.revolsys.elevation.gridded.compactbinary;

import java.io.Closeable;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.IntArrayScaleGriddedElevationModel;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IoFactory;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class CompactBinaryGriddedElevationReader extends BaseObjectWithProperties
  implements Closeable {
  protected static GeometryFactory readGeometryFactory(final ChannelReader reader,
    final short version) {
    GeometryFactory geometryFactory;
    final int coordinateSystemId = reader.getInt(); // Coordinate System ID
    if (version == 1) {
      final double scaleX = reader.getDouble();
      final double scaleZ = reader.getDouble();
      geometryFactory = GeometryFactory.fixed(coordinateSystemId, 3, scaleX, scaleX, scaleZ);
    } else {
      final double offsetX = reader.getDouble();
      final double scaleX = reader.getDouble();
      final double offsetY = reader.getDouble();
      final double scaleY = reader.getDouble();
      final double offsetZ = reader.getDouble();
      final double scaleZ = reader.getDouble();
      geometryFactory = GeometryFactory.newWithOffsets(coordinateSystemId, offsetX, scaleX, offsetY,
        scaleY, offsetZ, scaleZ);
    }
    return geometryFactory;
  }

  private Resource resource;

  private ChannelReader reader;

  private GeometryFactory geometryFactory;

  private BoundingBox boundingBox;

  private int gridCellSize;

  private int gridWidth;

  private int gridHeight;

  private boolean memoryMapped = false;

  CompactBinaryGriddedElevationReader(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void close() {
    super.close();
    final ChannelReader reader = this.reader;
    this.reader = null;
    if (reader != null) {
      reader.close();
    }
    this.resource = null;
  }

  public boolean isMemoryMapped() {
    return this.memoryMapped;
  }

  public void open() {
    if (this.reader == null) {
      this.reader = IoFactory.newChannelReader(this.resource);
      readHeader();
    }
  }

  public GriddedElevationModel read() {
    open();
    try {
      final ChannelReader in = this.reader;
      final int cellCount = this.gridWidth * this.gridHeight;
      final int[] elevations = new int[cellCount];
      final ReadableByteChannel channel = in.getChannel();
      if (isMemoryMapped() && channel instanceof FileChannel) {
        final FileChannel fileChannel = (FileChannel)channel;
        final MappedByteBuffer mappedBytes = fileChannel.map(MapMode.READ_ONLY,
          CompactBinaryGriddedElevation.HEADER_SIZE,
          cellCount * CompactBinaryGriddedElevation.RECORD_SIZE);
        final IntBuffer intBuffer = mappedBytes.asIntBuffer();
        for (int index = 0; index < cellCount; index++) {
          elevations[index] = intBuffer.get();
        }
      } else {

        for (int index = 0; index < cellCount; index++) {
          final int elevation = in.getInt();
          elevations[index] = elevation;
        }
      }
      final IntArrayScaleGriddedElevationModel elevationModel = new IntArrayScaleGriddedElevationModel(
        this.geometryFactory, this.boundingBox, this.gridWidth, this.gridHeight, this.gridCellSize,
        elevations);
      elevationModel.setResource(this.resource);
      return elevationModel;
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read DEM: " + this.resource, e);
    }
  }

  private void readHeader() {
    final byte[] fileTypeBytes = new byte[6];
    this.reader.getBytes(fileTypeBytes);
    @SuppressWarnings("unused")
    final String fileType = new String(fileTypeBytes, StandardCharsets.UTF_8); // File
                                                                               // type
    final short version = this.reader.getShort();
    final GeometryFactory geometryFactory = readGeometryFactory(this.reader, version);
    this.geometryFactory = geometryFactory;
    final double minX = this.reader.getDouble();
    final double minY = this.reader.getDouble();
    final double minZ = this.reader.getDouble();
    final double maxX = this.reader.getDouble();
    final double maxY = this.reader.getDouble();
    final double maxZ = this.reader.getDouble();
    this.gridCellSize = this.reader.getInt(); // Grid Cell Size
    this.gridWidth = this.reader.getInt(); // Grid Width
    this.gridHeight = this.reader.getInt(); // Grid Height

    this.boundingBox = geometryFactory.newBoundingBox(3, minX, minY, minZ, maxX, maxY, maxZ);

  }

  public void setMemoryMapped(final boolean memoryMapped) {
    this.memoryMapped = memoryMapped;
  }
}
