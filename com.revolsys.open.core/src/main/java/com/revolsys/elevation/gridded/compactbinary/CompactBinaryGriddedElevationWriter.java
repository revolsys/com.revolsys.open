package com.revolsys.elevation.gridded.compactbinary;

import java.io.IOException;
import java.io.OutputStream;
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
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class CompactBinaryGriddedElevationWriter extends AbstractWriter<GriddedElevationModel>
  implements GriddedElevationModelWriter {
  public static void writeHeader(final ChannelWriter out, final BoundingBox boundingBox,
    final GeometryFactory geometryFactory, final int gridWidth, final int gridHeight,
    final double gridCellSize) throws IOException {
    final int coordinateSystemId = geometryFactory.getCoordinateSystemId();
    double scaleXY = geometryFactory.getScaleXY();
    if (scaleXY <= 0) {
      scaleXY = 1000;
    }
    double scaleZ = geometryFactory.getScaleZ();
    if (scaleZ <= 0) {
      scaleZ = 1000;
    }
    out.putBytes(CompactBinaryGriddedElevation.FILE_FORMAT_BYTES); // File //
                                                                   // type
    out.putShort(CompactBinaryGriddedElevation.VERSION); // version
    out.putInt(coordinateSystemId); // Coordinate System ID

    for (int axisIndex = 0; axisIndex < 3; axisIndex++) {
      final double offset = geometryFactory.getOffset(axisIndex);
      out.putDouble(offset);
      final double scale = geometryFactory.getScale(axisIndex);
      out.putDouble(scale);
    }

    out.putDouble(boundingBox.getMinX()); // minX
    out.putDouble(boundingBox.getMinY()); // minY
    out.putDouble(boundingBox.getMinZ()); // minZ
    out.putDouble(boundingBox.getMaxX()); // maxX
    out.putDouble(boundingBox.getMaxY()); // maxY
    out.putDouble(boundingBox.getMaxZ()); // maxZ
    out.putInt((int)gridCellSize); // Grid Cell Size
    out.putInt(gridWidth); // Grid Width
    out.putInt(gridHeight); // Grid Height
  }

  private Resource resource;

  private ChannelWriter writer;

  private int gridWidth;

  private int gridHeight;

  CompactBinaryGriddedElevationWriter(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void close() {
    super.close();
    if (this.writer != null) {
      try {
        this.writer.close();
      } catch (final Throwable e) {
      } finally {
        this.writer = null;
      }
    }
    this.resource = null;
  }

  @Override
  public void open() {
    if (this.writer == null) {
      final String fileNameExtension = this.resource.getFileNameExtension();
      if ("zip".equals(fileNameExtension)
        || CompactBinaryGriddedElevation.FILE_EXTENSION_ZIP.equals(fileNameExtension)) {
        try {
          final OutputStream bufferedOut = this.resource.newBufferedOutputStream();
          final String fileName = this.resource.getBaseName();
          final ZipOutputStream zipOut = new ZipOutputStream(bufferedOut);
          final ZipEntry zipEntry = new ZipEntry(fileName);
          zipOut.putNextEntry(zipEntry);
          final WritableByteChannel channel = Channels.newChannel(zipOut);
          this.writer = new ChannelWriter(channel);
        } catch (final IOException e) {
          throw Exceptions.wrap("Error creating: " + this.resource, e);
        }
      } else if ("gz".equals(fileNameExtension)) {
        try {
          String fileName = this.resource.getBaseName();
          if (!fileName.endsWith("." + CompactBinaryGriddedElevation.FILE_EXTENSION)) {
            fileName += "." + CompactBinaryGriddedElevation.FILE_EXTENSION;
          }
          final OutputStream bufferedOut = this.resource.newBufferedOutputStream();
          final GZIPOutputStream zipOut = new GZIPOutputStream(bufferedOut);
          final WritableByteChannel channel = Channels.newChannel(zipOut);
          this.writer = new ChannelWriter(channel);
        } catch (final IOException e) {
          throw Exceptions.wrap("Error creating: " + this.resource, e);
        }
      } else {
        this.writer = this.resource.newChannelWriter();
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
        scaleModel.writeIntArray(this, this.writer);
      } else {
        writeGrid(elevationModel);
      }
    } catch (final IOException e) {
      Exceptions.throwUncheckedException(e);
    }
  }

  private void writeGrid(final GriddedElevationModel elevationModel) throws IOException {
    final ChannelWriter out = this.writer;
    final int gridWidth = this.gridWidth;
    final int gridHeight = this.gridHeight;
    final GeometryFactory geometryFactory = elevationModel.getGeometryFactory();
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        final double elevation = elevationModel.getElevation(gridX, gridY);
        final int zInt = geometryFactory.toIntZ(elevation);
        out.putInt(zInt);
      }
    }
  }

  private void writeHeader(final GriddedElevationModel elevationModel) throws IOException {
    final GeometryFactory geometryFactory = elevationModel.getGeometryFactory();
    elevationModel.updateZBoundingBox();
    final BoundingBox boundingBox = elevationModel.getBoundingBox();
    this.gridWidth = elevationModel.getGridWidth();
    this.gridHeight = elevationModel.getGridHeight();
    final double gridCellSize = elevationModel.getGridCellSize();

    writeHeader(this.writer, boundingBox, geometryFactory, this.gridWidth, this.gridHeight,
      gridCellSize);
  }
}
