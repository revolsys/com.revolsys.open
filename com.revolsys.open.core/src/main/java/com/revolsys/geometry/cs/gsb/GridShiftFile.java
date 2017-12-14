package com.revolsys.geometry.cs.gsb;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import com.revolsys.io.channels.ChannelReader;

public class GridShiftFile {

  private static final int REC_SIZE = 16;

  private String overviewHeaderCountId;

  private int overviewHeaderCount;

  private int subGridHeaderCount;

  private int subGridCount;

  private String shiftType;

  private String version;

  private String fromEllipsoid = "";

  private String toEllipsoid = "";

  private double fromSemiMajorAxis;

  private double fromSemiMinorAxis;

  private double toSemiMajorAxis;

  private double toSemiMinorAxis;

  private SubGrid[] topLevelSubGrid;

  private SubGrid lastSubGrid;

  private transient ChannelReader in;

  public GridShiftFile() {
  }

  /**
   * Create a tree of Sub Grids by adding each Sub Grid to its parent (where
   * it has one), and returning an array of the top level Sub Grids
   * @param subGrid an array of all Sub Grids
   * @return an array of top level Sub Grids with lower level Sub Grids set.
   */
  private SubGrid[] createSubGridTree(final SubGrid[] subGrid) {
    int topLevelCount = 0;
    final HashMap subGridMap = new HashMap();
    for (int i = 0; i < subGrid.length; i++) {
      if (subGrid[i].getParentSubGridName().equalsIgnoreCase("NONE")) {
        topLevelCount++;
      }
      subGridMap.put(subGrid[i].getSubGridName(), new ArrayList());
    }
    final SubGrid[] topLevelSubGrid = new SubGrid[topLevelCount];
    topLevelCount = 0;
    for (int i = 0; i < subGrid.length; i++) {
      if (subGrid[i].getParentSubGridName().equalsIgnoreCase("NONE")) {
        topLevelSubGrid[topLevelCount++] = subGrid[i];
      } else {
        final ArrayList parent = (ArrayList)subGridMap.get(subGrid[i].getParentSubGridName());
        parent.add(subGrid[i]);
      }
    }
    final SubGrid[] nullArray = new SubGrid[0];
    for (int i = 0; i < subGrid.length; i++) {
      final ArrayList subSubGrids = (ArrayList)subGridMap.get(subGrid[i].getSubGridName());
      if (subSubGrids.size() > 0) {
        final SubGrid[] subGridArray = (SubGrid[])subSubGrids.toArray(nullArray);
        subGrid[i].setSubGridArray(subGridArray);
      }
    }
    return topLevelSubGrid;
  }

  public String getFromEllipsoid() {
    return this.fromEllipsoid;
  }

  /**
   * Find the finest SubGrid containing the coordinate, specified
   * in Positive West Seconds
   *
   * @param lon Longitude in Positive West Seconds
   * @param lat Latitude in Seconds
   * @return The SubGrid found or null
   */
  private SubGrid getSubGrid(final double lon, final double lat) {
    SubGrid sub = null;
    for (int i = 0; i < this.topLevelSubGrid.length; i++) {
      sub = this.topLevelSubGrid[i].getSubGridForCoord(lon, lat);
      if (sub != null) {
        break;
      }
    }
    return sub;
  }

  /**
   * Get a copy of the SubGrid tree for this file.
   *
   * @return a deep clone of the current SubGrid tree
   */
  public SubGrid[] getSubGridTree() {
    final SubGrid[] clone = new SubGrid[this.topLevelSubGrid.length];
    for (int i = 0; i < this.topLevelSubGrid.length; i++) {
      clone[i] = (SubGrid)this.topLevelSubGrid[i].clone();
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
    SubGrid subGrid = this.lastSubGrid.getSubGridForCoord(gs.getLonPositiveWestSeconds(),
      gs.getLatSeconds());
    if (subGrid == null) {
      subGrid = getSubGrid(gs.getLonPositiveWestSeconds(), gs.getLatSeconds());
    }
    if (subGrid == null) {
      return false;
    } else {
      subGrid.interpolateGridShift(gs);
      gs.setSubGridName(subGrid.getSubGridName());
      this.lastSubGrid = subGrid;
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

  /**
   * Load a Grid Shift File from an InputStream. The Grid Shift node
   * data is stored in Java arrays, which will occupy about the same memory
   * as the original file with accuracy data included, and about half that
   * with accuracy data excluded. The size of the Australian national file
   * is 4.5MB, and the Canadian national file is 13.5MB
   * <p>The InputStream is closed by this method.
   *
   * @param in Grid Shift File InputStream
   * @param loadAccuracy is Accuracy data to be loaded as well as shift data?
   * @throws Exception
   */
  public void loadGridShiftFile(final ChannelReader in, final boolean loadAccuracy)
    throws IOException {
    final byte[] b8 = new byte[8];
    boolean bigEndian = true;
    this.fromEllipsoid = "";
    this.toEllipsoid = "";
    this.topLevelSubGrid = null;
    this.overviewHeaderCountId = in.getString(8, StandardCharsets.ISO_8859_1);
    if (!"NUM_OREC".equals(this.overviewHeaderCountId)) {
      throw new IllegalArgumentException("Input file is not an NTv2 grid shift file");
    }
    this.overviewHeaderCount = in.getInt();
    if (this.overviewHeaderCount == 11) {
      bigEndian = true;
    } else {
      this.overviewHeaderCount = Util.getIntLE(b8, 0);
      if (this.overviewHeaderCount == 11) {
        bigEndian = false;
      } else {
        throw new IllegalArgumentException("Input file is not an NTv2 grid shift file");
      }
    }
    // in.getInt();
    // in.read(b8);
    // in.read(b8);
    // this.subGridHeaderCount = Util.getInt(b8, bigEndian);
    // in.read(b8);
    // in.read(b8);
    // this.subGridCount = Util.getInt(b8, bigEndian);
    // final SubGrid[] subGrid = new SubGrid[this.subGridCount];
    // in.read(b8);
    // in.read(b8);
    // this.shiftType = new String(b8);
    // in.read(b8);
    // in.read(b8);
    // this.version = new String(b8);
    // in.read(b8);
    // in.read(b8);
    // this.fromEllipsoid = new String(b8);
    // in.read(b8);
    // in.read(b8);
    // this.toEllipsoid = new String(b8);
    // in.read(b8);
    // in.read(b8);
    // this.fromSemiMajorAxis = Util.getDouble(b8, bigEndian);
    // in.read(b8);
    // in.read(b8);
    // this.fromSemiMinorAxis = Util.getDouble(b8, bigEndian);
    // in.read(b8);
    // in.read(b8);
    // this.toSemiMajorAxis = Util.getDouble(b8, bigEndian);
    // in.read(b8);
    // in.read(b8);
    // this.toSemiMinorAxis = Util.getDouble(b8, bigEndian);
    //
    // for (int i = 0; i < this.subGridCount; i++) {
    // subGrid[i] = new SubGrid(in, bigEndian, loadAccuracy);
    // }
    // this.topLevelSubGrid = createSubGridTree(subGrid);
    // this.lastSubGrid = this.topLevelSubGrid[0];
    //
    // in.close();
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
