package com.revolsys.gis.model.geometry.operation.chain;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;

/**
 * An interface for classes which represent a sequence of contiguous line
 * segments. SegmentStrings can carry a context object, which is useful for
 * preserving topological or parentage information.
 * 
 * @version 1.7
 */
public interface SegmentString {
  public Coordinates getCoordinate(int i);

  public CoordinatesList getCoordinates();

  /**
   * Gets the user-defined data for this segment string.
   * 
   * @return the user-defined data
   */
  public Object getData();

  public boolean isClosed();

  /**
   * Sets the user-defined data for this segment string.
   * 
   * @param data an Object containing user-defined data
   */
  public void setData(Object data);

  public int size();
}
