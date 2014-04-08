package com.revolsys.gis.model.geometry.operation.geomgraph;

import com.revolsys.jts.geom.Location;

/**
 * A TopologyLocation is the labelling of a GraphComponent's topological
 * relationship to a single Geometry.
 * <p>
 * If the parent component is an area edge, each side and the edge itself have a
 * topological location. These locations are named
 * <ul>
 * <li>ON: on the edge
 * <li>LEFT: left-hand side of the edge
 * <li>RIGHT: right-hand side
 * </ul>
 * If the parent component is a line edge or node, there is a single topological
 * relationship attribute, ON.
 * <p>
 * The possible values of a topological location are {Location.NONE,
 * Location.EXTERIOR, Location.BOUNDARY, Location.INTERIOR}
 * <p>
 * The labelling is stored in an array location[j] where where j has the values
 * ON, LEFT, RIGHT
 * 
 * @version 1.7
 */
public class TopologyLocation {

  int location[];

  public TopologyLocation(final int on) {
    init(1);
    location[Position.ON] = on;
  }

  /**
   * Constructs a TopologyLocation specifying how points on, to the left of, and
   * to the right of some GraphComponent relate to some Geometry. Possible
   * values for the parameters are Location.NULL, Location.EXTERIOR,
   * Location.BOUNDARY, and Location.INTERIOR.
   * 
   * @see Location
   */
  public TopologyLocation(final int on, final int left, final int right) {
    init(3);
    location[Position.ON] = on;
    location[Position.LEFT] = left;
    location[Position.RIGHT] = right;
  }

  public TopologyLocation(final int[] location) {
    init(location.length);
  }

  public TopologyLocation(final TopologyLocation gl) {
    init(gl.location.length);
    if (gl != null) {
      for (int i = 0; i < location.length; i++) {
        location[i] = gl.location[i];
      }
    }
  }

  public boolean allPositionsEqual(final int loc) {
    for (int i = 0; i < location.length; i++) {
      if (location[i] != loc) {
        return false;
      }
    }
    return true;
  }

  public void flip() {
    if (location.length <= 1) {
      return;
    }
    final int temp = location[Position.LEFT];
    location[Position.LEFT] = location[Position.RIGHT];
    location[Position.RIGHT] = temp;
  }

  public int get(final int posIndex) {
    if (posIndex < location.length) {
      return location[posIndex];
    }
    return Location.NONE;
  }

  public int[] getLocations() {
    return location;
  }

  private void init(final int size) {
    location = new int[size];
    setAllLocations(Location.NONE);
  }

  /**
   * @return true if any locations are NULL
   */
  public boolean isAnyNull() {
    for (int i = 0; i < location.length; i++) {
      if (location[i] == Location.NONE) {
        return true;
      }
    }
    return false;
  }

  public boolean isArea() {
    return location.length > 1;
  }

  public boolean isEqualOnSide(final TopologyLocation le, final int locIndex) {
    return location[locIndex] == le.location[locIndex];
  }

  public boolean isLine() {
    return location.length == 1;
  }

  /**
   * @return true if all locations are NULL
   */
  public boolean isNull() {
    for (int i = 0; i < location.length; i++) {
      if (location[i] != Location.NONE) {
        return false;
      }
    }
    return true;
  }

  /**
   * merge updates only the NULL attributes of this object with the attributes
   * of another.
   */
  public void merge(final TopologyLocation gl) {
    // if the src is an Area label & and the dest is not, increase the dest to
    // be an Area
    if (gl.location.length > location.length) {
      final int[] newLoc = new int[3];
      newLoc[Position.ON] = location[Position.ON];
      newLoc[Position.LEFT] = Location.NONE;
      newLoc[Position.RIGHT] = Location.NONE;
      location = newLoc;
    }
    for (int i = 0; i < location.length; i++) {
      if (location[i] == Location.NONE && i < gl.location.length) {
        location[i] = gl.location[i];
      }
    }
  }

  public void setAllLocations(final int locValue) {
    for (int i = 0; i < location.length; i++) {
      location[i] = locValue;
    }
  }

  public void setAllLocationsIfNull(final int locValue) {
    for (int i = 0; i < location.length; i++) {
      if (location[i] == Location.NONE) {
        location[i] = locValue;
      }
    }
  }

  public void setLocation(final int locValue) {
    setLocation(Position.ON, locValue);
  }

  public void setLocation(final int locIndex, final int locValue) {
    location[locIndex] = locValue;
  }

  public void setLocations(final int on, final int left, final int right) {
    location[Position.ON] = on;
    location[Position.LEFT] = left;
    location[Position.RIGHT] = right;
  }

  @Override
  public String toString() {
    final StringBuffer buf = new StringBuffer();
    if (location.length > 1) {
      buf.append(Location.toLocationSymbol(location[Position.LEFT]));
    }
    buf.append(Location.toLocationSymbol(location[Position.ON]));
    if (location.length > 1) {
      buf.append(Location.toLocationSymbol(location[Position.RIGHT]));
    }
    return buf.toString();
  }
}
