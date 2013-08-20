package com.revolsys.gis.model.geometry.operation.geomgraph;

import com.vividsolutions.jts.geom.Location;

/**
 * A Depth object records the topological depth of the sides of an Edge for up
 * to two Geometries.
 * 
 * @version 1.7
 */
public class Depth {

  private final static int NULL_VALUE = -1;

  public static int depthAtLocation(final int location) {
    if (location == Location.EXTERIOR) {
      return 0;
    }
    if (location == Location.INTERIOR) {
      return 1;
    }
    return NULL_VALUE;
  }

  private final int[][] depth = new int[2][3];

  public Depth() {
    // initialize depth array to a sentinel value
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 3; j++) {
        depth[i][j] = NULL_VALUE;
      }
    }
  }

  public void add(final int geomIndex, final int posIndex, final int location) {
    if (location == Location.INTERIOR) {
      depth[geomIndex][posIndex]++;
    }
  }

  public void add(final Label lbl) {
    for (int i = 0; i < 2; i++) {
      for (int j = 1; j < 3; j++) {
        final int loc = lbl.getLocation(i, j);
        if (loc == Location.EXTERIOR || loc == Location.INTERIOR) {
          // initialize depth if it is null, otherwise add this location value
          if (isNull(i, j)) {
            depth[i][j] = depthAtLocation(loc);
          } else {
            depth[i][j] += depthAtLocation(loc);
          }
        }
      }
    }
  }

  public int getDelta(final int geomIndex) {
    return depth[geomIndex][Position.RIGHT] - depth[geomIndex][Position.LEFT];
  }

  public int getDepth(final int geomIndex, final int posIndex) {
    return depth[geomIndex][posIndex];
  }

  public int getLocation(final int geomIndex, final int posIndex) {
    if (depth[geomIndex][posIndex] <= 0) {
      return Location.EXTERIOR;
    }
    return Location.INTERIOR;
  }

  /**
   * A Depth object is null (has never been initialized) if all depths are null.
   */
  public boolean isNull() {
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 3; j++) {
        if (depth[i][j] != NULL_VALUE) {
          return false;
        }
      }
    }
    return true;
  }

  public boolean isNull(final int geomIndex) {
    return depth[geomIndex][1] == NULL_VALUE;
  }

  public boolean isNull(final int geomIndex, final int posIndex) {
    return depth[geomIndex][posIndex] == NULL_VALUE;
  }

  /**
   * Normalize the depths for each geometry, if they are non-null. A normalized
   * depth has depth values in the set { 0, 1 }. Normalizing the depths involves
   * reducing the depths by the same amount so that at least one of them is 0.
   * If the remaining value is > 0, it is set to 1.
   */
  public void normalize() {
    for (int i = 0; i < 2; i++) {
      if (!isNull(i)) {
        int minDepth = depth[i][1];
        if (depth[i][2] < minDepth) {
          minDepth = depth[i][2];
        }

        if (minDepth < 0) {
          minDepth = 0;
        }
        for (int j = 1; j < 3; j++) {
          int newValue = 0;
          if (depth[i][j] > minDepth) {
            newValue = 1;
          }
          depth[i][j] = newValue;
        }
      }
    }
  }

  public void setDepth(final int geomIndex, final int posIndex,
    final int depthValue) {
    depth[geomIndex][posIndex] = depthValue;
  }

  @Override
  public String toString() {
    return "A: " + depth[0][1] + "," + depth[0][2] + " B: " + depth[1][1] + ","
      + depth[1][2];
  }
}
