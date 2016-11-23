package com.revolsys.elevation.gridded.compactbinary;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.IntArrayScaleGriddedElevationModel;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.Buffers;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class CompactBinaryGriddedElevationReader extends BaseObjectWithProperties
  implements Closeable {
  private Resource resource;

  private ReadableByteChannel in;

  private GeometryFactory geometryFactory;

  private double scaleFactorZ;

  private BoundingBox boundingBox;

  private int gridCellSize;

  private int gridWidth;

  private int gridHeight;

  CompactBinaryGriddedElevationReader(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void close() {
    super.close();
    this.resource = null;
  }

  public void open() {
    if (this.in == null) {
      this.in = this.resource.newReadableByteChannel();
      readHeader();
    }
  }

  public GriddedElevationModel read() {
    open();
    try {
      final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * this.gridWidth);

      final int cellCount = this.gridWidth * this.gridHeight;
      final int[] elevations = new int[cellCount];
      int index = 0;
      for (int gridY = 0; gridY < this.gridHeight; gridY++) {
        buffer.clear();
        Buffers.readAll(this.in, buffer);
        for (int gridX = 0; gridX < this.gridWidth; gridX++) {
          elevations[index++] = buffer.getInt();
        }
      }

      final IntArrayScaleGriddedElevationModel elevationModel = new IntArrayScaleGriddedElevationModel(
        this.geometryFactory, this.boundingBox, this.gridWidth, this.gridHeight, this.gridCellSize,
        this.scaleFactorZ, elevations);
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
      this.scaleFactorZ = buffer.getDouble();
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
        this.scaleFactorZ);
      this.boundingBox = this.geometryFactory.newBoundingBox(2, minX, minY, minZ, maxX, maxY, maxZ);

    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read: " + this.resource, e);
    }
  }
}
