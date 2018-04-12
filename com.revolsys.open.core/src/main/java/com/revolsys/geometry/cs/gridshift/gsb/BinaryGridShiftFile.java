package com.revolsys.geometry.cs.gridshift.gsb;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.geometry.cs.Ellipsoid;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.cs.gridshift.GridShiftOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.record.io.format.tsv.Tsv;
import com.revolsys.record.io.format.tsv.TsvWriter;
import com.revolsys.spring.resource.Resource;

public class BinaryGridShiftFile {
  private static GeographicCoordinateSystem getCoordinateSystem(final String name) {
    final GeographicCoordinateSystem coordinateSystem = EpsgCoordinateSystems
      .getCoordinateSystem(name);
    if (coordinateSystem == null) {
      if ("CSRS98".equals(name)) { // NAD83(CSRS98)
        return EpsgCoordinateSystems.getCoordinateSystem(4140);
      } else if ("CSRSv2".equals(name)) { // NAD83(CSRS)v2
        return EpsgCoordinateSystems.getCoordinateSystem(8237);
      } else if ("CSRSv3".equals(name)) { // NAD83(CSRS)v3
        return EpsgCoordinateSystems.getCoordinateSystem(8240);
      } else if ("CSRSv4".equals(name)) { // NAD83(CSRS)v4
        return EpsgCoordinateSystems.getCoordinateSystem(8246);
      } else if ("CSRSv5".equals(name)) { // NAD83(CSRS)v5
        return EpsgCoordinateSystems.getCoordinateSystem(8249);
      } else if ("CSRSv6".equals(name)) { // NAD83(CSRS)v6
        return EpsgCoordinateSystems.getCoordinateSystem(8252);
      } else if ("CSRSv7".equals(name)) { // NAD83(CSRS)v7
        return EpsgCoordinateSystems.getCoordinateSystem(8255);
      } else {
        return null;
      }
    } else {
      return coordinateSystem;
    }
  }

  private final String version;

  private final List<BinaryGridShiftGrid> grids = new ArrayList<>();

  private transient ChannelReader in;

  private GeographicCoordinateSystem fromCoordinateSystem;

  private GeographicCoordinateSystem toCoordinateSystem;

  private final GridShiftOperation forwardOperation = this::shiftForward;

  private final GridShiftOperation inverseOperation = this::shiftInverse;

  @SuppressWarnings("unused")
  public BinaryGridShiftFile(final Object source, final boolean loadAccuracy) {
    try (
      ChannelReader in = Resource.getResource(source).newChannelReader()) {
      this.in = in;
      in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
      final String overviewHeaderCountId = in.getString(8, StandardCharsets.ISO_8859_1);
      if (!"NUM_OREC".equals(overviewHeaderCountId)) {
        throw new IllegalArgumentException("Input file is not an NTv2 grid shift file");
      }
      int overviewHeaderCount = readInt();
      if (overviewHeaderCount == 11) {
      } else {
        in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        overviewHeaderCount = Integer.reverseBytes(overviewHeaderCount);
        if (overviewHeaderCount == 11) {
        } else {
          throw new IllegalArgumentException("Input file is not an NTv2 grid shift file");
        }
      }
      final int gridHeaderCount = readRecordInt();
      final int gridCount = readRecordInt();
      final String shiftType = readRecordString();
      if (!"SECONDS".equals(shiftType)) {
        throw new IllegalArgumentException(
          "shiftType=" + shiftType + " not supported, must be SECONDS");
      }
      this.version = readRecordString();
      final String fromEllipsoidName = readRecordString();
      final String toEllipsoidName = readRecordString();
      this.fromCoordinateSystem = readCoordinateSystem(fromEllipsoidName);
      this.toCoordinateSystem = readCoordinateSystem(toEllipsoidName);

      loadGrids(loadAccuracy, gridCount);
    } finally {
      this.in = null;
    }
  }

  public GridShiftOperation getForwardOperation() {
    return this.forwardOperation;
  }

  public GeographicCoordinateSystem getFromCoordinateSystem() {
    return this.fromCoordinateSystem;
  }

  public BinaryGridShiftGrid getGrid(final double lonPositiveWestSeconds, final double latSeconds) {
    for (final BinaryGridShiftGrid topLevelSubGrid : this.grids) {
      final BinaryGridShiftGrid binaryGridShiftGrid = topLevelSubGrid
        .getGrid(lonPositiveWestSeconds, latSeconds);
      if (binaryGridShiftGrid != null) {
        return binaryGridShiftGrid;
      }
    }
    return null;
  }

  public GridShiftOperation getInverseOperation() {
    return this.inverseOperation;
  }

  public GeographicCoordinateSystem getToCoordinateSystem() {
    return this.toCoordinateSystem;
  }

  public String getVersion() {
    return this.version;
  }

  private void loadGrids(final boolean loadAccuracy, final int gridCount) {
    try (
      TsvWriter writer = Tsv.plainWriter("/Users/paustin/Downloads/bc.tsv")) {
      writer.write("polygon");
      final List<BinaryGridShiftGrid> grids = new ArrayList<>();
      for (int i = 0; i < gridCount; i++) {
        final BinaryGridShiftGrid grid = new BinaryGridShiftGrid(this, loadAccuracy);
        grids.add(grid);
        writer.write(grid.getBoundingBox().toPolygon(100));
      }
    }
    final Map<String, BinaryGridShiftGrid> gridByName = new HashMap<>();
    for (final BinaryGridShiftGrid grid : this.grids) {
      if (!grid.hasParent()) {
        this.grids.add(grid);
      }
      final String name = grid.getName();
      gridByName.put(name, grid);
    }
    for (final BinaryGridShiftGrid grid : this.grids) {
      if (grid.hasParent()) {
        final String parentName = grid.getParentName();
        final BinaryGridShiftGrid parentGrid = gridByName.get(parentName);
        parentGrid.addGrid(grid);
      }
    }
  }

  private GeographicCoordinateSystem readCoordinateSystem(final String name) {
    final double semiMajorAxis = readRecordDouble();
    final double semiMinorAxis = readRecordDouble();
    final GeographicCoordinateSystem coordinateSystem = getCoordinateSystem(name);
    if (coordinateSystem == null) {
      final Ellipsoid ellipsoid = Ellipsoid.newMajorMinor(name, semiMajorAxis, semiMinorAxis);
      return new GeographicCoordinateSystem(name, ellipsoid);
    }
    return coordinateSystem;
  }

  protected float readFloat() {
    return this.in.getFloat();
  }

  @SuppressWarnings("unused")
  protected int readInt() {
    final int value = this.in.getInt();
    final int suffix = this.in.getInt();
    return value;
  }

  @SuppressWarnings("unused")
  protected double readRecordDouble() {
    final long prefix = this.in.getLong();
    final double value = this.in.getDouble();
    return value;
  }

  @SuppressWarnings("unused")
  protected int readRecordInt() {
    final long prefix = this.in.getLong();
    return readInt();
  }

  @SuppressWarnings("unused")
  protected String readRecordString() {
    final long prefix = this.in.getLong();
    final String value = this.in.getString(8, StandardCharsets.ISO_8859_1).trim();
    return value;
  }

  public boolean shiftForward(final CoordinatesOperationPoint point) {
    final double lon = point.x;
    final double lat = point.y;
    final double lonPositiveWestSeconds = -lon * 3600;
    final double latSeconds = lat * 3600;
    final BinaryGridShiftGrid binaryGridShiftGrid = getGrid(lonPositiveWestSeconds, latSeconds);
    if (binaryGridShiftGrid == null) {
      return false;
    } else {
      final double lonShift = binaryGridShiftGrid.getLonShift(lonPositiveWestSeconds, latSeconds);
      final double latShift = binaryGridShiftGrid.getLatShift(lonPositiveWestSeconds, latSeconds);
      point.x = -(lonPositiveWestSeconds + lonShift) / 3600;
      point.y = (latSeconds + latShift) / 3600;
      return true;
    }
  }

  public boolean shiftInverse(final CoordinatesOperationPoint point) {
    final double lon = point.x;
    final double lat = point.y;
    final double lonPositiveWestSeconds = -lon * 3600;
    final double latSeconds = lat * 3600;
    double lonShift = 0;
    double latShift = 0;
    for (int i = 0; i < 4; i++) {
      final double forwardLonPositiveWestSeconds = lonPositiveWestSeconds - lonShift;
      final double forwardLatSeconds = latSeconds - latShift;
      final BinaryGridShiftGrid binaryGridShiftGrid = getGrid(forwardLonPositiveWestSeconds,
        forwardLatSeconds);
      if (binaryGridShiftGrid == null) {
        if (i == 0) {
          return false;
        } else {
          return true;
        }
      } else {
        lonShift = binaryGridShiftGrid.getLonShift(forwardLonPositiveWestSeconds,
          forwardLatSeconds);
        latShift = binaryGridShiftGrid.getLatShift(forwardLonPositiveWestSeconds,
          forwardLatSeconds);
      }
    }
    point.x = -(lonPositiveWestSeconds - lonShift) / 3600;
    point.y = (latSeconds - latShift) / 3600;
    return true;
  }

  @Override
  public String toString() {
    return this.fromCoordinateSystem + " -> " + this.toCoordinateSystem;
  }

}
