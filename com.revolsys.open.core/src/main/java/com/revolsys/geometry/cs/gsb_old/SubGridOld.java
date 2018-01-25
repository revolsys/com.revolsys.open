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
package com.revolsys.geometry.cs.gsb_old;

import java.io.IOException;
import java.io.Serializable;

import com.revolsys.geometry.cs.gsb.GridShift;

/**
 * Models the NTv2 Sub Grid within a Grid Shift File
 *
 * @author Peter Yuill
 */
public class SubGridOld implements Cloneable, Serializable {
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

  private final float[] latShift;

  private final float[] lonShift;

  private float[] latAccuracy;

  private float[] lonAccuracy;

  private long subGridOffset;

  private SubGridOld[] subGridOld;

  /**
   * Construct a Sub Grid from an InputStream, loading the node data into
   * arrays in this object.
   *
   * @param in GridShiftFile InputStream
   * @param bigEndian is the file bigEndian?
   * @param loadAccuracy is the node Accuracy data to be loaded?
   * @throws Exception
   */
  public SubGridOld(final GridShiftFileOld file, final boolean loadAccuracy) throws IOException {
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
    this.lonColumnCount = 1 + (int)((this.maxLon - this.minLon) / this.lonInterval);
    this.latRowCount = 1 + (int)((this.maxLat - this.minLat) / this.latInterval);
    final int nodeCount = file.readRecordInt();
    this.nodeCount = nodeCount;
    if (nodeCount != this.lonColumnCount * this.latRowCount) {
      throw new IllegalStateException(
        "SubGridOld " + this.subGridName + " has inconsistent grid dimesions");
    }
    this.latShift = new float[nodeCount];
    this.lonShift = new float[nodeCount];
    if (loadAccuracy) {
      this.latAccuracy = new float[nodeCount];
      this.lonAccuracy = new float[nodeCount];
    }

    for (int i = 0; i < nodeCount; i++) {
      this.latShift[i] = file.readFloat();
      this.lonShift[i] = file.readFloat();

      final float latAccuracy = file.readFloat();
      if (loadAccuracy) {
        this.latAccuracy[i] = latAccuracy;
      }
      final float lonAccuracy = file.readFloat();
      if (loadAccuracy) {
        this.lonAccuracy[i] = lonAccuracy;
      }
    }
  }

  /**
   * Make a deep clone of this Sub Grid
   */
  @Override
  public Object clone() {
    SubGridOld clone = null;
    try {
      clone = (SubGridOld)super.clone();
    } catch (final CloneNotSupportedException cnse) {
    }
    // Do a deep clone of the sub grids
    if (this.subGridOld != null) {
      clone.subGridOld = new SubGridOld[this.subGridOld.length];
      for (int i = 0; i < this.subGridOld.length; i++) {
        clone.subGridOld[i] = (SubGridOld)this.subGridOld[i].clone();
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

  public SubGridOld getSubGrid(final int index) {
    return this.subGridOld == null ? null : this.subGridOld[index];
  }

  public int getSubGridCount() {
    return this.subGridOld == null ? 0 : this.subGridOld.length;
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
  public SubGridOld getSubGridForCoord(final double lon, final double lat) {
    if (isCoordWithin(lon, lat)) {
      if (this.subGridOld == null) {
        return this;
      } else {
        for (int i = 0; i < this.subGridOld.length; i++) {
          if (this.subGridOld[i].isCoordWithin(lon, lat)) {
            return this.subGridOld[i].getSubGridForCoord(lon, lat);
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
   * Bi-Linear interpolation of four nearest node values as described in
   * 'GDAit Software Architecture Manual' produced by the <a
   * href='http://www.sli.unimelb.edu.au/gda94'>Geomatics
   * Department of the University of Melbourne</a>
   * @param a value at the A node
   * @param b value at the B node
   * @param c value at the C node
   * @param d value at the D node
   * @param X Longitude factor
   * @param Y Latitude factor
   * @return interpolated value
   */
  private final double interpolate(final float a, final float b, final float c, final float d,
    final double X, final double Y) {
    return a + ((double)b - (double)a) * X + ((double)c - (double)a) * Y
      + ((double)a + (double)d - b - c) * X * Y;
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
    final int lonIndex = (int)((gs.getLonPositiveWestSeconds() - this.minLon) / this.lonInterval);
    final int latIndex = (int)((gs.getLatSeconds() - this.minLat) / this.latInterval);

    final double X = (gs.getLonPositiveWestSeconds() - (this.minLon + this.lonInterval * lonIndex))
      / this.lonInterval;
    final double Y = (gs.getLatSeconds() - (this.minLat + this.latInterval * latIndex))
      / this.latInterval;

    // Find the nodes at the four corners of the cell

    final int indexA = lonIndex + latIndex * this.lonColumnCount;
    final int indexB = indexA + 1;
    final int indexC = indexA + this.lonColumnCount;
    final int indexD = indexC + 1;

    gs.setLonShiftPositiveWestSeconds(interpolate(this.lonShift[indexA], this.lonShift[indexB],
      this.lonShift[indexC], this.lonShift[indexD], X, Y));

    gs.setLatShiftSeconds(interpolate(this.latShift[indexA], this.latShift[indexB],
      this.latShift[indexC], this.latShift[indexD], X, Y));

    if (this.lonAccuracy == null) {
      gs.setLonAccuracyAvailable(false);
    } else {
      gs.setLonAccuracyAvailable(true);
      gs.setLonAccuracySeconds(interpolate(this.lonAccuracy[indexA], this.lonAccuracy[indexB],
        this.lonAccuracy[indexC], this.lonAccuracy[indexD], X, Y));
    }

    if (this.latAccuracy == null) {
      gs.setLatAccuracyAvailable(false);
    } else {
      gs.setLatAccuracyAvailable(true);
      gs.setLatAccuracySeconds(interpolate(this.latAccuracy[indexA], this.latAccuracy[indexB],
        this.latAccuracy[indexC], this.latAccuracy[indexD], X, Y));
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
   * @param subGridOld
   */
  public void setSubGridArray(final SubGridOld[] subGrid) {
    this.subGridOld = subGrid;
  }

  @Override
  public String toString() {
    return this.subGridName;
  }

}
