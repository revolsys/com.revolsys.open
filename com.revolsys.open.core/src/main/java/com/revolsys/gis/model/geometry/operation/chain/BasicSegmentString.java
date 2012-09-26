package com.revolsys.gis.model.geometry.operation.chain;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Represents a list of contiguous line segments, and supports noding the
 * segments. The line segments are represented by an array of {@link Coordinate}
 * s. Intended to optimize the noding of contiguous segments by reducing the
 * number of allocated objects. SegmentStrings can carry a context object, which
 * is useful for preserving topological or parentage information. All noded
 * substrings are initialized with the same context object.
 * 
 * @version 1.7
 */
public class BasicSegmentString implements SegmentString {
  private CoordinatesList pts;

  private Object data;

  /**
   * Creates a new segment string from a list of vertices.
   * 
   * @param pts the vertices of the segment string
   * @param data the user-defined data of this segment string (may be null)
   */
  public BasicSegmentString(CoordinatesList pts, Object data) {
    this.pts = pts;
    this.data = data;
  }

  /**
   * Gets the user-defined data for this segment string.
   * 
   * @return the user-defined data
   */
  public Object getData() {
    return data;
  }

  /**
   * Sets the user-defined data for this segment string.
   * 
   * @param data an Object containing user-defined data
   */
  public void setData(Object data) {
    this.data = data;
  }

  public int size() {
    return pts.size();
  }

  public Coordinates getCoordinate(int i) {
    return pts.get(i);
  }

  public CoordinatesList getCoordinates() {
    return pts;
  }

  public boolean isClosed() {
    return pts.get(0).equals(pts.get(pts.size() - 1));
  }

  /**
   * Gets the octant of the segment starting at vertex <code>index</code>.
   * 
   * @param index the index of the vertex starting the segment. Must not be the
   *          last index in the vertex list
   * @return the octant of the segment at the vertex
   */
  public int getSegmentOctant(int index) {
    if (index == pts.size() - 1)
      return -1;
    return CoordinatesUtil.octant(getCoordinate(index),
      getCoordinate(index + 1));
  }

}
