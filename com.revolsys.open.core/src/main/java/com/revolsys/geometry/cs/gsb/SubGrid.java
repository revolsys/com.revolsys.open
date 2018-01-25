/*
 * Copyright (c) 2003 Objectix Pty Ltd  All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL OBJECTIX PTY LTD BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.revolsys.geometry.cs.gsb;

import java.io.IOException;
import java.io.Serializable;

import com.revolsys.elevation.gridded.FloatArrayGriddedElevationModel;

/**
 * Models the NTv2 Sub Grid within a Grid Shift File
 *
 * @author Peter Yuill
 */
public class SubGrid implements Cloneable, Serializable {
  private final String subGridName;

  private final String parentSubGridName;

  private final String created;

  private final String updated;

  private final double minLat;

  private final double maxLat;

  private final double minLon;

  private final double maxLon;

  private final double latInterval;

  private final double lonInterval;

  private final int nodeCount;

  private final int lonColumnCount;

  private final int latRowCount;

  private final FloatArrayGriddedElevationModel latShifts;

  private final FloatArrayGriddedElevationModel lonShifts;

  private FloatArrayGriddedElevationModel latAccuracies;

  private FloatArrayGriddedElevationModel lonAccuracies;

  private long subGridOffset;

  private SubGrid[] subGrid;

  /**
   * Construct a Sub Grid from an InputStream, loading the node data into
   * arrays in this object.
   *
   * @param in GridShiftFile InputStream
   * @param bigEndian is the file bigEndian?
   * @param loadAccuracy is the node Accuracy data to be loaded?
   * @throws Exception
   */
  public SubGrid(final GridShiftFile file, final boolean loadAccuracy) throws IOException {
    this.subGridName = file.readRecordString();
    this.parentSubGridName = file.readRecordString();
    this.created = file.readRecordString();
    this.updated = file.readRecordString();
    this.minLat = file.readRecordDouble();
    this.maxLat = file.readRecordDouble();
    this.minLon = file.readRecordDouble();
    this.maxLon = file.readRecordDouble();
    this.latInterval = file.readRecordDouble();
    this.lonInterval = file.readRecordDouble();
    if (this.latInterval != this.lonInterval) {
      throw new IllegalStateException("latInterval != lonInterval");
    }
    this.lonColumnCount = 1 + (int)((this.maxLon - this.minLon) / this.lonInterval);
    this.latRowCount = 1 + (int)((this.maxLat - this.minLat) / this.latInterval);
    final int nodeCount = file.readRecordInt();
    this.nodeCount = nodeCount;
    if (nodeCount != this.lonColumnCount * this.latRowCount) {
      throw new IllegalStateException(
        "SubGridOld " + this.subGridName + " has inconsistent grid dimensions");
    }
    final float[] latShifts = new float[nodeCount];
    final float[] lonShifts = new float[nodeCount];
    final float[] latAccuracies;
    final float[] lonAccuracies;
    if (loadAccuracy) {
      latAccuracies = new float[nodeCount];
      lonAccuracies = new float[nodeCount];
    } else {
      latAccuracies = null;
      lonAccuracies = null;
    }

    for (int i = 0; i < nodeCount; i++) {
      final float latShift = file.readFloat();
      latShifts[i] = latShift;
      final float lonShift = file.readFloat();
      lonShifts[i] = lonShift;

      final float latAccuracy = file.readFloat();
      if (loadAccuracy) {
        latAccuracies[i] = latAccuracy;
      }
      final float lonAccuracy = file.readFloat();
      if (loadAccuracy) {
        lonAccuracies[i] = lonAccuracy;
      }
    }

    this.latShifts = new FloatArrayGriddedElevationModel(null, this.minLon, this.minLat,
      this.lonColumnCount, this.latRowCount, this.lonInterval, latShifts);
    this.lonShifts = new FloatArrayGriddedElevationModel(null, this.minLon, this.minLat,
      this.lonColumnCount, this.latRowCount, this.lonInterval, latShifts);
    if (loadAccuracy) {
      this.latAccuracies = new FloatArrayGriddedElevationModel(null, this.minLon, this.minLat,
        this.lonColumnCount, this.latRowCount, this.lonInterval, latAccuracies);
      this.lonAccuracies = new FloatArrayGriddedElevationModel(null, this.minLon, this.minLat,
        this.lonColumnCount, this.latRowCount, this.lonInterval, lonAccuracies);
    }
  }

  /**
   * Make a deep clone of this Sub Grid
   */
  @Override
  public Object clone() {
    SubGrid clone = null;
    try {
      clone = (SubGrid)super.clone();
    } catch (final CloneNotSupportedException cnse) {
    }
    // Do a deep clone of the sub grids
    if (this.subGrid != null) {
      clone.subGrid = new SubGrid[this.subGrid.length];
      for (int i = 0; i < this.subGrid.length; i++) {
        clone.subGrid[i] = (SubGrid)this.subGrid[i].clone();
      }
    }
    return clone;
  }

  public String getDetails() {
    final StringBuffer buf = new StringBuffer("Sub Grid : ");
    buf.append(this.subGridName);
    buf.append("\nParent   : ");
    buf.append(this.parentSubGridName);
    buf.append("\nCreated  : ");
    buf.append(this.created);
    buf.append("\nUpdated  : ");
    buf.append(this.updated);
    buf.append("\nMin Lat  : ");
    buf.append(this.minLat);
    buf.append("\nMax Lat  : ");
    buf.append(this.maxLat);
    buf.append("\nMin Lon  : ");
    buf.append(this.minLon);
    buf.append("\nMax Lon  : ");
    buf.append(this.maxLon);
    buf.append("\nLat Intvl: ");
    buf.append(this.latInterval);
    buf.append("\nLon Intvl: ");
    buf.append(this.lonInterval);
    buf.append("\nNode Cnt : ");
    buf.append(this.nodeCount);
    return buf.toString();
  }

  /**
   * @return
   */
  public double getMaxLat() {
    return this.maxLat;
  }

  /**
   * @return
   */
  public double getMaxLon() {
    return this.maxLon;
  }

  /**
   * @return
   */
  public double getMinLat() {
    return this.minLat;
  }

  /**
   * @return
   */
  public double getMinLon() {
    return this.minLon;
  }

  public int getNodeCount() {
    return this.nodeCount;
  }

  public String getParentSubGridName() {
    return this.parentSubGridName;
  }

  public SubGrid getSubGrid(final int index) {
    return this.subGrid == null ? null : this.subGrid[index];
  }

  public int getSubGridCount() {
    return this.subGrid == null ? 0 : this.subGrid.length;
  }

  /**
   * Tests if a specified coordinate is within this Sub Grid
   * or one of its Sub Grids. If the coordinate is outside
   * this Sub Grid, null is returned. If the coordinate is
   * within this Sub Grid, but not within any of its Sub Grids,
   * this Sub Grid is returned. If the coordinate is within
   * one of this Sub Grid's Sub Grids, the method is called
   * recursively on the child Sub Grid.
   *
   * @param lon Longitude in Positive West Seconds
   * @param lat Latitude in Seconds
   * @return the Sub Grid containing the Coordinate or null
   */
  public SubGrid getSubGridForCoord(final double lon, final double lat) {
    if (isCoordWithin(lon, lat)) {
      if (this.subGrid == null) {
        return this;
      } else {
        for (int i = 0; i < this.subGrid.length; i++) {
          if (this.subGrid[i].isCoordWithin(lon, lat)) {
            return this.subGrid[i].getSubGridForCoord(lon, lat);
          }
        }
        return this;
      }
    } else {
      return null;
    }
  }

  public String getSubGridName() {
    return this.subGridName;
  }

  /**
   * Interpolate shift and accuracy values for a coordinate in the 'from' datum
   * of the GridShiftFile. The algorithm is described in
   * 'GDAit Software Architecture Manual' produced by the <a
   * href='http://www.sli.unimelb.edu.au/gda94'>Geomatics
   * Department of the University of Melbourne</a>
   * <p>This method is thread safe for both memory based and file based node data.
   * @param gs GridShift object containing the coordinate to shift and the shift values
   * @return the GridShift object supplied, with values updated.
   * @throws IOException
   */
  public GridShift interpolateGridShift(final GridShift gs) throws IOException {
    final double x = gs.getLonPositiveWestSeconds();
    final double y = gs.getLatSeconds();

    final double lonShift = this.lonShifts.getElevationBilinear(x, y);
    gs.setLonShiftPositiveWestSeconds(lonShift);

    final double latShift = this.latShifts.getElevationBilinear(x, y);
    gs.setLatShiftSeconds(latShift);

    if (this.lonAccuracies == null) {
      gs.setLonAccuracyAvailable(false);
    } else {
      gs.setLonAccuracyAvailable(true);
      final double lonAccuracy = this.lonAccuracies.getElevationBilinear(x, y);
      gs.setLonAccuracySeconds(lonAccuracy);
    }

    if (this.latAccuracies == null) {
      gs.setLatAccuracyAvailable(false);
    } else {
      gs.setLatAccuracyAvailable(true);
      final double latAccuracy = this.latAccuracies.getElevationBilinear(x, y);
      gs.setLatAccuracySeconds(latAccuracy);
    }
    return gs;
  }

  /**
   * Tests if a specified coordinate is within this Sub Grid.
   * A coordinate on either outer edge (maximum Latitude or
   * maximum Longitude) is deemed to be outside the grid.
   *
   * @param lon Longitude in Positive West Seconds
   * @param lat Latitude in Seconds
   * @return true or false
   */
  private boolean isCoordWithin(final double lon, final double lat) {
    if (lon >= this.minLon && lon < this.maxLon && lat >= this.minLat && lat < this.maxLat) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Set an array of Sub Grids of this sub grid
   * @param subGrid
   */
  public void setSubGridArray(final SubGrid[] subGrid) {
    this.subGrid = subGrid;
  }

  @Override
  public String toString() {
    return this.subGridName;
  }

}
