package com.revolsys.elevation.gridded.compactbinary;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelWriter;
import com.revolsys.elevation.gridded.IntArrayScaleGriddedElevationModel;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.AbstractWriter;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class CompactBinaryGriddedElevationWriter extends AbstractWriter<GriddedElevationModel>
  implements GriddedElevationModelWriter {
  private Resource resource;

  private byte[] bytes;

  private ByteBuffer buffer;

  private OutputStream out;

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
      this.out = this.resource.newOutputStream();
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
    this.out.write(this.bytes, 0, this.buffer.position());
    this.buffer.rewind();
  }

  private void writeGrid(final GriddedElevationModel elevationModel) throws IOException {
    for (int gridY = 0; gridY < this.gridHeight; gridY++) {
      for (int gridX = 0; gridX < this.gridWidth; gridX++) {
        final double elevation = elevationModel.getElevation(gridX, gridY);
        if (Double.isNaN(elevation)) {
          this.buffer.putInt(Integer.MIN_VALUE);
        } else {
          this.buffer.putInt((int)Math.round(elevation / this.scaleZ));
        }
      }
      writeBuffer();
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
    writeBuffer();
  }
}
