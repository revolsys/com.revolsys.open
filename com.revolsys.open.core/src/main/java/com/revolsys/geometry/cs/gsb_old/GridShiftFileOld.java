package com.revolsys.geometry.cs.gsb_old;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import com.revolsys.geometry.cs.gsb.GridShift;
import com.revolsys.io.channels.ChannelReader;

public class GridShiftFileOld {
  private final String overviewHeaderCountId;

  private int overviewHeaderCount;

  private final int subGridHeaderCount;

  private final int subGridCount;

  private final String shiftType;

  private final String version;

  private String fromEllipsoid = "";

  private String toEllipsoid = "";

  private final double fromSemiMajorAxis;

  private final double fromSemiMinorAxis;

  private final double toSemiMajorAxis;

  private final double toSemiMinorAxis;

  private SubGridOld[] topLevelSubGrid;

  private SubGridOld lastSubGrid;

  private transient ChannelReader in;

  public GridShiftFileOld(final ChannelReader in, final boolean loadAccuracy) throws IOException {
    this.in = in;
    in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    this.fromEllipsoid = "";
    this.toEllipsoid = "";
    this.topLevelSubGrid = null;
    this.overviewHeaderCountId = in.getString(8, StandardCharsets.ISO_8859_1);
    if (!"NUM_OREC".equals(this.overviewHeaderCountId)) {
      throw new IllegalArgumentException("Input file is not an NTv2 grid shift file");
    }
    this.overviewHeaderCount = readInt();
    if (this.overviewHeaderCount == 11) {
    } else {
      in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
      this.overviewHeaderCount = Integer.reverseBytes(this.overviewHeaderCount);
      if (this.overviewHeaderCount == 11) {
      } else {
        throw new IllegalArgumentException("Input file is not an NTv2 grid shift file");
      }
    }
    this.subGridHeaderCount = readRecordInt();
    this.subGridCount = readRecordInt();
    this.shiftType = readRecordString();
    this.version = readRecordString();
    this.fromEllipsoid = readRecordString();
    this.toEllipsoid = readRecordString();
    this.fromSemiMajorAxis = readRecordDouble();
    this.fromSemiMinorAxis = readRecordDouble();
    this.toSemiMajorAxis = readRecordDouble();
    this.toSemiMinorAxis = readRecordDouble();

    final SubGridOld[] subGrid = new SubGridOld[this.subGridCount];
    for (int i = 0; i < this.subGridCount; i++) {
      subGrid[i] = new SubGridOld(this, loadAccuracy);
    }
    this.topLevelSubGrid = createSubGridTree(subGrid);
    this.lastSubGrid = this.topLevelSubGrid[0];
  }

  /**
   * Create a tree of Sub Grids by adding each Sub Grid to its parent (where
   * it has one), and returning an array of the top level Sub Grids
   * @param subGrid an array of all Sub Grids
   * @return an array of top level Sub Grids with lower level Sub Grids set.
   */
  private SubGridOld[] createSubGridTree(final SubGridOld[] subGrid) {
    int topLevelCount = 0;
    final HashMap subGridMap = new HashMap();
    for (int i = 0; i < subGrid.length; i++) {
      if (subGrid[i].getParentSubGridName().equalsIgnoreCase("NONE")) {
        topLevelCount++;
      }
      subGridMap.put(subGrid[i].getSubGridName(), new ArrayList());
    }
    final SubGridOld[] topLevelSubGrid = new SubGridOld[topLevelCount];
    topLevelCount = 0;
    for (int i = 0; i < subGrid.length; i++) {
      if (subGrid[i].getParentSubGridName().equalsIgnoreCase("NONE")) {
        topLevelSubGrid[topLevelCount++] = subGrid[i];
      } else {
        final ArrayList parent = (ArrayList)subGridMap.get(subGrid[i].getParentSubGridName());
        parent.add(subGrid[i]);
      }
    }
    final SubGridOld[] nullArray = new SubGridOld[0];
    for (int i = 0; i < subGrid.length; i++) {
      final ArrayList subSubGrids = (ArrayList)subGridMap.get(subGrid[i].getSubGridName());
      if (subSubGrids.size() > 0) {
        final SubGridOld[] subGridArray = (SubGridOld[])subSubGrids.toArray(nullArray);
        subGrid[i].setSubGridArray(subGridArray);
      }
    }
    return topLevelSubGrid;
  }

  public String getFromEllipsoid() {
    return this.fromEllipsoid;
  }

  /**
   * Find the finest SubGridOld containing the coordinate, specified
   * in Positive West Seconds
   *
   * @param lon Longitude in Positive West Seconds
   * @param lat Latitude in Seconds
   * @return The SubGridOld found or null
   */
  private SubGridOld getSubGrid(final double lon, final double lat) {
    SubGridOld sub = null;
    for (int i = 0; i < this.topLevelSubGrid.length; i++) {
      sub = this.topLevelSubGrid[i].getSubGridForCoord(lon, lat);
      if (sub != null) {
        break;
      }
    }
    return sub;
  }

  /**
   * Get a copy of the SubGridOld tree for this file.
   *
   * @return a deep clone of the current SubGridOld tree
   */
  public SubGridOld[] getSubGridTree() {
    final SubGridOld[] clone = new SubGridOld[this.topLevelSubGrid.length];
    for (int i = 0; i < this.topLevelSubGrid.length; i++) {
      clone[i] = (SubGridOld)this.topLevelSubGrid[i].clone();
    }
    return clone;
  }

  public String getToEllipsoid() {
    return this.toEllipsoid;
  }

  /**
   * Shift a coordinate in the Forward direction of the Grid Shift File.
   *
   * @param gs A GridShift object containing the coordinate to shift
   * @return True if the coordinate is within a Sub Grid, false if not
   * @throws IOException
   */
  public boolean gridShiftForward(final GridShift gs) throws IOException {
    // Try the last sub grid first, big chance the coord is still within it
    SubGridOld subGridOld = this.lastSubGrid.getSubGridForCoord(gs.getLonPositiveWestSeconds(),
      gs.getLatSeconds());
    if (subGridOld == null) {
      subGridOld = getSubGrid(gs.getLonPositiveWestSeconds(), gs.getLatSeconds());
    }
    if (subGridOld == null) {
      return false;
    } else {
      subGridOld.interpolateGridShift(gs);
      gs.setSubGridName(subGridOld.getSubGridName());
      this.lastSubGrid = subGridOld;
      return true;
    }
  }

  /**
   * Shift a coordinate in the Reverse direction of the Grid Shift File.
   *
   * @param gs A GridShift object containing the coordinate to shift
   * @return True if the coordinate is within a Sub Grid, false if not
   * @throws IOException
   */
  public boolean gridShiftReverse(final GridShift gs) throws IOException {
    // set up the first estimate
    final GridShift forwardGs = new GridShift();
    forwardGs.setLonPositiveWestSeconds(gs.getLonPositiveWestSeconds());
    forwardGs.setLatSeconds(gs.getLatSeconds());
    for (int i = 0; i < 4; i++) {
      if (!gridShiftForward(forwardGs)) {
        return false;
      }
      forwardGs.setLonPositiveWestSeconds(
        gs.getLonPositiveWestSeconds() - forwardGs.getLonShiftPositiveWestSeconds());
      forwardGs.setLatSeconds(gs.getLatSeconds() - forwardGs.getLatShiftSeconds());
    }
    gs.setLonShiftPositiveWestSeconds(-forwardGs.getLonShiftPositiveWestSeconds());
    gs.setLatShiftSeconds(-forwardGs.getLatShiftSeconds());
    gs.setLonAccuracyAvailable(forwardGs.isLonAccuracyAvailable());
    if (forwardGs.isLonAccuracyAvailable()) {
      gs.setLonAccuracySeconds(forwardGs.getLonAccuracySeconds());
    }
    gs.setLatAccuracyAvailable(forwardGs.isLatAccuracyAvailable());
    if (forwardGs.isLatAccuracyAvailable()) {
      gs.setLatAccuracySeconds(forwardGs.getLatAccuracySeconds());
    }
    return true;
  }

  public boolean isLoaded() {
    return this.topLevelSubGrid != null;
  }

  protected float readFloat() {
    return this.in.getFloat();
  }

  protected int readInt() {
    final int value = this.in.getInt();
    final int suffix = this.in.getInt();
    return value;
  }

  protected double readRecordDouble() {
    final long prefix = this.in.getLong();
    final double value = this.in.getDouble();
    return value;
  }

  protected int readRecordInt() {
    final long prefix = this.in.getLong();
    return readInt();
  }

  protected String readRecordString() {
    final long prefix = this.in.getLong();
    final String value = this.in.getString(8, StandardCharsets.ISO_8859_1).trim();
    return value;
  }

  @Override
  public String toString() {
    final StringBuffer buf = new StringBuffer("Headers  : ");
    buf.append(this.overviewHeaderCount);
    buf.append("\nSub Hdrs : ");
    buf.append(this.subGridHeaderCount);
    buf.append("\nSub Grids: ");
    buf.append(this.subGridCount);
    buf.append("\nType     : ");
    buf.append(this.shiftType);
    buf.append("\nVersion  : ");
    buf.append(this.version);
    buf.append("\nFr Ellpsd: ");
    buf.append(this.fromEllipsoid);
    buf.append("\nTo Ellpsd: ");
    buf.append(this.toEllipsoid);
    buf.append("\nFr Maj Ax: ");
    buf.append(this.fromSemiMajorAxis);
    buf.append("\nFr Min Ax: ");
    buf.append(this.fromSemiMinorAxis);
    buf.append("\nTo Maj Ax: ");
    buf.append(this.toSemiMajorAxis);
    buf.append("\nTo Min Ax: ");
    buf.append(this.toSemiMinorAxis);
    return buf.toString();
  }

  public void unload() throws IOException {
    this.topLevelSubGrid = null;
    if (this.in != null) {
      this.in.close();
      this.in = null;
    }
  }

}
