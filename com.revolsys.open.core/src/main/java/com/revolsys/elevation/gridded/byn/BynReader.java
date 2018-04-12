package com.revolsys.elevation.gridded.byn;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedByInterruptException;
import java.util.Map;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReader;
import com.revolsys.elevation.gridded.IntArrayScaleGriddedElevationModel;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IoFactory;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class BynReader extends BaseObjectWithProperties implements GriddedElevationModelReader {
  private boolean initialized;

  private Resource resource;

  private ByteBuffer byteBuffer;

  private ChannelReader reader;

  private GeometryFactory geometryFactory;

  private BoundingBox boundingBox;

  private double gridCellSize;

  private int gridWidth;

  private int gridHeight;

  private boolean memoryMapped = false;

  private boolean exists;

  BynReader(final Resource resource, final Map<String, ? extends Object> properties) {
    this.resource = resource;
    setProperties(properties);
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

  @Override
  public BoundingBox getBoundingBox() {
    init();
    return this.boundingBox;
  }

  public ByteBuffer getByteBuffer() {
    return this.byteBuffer;
  }

  @Override
  public double getGridCellSize() {
    init();
    return this.gridCellSize;
  }

  private void init() {
    if (!this.initialized) {
      this.initialized = true;
      if (this.byteBuffer == null) {
        this.reader = IoFactory.newChannelReader(this.resource, 8192);
      } else {
        this.reader = IoFactory.newChannelReader(this.resource, this.byteBuffer);
      }
      if (this.reader == null) {
        this.exists = false;
      } else {
        this.exists = true;
        try {
          readHeader();
        } catch (final Exception e) {
          throw Exceptions.wrap("Unable to read DEM: " + this.resource, e);
        }
      }
    }
  }

  public boolean isMemoryMapped() {
    return this.memoryMapped;
  }

  @Override
  public GriddedElevationModel read() {
    init();
    if (this.exists) {
      try {
        final ChannelReader reader = this.reader;
        final int gridWidth = this.gridWidth;
        final int gridHeight = this.gridHeight;
        final int cellCount = gridWidth * gridHeight;
        final int[] elevations = new int[cellCount];
        for (int gridY = gridHeight - 1; gridY >= 0; gridY--) {
          int index = gridY * gridWidth;
          for (int gridX = 0; gridX < gridWidth; gridX++) {
            final int elevation = reader.getInt();
            elevations[index++] = elevation;
          }
        }
        final IntArrayScaleGriddedElevationModel elevationModel = new IntArrayScaleGriddedElevationModel(
          this.geometryFactory, this.boundingBox, gridWidth, gridHeight, this.gridCellSize,
          elevations);
        elevationModel.setResource(this.resource);
        return elevationModel;
      } catch (final RuntimeException e) {
        if (Exceptions.isException(e, ClosedByInterruptException.class)) {
          return null;
        } else {
          throw Exceptions.wrap("Unable to read DEM: " + this.resource, e);
        }
      }
    } else {
      return null;
    }
  }

  private void readHeader() {
    this.reader.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    final int south = this.reader.getInt();
    final int north = this.reader.getInt();
    final int west = this.reader.getInt();
    final int east = this.reader.getInt();

    final short cellHeight = this.reader.getShort();
    final short cellWidth = this.reader.getShort();

    final short gridType = this.reader.getShort();
    final short dataType = this.reader.getShort();

    final double scaleZ = this.reader.getDouble();

    final long zByteCount = this.reader.getShort();

    this.reader.getBytes(6);

    final short dataDescription = this.reader.getShort();
    final short subType = this.reader.getShort();
    final short datum = this.reader.getShort();
    final short ellipsoid = this.reader.getShort();
    if (datum == 0) {
      this.geometryFactory = GeometryFactory.fixed3d(EpsgCoordinateSystems.WGS84_ID, 3600.0, 3600.0, scaleZ);
    } else if (datum == 1) {
      this.geometryFactory = GeometryFactory.fixed3d(EpsgCoordinateSystems.NAD83_ID, 3600.0, 3600.0,
        scaleZ);
    } else {
    }
    final short byteOrder = this.reader.getShort();
    final short scaleFlag = this.reader.getShort();

    final double geopotentialWo = this.reader.getDouble();
    final double gm = this.reader.getDouble();
    final short tideSystem = this.reader.getShort();
    final short refRealization = this.reader.getShort();
    final double epoch = this.reader.getFloat();
    final short recordType = this.reader.getShort();
    final short blank = this.reader.getShort();

    final double minY = south / 3600.0;
    final double maxY = north / 3600.0;
    final double minX = west / 3600.0;
    final double maxX = east / 3600.0;

    this.gridCellSize = cellWidth / 3600.0;
    this.gridWidth = (east - west) / cellWidth + 1;
    this.gridHeight = (north - south) / cellHeight + 1;

    this.boundingBox = this.geometryFactory.newBoundingBox(2, minX, minY, maxX, maxY);
  }

  public void setByteBuffer(final ByteBuffer byteBuffer) {
    this.byteBuffer = byteBuffer;
  }

  public void setMemoryMapped(final boolean memoryMapped) {
    this.memoryMapped = memoryMapped;
  }
}
