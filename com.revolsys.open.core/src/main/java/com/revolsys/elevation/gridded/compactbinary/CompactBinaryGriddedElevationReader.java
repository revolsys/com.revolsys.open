package com.revolsys.elevation.gridded.compactbinary;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
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
import com.revolsys.io.Buffers;
import com.revolsys.io.IoFactory;
import com.revolsys.logging.Logs;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class CompactBinaryGriddedElevationReader extends BaseObjectWithProperties
  implements Closeable {
  private Resource resource;

  private ReadableByteChannel in;

  private GeometryFactory geometryFactory;

  private double scaleZ;

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
    final ReadableByteChannel in = this.in;
    if (in != null) {
      this.in = null;
      try {
        in.close();
      } catch (final IOException e) {
        Logs.debug(this, "Unable to close: " + this.resource, e);
      }
    }
    this.resource = null;
  }

  public boolean isMemoryMapped() {
    return this.memoryMapped;
  }

  public void open() {
    if (this.in == null) {
      this.in = IoFactory.newReadableByteChannel(this.resource);
      readHeader();
    }
  }

  public GriddedElevationModel read() {
    open();
    try {
      final ReadableByteChannel in = this.in;
      final int cellCount = this.gridWidth * this.gridHeight;
      final int[] elevations = new int[cellCount];
      if (isMemoryMapped() && in instanceof FileChannel) {
        final FileChannel fileChannel = (FileChannel)in;
        final MappedByteBuffer buffer = fileChannel.map(MapMode.READ_ONLY,
          CompactBinaryGriddedElevation.HEADER_SIZE,
          cellCount * CompactBinaryGriddedElevation.RECORD_SIZE);
        final IntBuffer intBuffer = buffer.asIntBuffer();
        for (int index = 0; index < cellCount; index++) {
          elevations[index] = intBuffer.get();
        }
      } else {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4092);

        int bufferCount = 0;
        for (int index = 0; index < cellCount; index++) {
          if (bufferCount == 0) {
            buffer.clear();
            int bufferByteCount = 0;
            do {
              final int readCount = in.read(buffer);
              if (readCount == -1) {
                throw new RuntimeException("Unexpected end of file: " + this.resource);
              } else {
                bufferByteCount += readCount;
              }
            } while (bufferByteCount % 4 != 0);
            buffer.flip();
            bufferCount = bufferByteCount / 4;
          }
          final int elevation = buffer.getInt();
          elevations[index] = elevation;
          bufferCount--;
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
    try {
      final ByteBuffer buffer = ByteBuffer
        .allocateDirect(CompactBinaryGriddedElevation.HEADER_SIZE);
      Buffers.readAll(this.in, buffer);

      final byte[] fileTypeBytes = new byte[6];
      buffer.get(fileTypeBytes);
      @SuppressWarnings("unused")
      final String fileType = new String(fileTypeBytes, StandardCharsets.UTF_8); // File
                                                                                 // type
      @SuppressWarnings("unused")
      final short version = buffer.getShort();
      final int coordinateSystemId = buffer.getInt(); // Coordinate System
                                                      // ID
      final double scaleFactorXY = buffer.getDouble();
      this.scaleZ = buffer.getDouble();
      final double minX = buffer.getDouble();
      final double minY = buffer.getDouble();
      final double minZ = buffer.getDouble();
      final double maxX = buffer.getDouble();
      final double maxY = buffer.getDouble();
      final double maxZ = buffer.getDouble();
      this.gridCellSize = buffer.getInt(); // Grid Cell Size
      this.gridWidth = buffer.getInt(); // Grid Width
      this.gridHeight = buffer.getInt(); // Grid Height

      this.geometryFactory = GeometryFactory.fixed(coordinateSystemId, 3, scaleFactorXY,
        scaleFactorXY, this.scaleZ);
      this.boundingBox = this.geometryFactory.newBoundingBox(3, minX, minY, minZ, maxX, maxY, maxZ);

    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read: " + this.resource, e);
    }
  }

  public void setMemoryMapped(final boolean memoryMapped) {
    this.memoryMapped = memoryMapped;
  }
}
