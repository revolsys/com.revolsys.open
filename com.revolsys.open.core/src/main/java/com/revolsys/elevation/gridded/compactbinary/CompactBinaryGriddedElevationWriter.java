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
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.Buffers;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class CompactBinaryGriddedElevationWriter extends AbstractWriter<GriddedElevationModel>
  implements GriddedElevationModelWriter {
  public static void writeHeader(final WritableByteChannel out, final ByteBuffer buffer,
    final BoundingBox boundingBox, final GeometryFactory geometryFactory, final int gridWidth,
    final int gridHeight, final int gridCellSize) throws IOException {
    final int coordinateSystemId = geometryFactory.getCoordinateSystemId();
    double scaleXY = geometryFactory.getScaleXY();
    if (scaleXY <= 0) {
      scaleXY = 1000;
    }
    double scaleZ = geometryFactory.getScaleZ();
    if (scaleZ <= 0) {
      scaleZ = 1000;
    }

    buffer.put(CompactBinaryGriddedElevation.FILE_FORMAT_BYTES); // File // type
    buffer.putShort(CompactBinaryGriddedElevation.VERSION); // version
    buffer.putInt(coordinateSystemId); // Coordinate System ID
    buffer.putDouble(scaleXY); // Scale XY
    buffer.putDouble(scaleZ); // Scale Z
    buffer.putDouble(boundingBox.getMinX()); // minX
    buffer.putDouble(boundingBox.getMinY()); // minY
    buffer.putDouble(boundingBox.getMinZ()); // minZ
    buffer.putDouble(boundingBox.getMaxX()); // maxX
    buffer.putDouble(boundingBox.getMaxY()); // maxY
    buffer.putDouble(boundingBox.getMaxZ()); // maxZ
    buffer.putInt(gridCellSize); // Grid Cell Size
    buffer.putInt(gridWidth); // Grid Width
    buffer.putInt(gridHeight); // Grid Height
    Buffers.writeAll(out, buffer);
  }

  private Resource resource;

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
      final OutputStream bufferedOut = this.resource.newBufferedOutputStream();
      if ("zip".equals(fileNameExtension)
        || CompactBinaryGriddedElevation.FILE_EXTENSION_ZIP.equals(fileNameExtension)) {
        try {
          final String fileName = this.resource.getBaseName();
          final ZipOutputStream zipOut = new ZipOutputStream(bufferedOut);
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
          final GZIPOutputStream zipOut = new GZIPOutputStream(bufferedOut);
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
    elevationModel.updateZBoundingBox();
    final BoundingBox boundingBox = elevationModel.getBoundingBox();
    this.gridWidth = elevationModel.getGridWidth();
    this.gridHeight = elevationModel.getGridHeight();
    final int gridCellSize = elevationModel.getGridCellSize();

    this.buffer = ByteBuffer.allocateDirect(4 * this.gridWidth);
    this.scaleZ = geometryFactory.getScaleZ();
    if (this.scaleZ <= 0) {
      this.scaleZ = 1000;
    }

    writeHeader(this.out, this.buffer, boundingBox, geometryFactory, this.gridWidth,
      this.gridHeight, gridCellSize);
  }
}
