package com.revolsys.geometry.cs.geoid.usngsbin;

import java.nio.ByteOrder;
import java.nio.channels.ClosedByInterruptException;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.FloatArrayGriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReader;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IoFactory;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class UsNgsBinGeoidModelReader extends BaseObjectWithProperties
  implements GriddedElevationModelReader {
  private final Resource resource;

  private GeometryFactory geometryFactory;

  private BoundingBox boundingBox;

  private double gridCellWidth;

  private int gridWidth;

  private int gridHeight;

  private double gridCellHeight;

  public UsNgsBinGeoidModelReader(final Resource resource, final MapEx properties) {
    this.resource = resource;
    setProperties(properties);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  public double getGridCellHeight() {
    return this.gridCellHeight;
  }

  @Override
  public double getGridCellWidth() {
    return this.gridCellWidth;
  }

  @Override
  public GriddedElevationModel read() {
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
          final FloatArrayGriddedElevationModel grid = new FloatArrayGriddedElevationModel(
            this.geometryFactory, this.boundingBox, gridWidth, gridHeight, this.gridCellWidth,
            this.gridCellHeight, cells);
          return grid;
        } catch (final RuntimeException e) {
          if (Exceptions.isException(e, ClosedByInterruptException.class)) {
            return null;
          } else {
            throw Exceptions.wrap("Unable to read : " + this.resource, e);
          }
        }
      }
    }
    return null;
  }

  @SuppressWarnings("unused")
  private void readHeader(final ChannelReader reader) {
    double minY = reader.getDouble();
    if (Math.abs(minY) < 1e-10) {
      minY = Double.longBitsToDouble(Long.reverseBytes(Double.doubleToRawLongBits(minY)));
      reader.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    }
    final double lonEast = reader.getDouble();
    final double minX = -(360 - lonEast);
    this.gridCellHeight = reader.getDouble();
    this.gridCellWidth = reader.getDouble();
    this.gridHeight = reader.getInt();
    this.gridWidth = reader.getInt();
    final int kind = reader.getInt();
    this.geometryFactory = GeometryFactory.nad83();
    // minX -= this.gridCellWidth / 2;
    // minY -= this.gridCellHeight / 2;
    this.boundingBox = this.geometryFactory.newBoundingBox(2, minX, minY,
      minX + this.gridCellWidth * this.gridWidth, minY + this.gridCellHeight * this.gridHeight);
  }
}
