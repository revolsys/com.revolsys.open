package com.revolsys.gis.grid;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public class SimpleRectangularMapTile implements RectangularMapTile {

  private final BoundingBox boundingBox;

  private final String formattedName;

  private final RectangularMapGrid grid;

  private final String name;

  public SimpleRectangularMapTile(final RectangularMapGrid grid,
    final String formattedName, final String name, final BoundingBox boundingBox) {
    this.grid = grid;
    this.name = name;
    this.formattedName = formattedName;
    this.boundingBox = boundingBox;
  }

  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  public String getFormattedName() {
    return formattedName;
  }

  public RectangularMapGrid getGrid() {
    return grid;
  }

  public String getName() {
    return name;
  }

  public Polygon getPolygon(final GeometryFactory factory, final int numPoints) {
    return boundingBox.toPolygon(factory, numPoints);
  }

  public Polygon getPolygon(
    final GeometryFactory factory,
    final int numXPoints,
    final int numYPoints) {
    return boundingBox.toPolygon(factory, numXPoints, numYPoints);
  }

  public Polygon getPolygon(final int numPoints) {
    final GeometryFactory factory = GeometryFactory.getFactory(4326);
    return getPolygon(factory, numPoints);
  }

  public Polygon getPolygon(final int numXPoints, final int numYPoints) {
    final GeometryFactory factory = GeometryFactory.getFactory(4326);
    return getPolygon(factory, numXPoints, numYPoints);
  }

  @Override
  public String toString() {
    return name;
  }
}
