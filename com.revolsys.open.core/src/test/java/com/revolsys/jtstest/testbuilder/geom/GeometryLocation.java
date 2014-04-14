package com.revolsys.jtstest.testbuilder.geom;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;

/**
 * Models the location of a point on a Geometry
 * 
 * @author Martin Davis
 *
 */
public class GeometryLocation {
  /**
   * The top-level geometry containing the location
   */
  private final Geometry parent;

  /**
   * The Geometry component containing the location
   */
  private final Geometry component;

  /**
   * The path of indexes to the component containing the location
   */
  private int[] componentPath;

  /**
   * The index of the vertex or segment the location occurs on
   */
  private int index;

  /**
   * Indicates whether this location is a vertex of the geometry
   */
  private boolean isVertex = true;

  /**
   * The actual Coordinates for the location
   */
  private Coordinates pt;

  public GeometryLocation(final Geometry parent, final Geometry component,
    final int segmentIndex, final boolean isVertex, final Coordinates pt) {
    this.parent = parent;
    this.component = component;
    this.index = segmentIndex;
    this.isVertex = isVertex;
    this.pt = pt;
  }

  public GeometryLocation(final Geometry parent, final Geometry component,
    final int index, final Coordinates pt) {
    this.parent = parent;
    this.component = component;
    this.index = index;
    this.pt = pt;
  }

  public GeometryLocation(final Geometry parent, final Geometry component,
    final int[] componentPath) {
    this.parent = parent;
    this.component = component;
    this.componentPath = componentPath;
  }

  public GeometryLocation(final Geometry parent, final Geometry component,
    final int[] componentPath, final int segmentIndex, final boolean isVertex,
    final Coordinates pt) {
    this.parent = parent;
    this.component = component;
    this.componentPath = componentPath;
    this.index = segmentIndex;
    this.isVertex = isVertex;
    this.pt = pt;
  }

  public Geometry delete() {
    return GeometryVertexDeleter.delete(parent, (LineString)component, index);
  }

  public Geometry getComponent() {
    return component;
  }

  public Coordinates getCoordinate() {
    return pt;
  }

  public Geometry insert() {
    return GeometryVertexInserter.insert(parent, (LineString)component, index,
      pt);
  }

  public boolean isVertex() {
    return isVertex;
  }

  public String pathString() {
    final StringBuffer buf = new StringBuffer();
    for (int i = 0; i < componentPath.length; i++) {
      if (i > 0) {
        buf.append(":");
      }
      buf.append(componentPath[i]);
    }
    return buf.toString();
  }

  public String toFacetString() {
    final StringBuffer buf = new StringBuffer();

    // facet index
    buf.append("[");
    for (int i = 0; i < componentPath.length; i++) {
      if (i > 0) {
        buf.append(":");
      }
      buf.append(componentPath[i]);
    }
    buf.append(" ");
    buf.append(index);
    if (!isVertex()) {
      buf.append("-" + (index + 1));
    }
    buf.append("]  ");

    // facet value
    buf.append(isVertex() ? "POINT " : "LINESTRING ");

    buf.append("( ");
    buf.append(pt.getX());
    buf.append(" ");
    buf.append(pt.getY());
    if (!isVertex()) {
      final Coordinates p1 = component.getCoordinateArray()[index + 1];
      buf.append(", ");
      buf.append(p1.getX());
      buf.append(" ");
      buf.append(p1.getY());
    }
    buf.append(" )");
    return buf.toString();
  }

  @Override
  public String toString() {
    return pt.toString();
  }

}
