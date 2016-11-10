package com.revolsys.elevation.gridded.compactbinary;

import java.io.DataOutputStream;
import java.io.IOException;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelWriter;
import com.revolsys.io.AbstractWriter;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class CompactBinaryGriddedElevationModelWriter extends AbstractWriter<GriddedElevationModel>
  implements GriddedElevationModelWriter {
  private final String version = "   0.0.1";

  private boolean floatingPoint = false;

  private Resource resource;

  public CompactBinaryGriddedElevationModelWriter(final Resource resource,
    final boolean floatingPoint) {
    this.resource = resource;
    this.floatingPoint = floatingPoint;
  }

  @Override
  public void close() {
    super.close();
    this.resource = null;
  }

  @Override
  public void write(final GriddedElevationModel elevationModel) {
    if (this.resource == null) {
      throw new IllegalStateException("Writer is closed");
    } else {
      try (
        DataOutputStream out = this.resource.newBufferedOutputStream(DataOutputStream::new)) {
        out.writeChars("DEMC"); // File type
        if (this.floatingPoint) {
          out.writeChar('F');
        } else {
          out.writeChar('S');
        }
        out.writeChar('-');
        out.writeChars(this.version); // version
        out.writeInt(elevationModel.getCoordinateSystemId()); // Coordinate System ID
        out.writeDouble(elevationModel.getMinX()); // minX
        out.writeDouble(elevationModel.getMinY()); // maxX
        out.writeInt(elevationModel.getGridCellSize()); // Grid Cell Size
        final int gridWidth = elevationModel.getGridWidth();
        out.writeInt(gridWidth); // Grid Width
        final int gridHeight = elevationModel.getGridHeight();
        out.writeInt(gridHeight); // Grid Height
        if (this.floatingPoint) {
          writeGridFloat(out, elevationModel, gridWidth, gridHeight);
        } else {
          writeGridShort(out, elevationModel, gridWidth, gridHeight);
        }
      } catch (final IOException e) {
        Exceptions.throwUncheckedException(e);
      }
    }
  }

  private void writeGridFloat(final DataOutputStream out,
    final GriddedElevationModel elevationModel, final int gridWidth, final int gridHeight)
    throws IOException {
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        final double elevation;
        if (elevationModel.isNull(gridX, gridY)) {
          elevation = Double.NaN;
        } else {
          elevation = elevationModel.getElevation(gridX, gridY);
        }
        out.writeFloat((float)elevation);
      }
    }
  }

  private void writeGridShort(final DataOutputStream out,
    final GriddedElevationModel elevationModel, final int gridWidth, final int gridHeight)
    throws IOException {
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        final short elevationShort;
        if (elevationModel.isNull(gridX, gridY)) {
          elevationShort = Short.MIN_VALUE;
        } else {
          final double elevation = elevationModel.getElevation(gridX, gridY);
          if (Double.isNaN(elevation)) {
            elevationShort = Short.MIN_VALUE;
          } else {
            elevationShort = (short)elevation;
          }
        }
        out.writeShort(elevationShort);

      }
    }
  }
}
