package com.revolsys.geometry.cs.geoid.usngsbin;

import java.nio.ByteOrder;
import java.nio.channels.ClosedByInterruptException;

import com.revolsys.geometry.cs.geoid.AbstractGeoidGrid;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.grid.FloatArrayGrid;
import com.revolsys.io.IoFactory;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.util.Exceptions;

public class UsNgsBinGeoidGrid extends AbstractGeoidGrid {

  public UsNgsBinGeoidGrid(final Object source) {
    super(source);
  }

  @Override
  protected void read() {
    try (
      ChannelReader reader = IoFactory.newChannelReader(this.resource, 8192)) {
      if (reader != null) {
        readHeader(reader);
        try {
          final int gridWidth = this.gridWidth;
          final int gridHeight = this.gridHeight;
          final int cellCount = gridWidth * gridHeight;
          final float[] cells = new float[cellCount];
          for (int i = 0; i < cellCount; i++) {
            final float value = reader.getFloat();
            cells[i] = value;
          }
          this.grid = new FloatArrayGrid(this.geometryFactory, this.boundingBox, gridWidth,
            gridHeight, this.gridCellWidth, this.gridCellHeight, cells);
        } catch (final RuntimeException e) {
          if (!Exceptions.isException(e, ClosedByInterruptException.class)) {
            throw Exceptions.wrap("Unable to read : " + this.resource, e);
          }
        }
      }
    }
  }

  @SuppressWarnings("unused")
  private void readHeader(final ChannelReader reader) {
    double minY = reader.getDouble();
    if (Math.abs(minY) < 1e-10) {
      minY = Double.longBitsToDouble(Long.reverseBytes(Double.doubleToRawLongBits(minY)));
      reader.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    }
    double minX = -(reader.getDouble() - 100);
    this.gridCellHeight = reader.getDouble();
    this.gridCellWidth = reader.getDouble();
    this.gridHeight = reader.getInt();
    this.gridWidth = reader.getInt();
    final int kind = reader.getInt();
    this.geometryFactory = GeometryFactory.nad83();
    minX -= this.gridCellWidth / 2;
    minY -= this.gridCellHeight / 2;
    this.boundingBox = this.geometryFactory.newBoundingBox(2, minX, minY,
      minX + this.gridCellWidth * this.gridWidth, minY + this.gridCellHeight * this.gridHeight);
  }
}
