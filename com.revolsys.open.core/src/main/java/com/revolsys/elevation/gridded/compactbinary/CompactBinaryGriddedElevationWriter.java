package com.revolsys.elevation.gridded.compactbinary;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelWriter;
import com.revolsys.elevation.gridded.IntArrayScaleGriddedElevationModel;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.Buffers;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class CompactBinaryGriddedElevationWriter extends AbstractWriter<GriddedElevationModel>
  implements GriddedElevationModelWriter {
  private Resource resource;

  private byte[] bytes;

  private ByteBuffer buffer;

  private WritableByteChannel out;

  private int gridWidth;

  private int gridHeight;

  private double scaleZ;

  CompactBinaryGriddedElevationWriter(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void close() {
    super.close();
    if (this.out != null) {
      try {
        this.out.close();
      } catch (final Throwable e) {
      } finally {
        this.out = null;
      }
    }
    this.resource = null;
  }

  @Override
  public void open() {
    if (this.out == null) {
      final String fileNameExtension = this.resource.getFileNameExtension();
      OutputStream bufferedOut = this.resource.newBufferedOutputStream();
      if ("zip".equals(fileNameExtension)
        || CompactBinaryGriddedElevation.FILE_EXTENSION_ZIP.equals(fileNameExtension)) {
        try {
          final String fileName = this.resource.getBaseName();
          final ZipOutputStream zipOut = new ZipOutputStream(
            bufferedOut);
          final ZipEntry zipEntry = new ZipEntry(fileName);
          zipOut.putNextEntry(zipEntry);
          this.out = Channels.newChannel(zipOut);
        } catch (final IOException e) {
          throw Exceptions.wrap("Error creating: " + this.resource, e);
        }
      } else if ("gz".equals(fileNameExtension)) {
        try {
          String fileName = this.resource.getBaseName();
          if (!fileName.endsWith("." + CompactBinaryGriddedElevation.FILE_EXTENSION)) {
            fileName += "." + CompactBinaryGriddedElevation.FILE_EXTENSION;
          }
          final GZIPOutputStream zipOut = new GZIPOutputStream(
            bufferedOut);
          this.out = Channels.newChannel(zipOut);
        } catch (final IOException e) {
          throw Exceptions.wrap("Error creating: " + this.resource, e);
        }
      } else {
        this.out = this.resource.newWritableByteChannel();
      }
    }
  }

  @Override
  public void write(final GriddedElevationModel elevationModel) {
    open();
    try {
      writeHeader(elevationModel);
      if (elevationModel instanceof IntArrayScaleGriddedElevationModel) {
        final IntArrayScaleGriddedElevationModel scaleModel = (IntArrayScaleGriddedElevationModel)elevationModel;
        scaleModel.writeIntArray(this, this.buffer);
      } else {
        writeGrid(elevationModel);
      }
    } catch (final IOException e) {
      Exceptions.throwUncheckedException(e);
    }
  }

  public void writeBuffer() throws IOException {
    Buffers.writeAll(this.out, this.buffer);
  }

  private void writeGrid(final GriddedElevationModel elevationModel) throws IOException {
    for (int gridY = 0; gridY < this.gridHeight; gridY++) {
      for (int gridX = 0; gridX < this.gridWidth; gridX++) {
        final double elevation = elevationModel.getElevation(gridX, gridY);
        if (Double.isFinite(elevation)) {
          this.buffer.putInt((int)Math.round(elevation / this.scaleZ));
        } else {
          this.buffer.putInt(Integer.MIN_VALUE);
        }
      }
      Buffers.writeAll(this.out, this.buffer);
    }
  }

  private void writeHeader(final GriddedElevationModel elevationModel) throws IOException {
    final GeometryFactory geometryFactory = elevationModel.getGeometryFactory();

    final int coordinateSystemId = geometryFactory.getCoordinateSystemId();
    double scaleXY = geometryFactory.getScaleXy();
    if (scaleXY <= 0) {
      scaleXY = 1000;
    }
    this.scaleZ = geometryFactory.getScaleZ();
    if (this.scaleZ <= 0) {
      this.scaleZ = 1000;
    }
    this.gridWidth = elevationModel.getGridWidth();
    this.gridHeight = elevationModel.getGridHeight();

    this.bytes = new byte[4 * this.gridWidth];
    this.buffer = ByteBuffer.wrap(this.bytes);
    this.buffer.put(CompactBinaryGriddedElevation.FILE_FORMAT_BYTES); // File
    // type
    this.buffer.putShort(CompactBinaryGriddedElevation.VERSION); // version
    this.buffer.putInt(coordinateSystemId); // Coordinate System ID
    this.buffer.putDouble(scaleXY); // Scale XY
    this.buffer.putDouble(this.scaleZ); // Scale Z
    this.buffer.putDouble(elevationModel.getMinX()); // minX
    this.buffer.putDouble(elevationModel.getMinY()); // minY
    this.buffer.putDouble(elevationModel.getMinZ()); // minZ
    this.buffer.putDouble(elevationModel.getMaxX()); // maxX
    this.buffer.putDouble(elevationModel.getMaxY()); // maxY
    this.buffer.putDouble(elevationModel.getMaxZ()); // maxZ
    this.buffer.putInt(elevationModel.getGridCellSize()); // Grid Cell Size
    this.buffer.putInt(this.gridWidth); // Grid Width
    this.buffer.putInt(this.gridHeight); // Grid Height
    Buffers.writeAll(this.out, this.buffer);
  }
}
