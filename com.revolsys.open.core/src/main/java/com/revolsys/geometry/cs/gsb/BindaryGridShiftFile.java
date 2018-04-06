package com.revolsys.geometry.cs.gsb;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.geometry.cs.Ellipsoid;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.spring.resource.Resource;

public class BindaryGridShiftFile {
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

  private final List<BindaryGridShiftGrid> grids = new ArrayList<>();

  private transient ChannelReader in;

  private GeographicCoordinateSystem fromCoordinateSystem;

  private GeographicCoordinateSystem toCoordinateSystem;

  private final CoordinatesOperation forwardOperation = this::shiftForward;

  private final CoordinatesOperation reverseOperation = this::shiftReverse;

  @SuppressWarnings("unused")
  public BindaryGridShiftFile(final Object source, final boolean loadAccuracy) {
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

  public CoordinatesOperation getForwardOperation() {
    return this.forwardOperation;
  }

  public GeographicCoordinateSystem getFromCoordinateSystem() {
    return this.fromCoordinateSystem;
  }

  public BindaryGridShiftGrid getGrid(final double lonPositiveWestSeconds,
    final double latSeconds) {
    for (final BindaryGridShiftGrid topLevelSubGrid : this.grids) {
      final BindaryGridShiftGrid bindaryGridShiftGrid = topLevelSubGrid
        .getGrid(lonPositiveWestSeconds, latSeconds);
      if (bindaryGridShiftGrid != null) {
        return bindaryGridShiftGrid;
      }
    }
    return null;
  }

  public CoordinatesOperation getReverseOperation() {
    return this.reverseOperation;
  }

  public GeographicCoordinateSystem getToCoordinateSystem() {
    return this.toCoordinateSystem;
  }

  public String getVersion() {
    return this.version;
  }

  private void loadGrids(final boolean loadAccuracy, final int gridCount) {
    final List<BindaryGridShiftGrid> grids = new ArrayList<>();
    for (int i = 0; i < gridCount; i++) {
      final BindaryGridShiftGrid grid = new BindaryGridShiftGrid(this, loadAccuracy);
      grids.add(grid);
    }
    final Map<String, BindaryGridShiftGrid> gridByName = new HashMap<>();
    for (final BindaryGridShiftGrid grid : grids) {
      if (!grid.hasParent()) {
        this.grids.add(grid);
      }
      final String name = grid.getName();
      gridByName.put(name, grid);
    }
    for (final BindaryGridShiftGrid grid : grids) {
      if (grid.hasParent()) {
        final String parentName = grid.getParentName();
        final BindaryGridShiftGrid parentGrid = gridByName.get(parentName);
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

  public void shiftForward(final int sourceAxisCount, final double[] sourceCoordinates,
    final int targetAxisCount, final double[] targetCoordinates) {
    final int sourceLength = sourceCoordinates.length;
    int targetOffset = 0;
    for (int sourceOffset = 0; sourceOffset < sourceLength; sourceOffset += sourceAxisCount) {
      final double lon = sourceCoordinates[sourceOffset];
      final double lat = sourceCoordinates[sourceOffset + 1];
      final double lonPositiveWestSeconds = -lon * 3600;
      final double latSeconds = lat * 3600;
      final BindaryGridShiftGrid bindaryGridShiftGrid = getGrid(lonPositiveWestSeconds, latSeconds);
      if (bindaryGridShiftGrid == null) {
        targetCoordinates[targetOffset] = lon;
        targetCoordinates[targetOffset + 1] = lat;
      } else {
        final double lonShift = bindaryGridShiftGrid.getLonShift(lonPositiveWestSeconds,
          latSeconds);
        final double latShift = bindaryGridShiftGrid.getLatShift(lonPositiveWestSeconds,
          latSeconds);

        targetCoordinates[targetOffset] = -(lonPositiveWestSeconds + lonShift) / 3600;
        targetCoordinates[targetOffset + 1] = (latSeconds + latShift) / 3600;
      }
      for (int axisIndex = 2; axisIndex < targetAxisCount; axisIndex++) {
        double value;
        if (axisIndex < sourceAxisCount) {
          value = sourceCoordinates[sourceOffset + axisIndex];
        } else {
          value = Double.NaN;
        }
        targetCoordinates[targetOffset + axisIndex] = value;
      }
      targetOffset += targetAxisCount;
    }
  }

  // final GridShift forwardGs = new GridShift();
  // forwardGs.setLonPositiveWestSeconds(gs.getLonPositiveWestSeconds());
  // forwardGs.setLatSeconds(gs.getLatSeconds());
  // for (int i = 0; i < 4; i++) {
  // if (!gridShiftForward(forwardGs)) {
  // return false;
  // }
  // forwardGs.setLonPositiveWestSeconds(
  // gs.getLonPositiveWestSeconds() - forwardGs.getLonShiftPositiveWestSeconds());
  // forwardGs.setLatSeconds(gs.getLatSeconds() - forwardGs.getLatShiftSeconds());
  // }
  // gs.setLonShiftPositiveWestSeconds(-forwardGs.getLonShiftPositiveWestSeconds());
  // gs.setLatShiftSeconds(-forwardGs.getLatShiftSeconds());

  public void shiftReverse(final int sourceAxisCount, final double[] sourceCoordinates,
    final int targetAxisCount, final double[] targetCoordinates) {
    final int sourceLength = sourceCoordinates.length;
    int targetOffset = 0;
    for (int sourceOffset = 0; sourceOffset < sourceLength; sourceOffset += sourceAxisCount) {
      final double lon = sourceCoordinates[sourceOffset];
      final double lat = sourceCoordinates[sourceOffset + 1];
      final double lonPositiveWestSeconds = -lon * 3600;
      final double latSeconds = lat * 3600;
      double lonShift = 0;
      double latShift = 0;
      for (int i = 0; i < 4; i++) {
        final double forwardLonPositiveWestSeconds = lonPositiveWestSeconds - lonShift;
        final double forwardLatSeconds = latSeconds - latShift;
        final BindaryGridShiftGrid bindaryGridShiftGrid = getGrid(forwardLonPositiveWestSeconds,
          forwardLatSeconds);
        if (bindaryGridShiftGrid == null) {
          targetCoordinates[targetOffset] = lon;
          targetCoordinates[targetOffset + 1] = lat;
          break;
        } else {
          lonShift = bindaryGridShiftGrid.getLonShift(forwardLonPositiveWestSeconds,
            forwardLatSeconds);
          latShift = bindaryGridShiftGrid.getLatShift(forwardLonPositiveWestSeconds,
            forwardLatSeconds);
        }
      }
      targetCoordinates[targetOffset] = -(lonPositiveWestSeconds - lonShift) / 3600;
      targetCoordinates[targetOffset + 1] = (latSeconds - latShift) / 3600;
      for (int axisIndex = 2; axisIndex < targetAxisCount; axisIndex++) {
        double value;
        if (axisIndex < sourceAxisCount) {
          value = sourceCoordinates[sourceOffset + axisIndex];
        } else {
          value = Double.NaN;
        }
        targetCoordinates[targetOffset + axisIndex] = value;
      }
      targetOffset += targetAxisCount;
    }
  }

  @Override
  public String toString() {
    return this.fromCoordinateSystem + " -> " + this.toCoordinateSystem;
  }

}
