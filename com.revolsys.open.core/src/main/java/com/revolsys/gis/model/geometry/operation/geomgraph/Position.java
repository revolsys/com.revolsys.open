package com.revolsys.gis.model.geometry.operation.geomgraph;

/**
 * A Position indicates the position of a Location relative to a graph component
 * (Node, Edge, or Area).
 * 
 * @version 1.7
 */
public class Position {

  /** An indicator that a Location is <i>on</i> a GraphComponent */
  public static final int ON = 0;

  /** An indicator that a Location is to the <i>left</i> of a GraphComponent */
  public static final int LEFT = 1;

  /** An indicator that a Location is to the <i>right</i> of a GraphComponent */
  public static final int RIGHT = 2;

  /**
   * Returns LEFT if the position is RIGHT, RIGHT if the position is LEFT, or
   * the position otherwise.
   */
  public static final int opposite(final int position) {
    if (position == LEFT) {
      return RIGHT;
    }
    if (position == RIGHT) {
      return LEFT;
    }
    return position;
  }
}
