package com.revolsys.gis.grid;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Polygon;

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

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  public String getFormattedName() {
    return this.formattedName;
  }

  @Override
  public RectangularMapGrid getGrid() {
    return this.grid;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public Polygon getPolygon(final GeometryFactory factory, final int numPoints) {
    return this.boundingBox.toPolygon(factory, numPoints);
  }

  @Override
  public Polygon getPolygon(final GeometryFactory factory,
    final int numXPoints, final int numYPoints) {
    return this.boundingBox.toPolygon(factory, numXPoints, numYPoints);
  }

  @Override
  public Polygon getPolygon(final int numPoints) {
    final GeometryFactory factory = GeometryFactory.floating3(4326);
    return getPolygon(factory, numPoints);
  }

  @Override
  public Polygon getPolygon(final int numXPoints, final int numYPoints) {
    final GeometryFactory factory = GeometryFactory.floating3(4326);
    return getPolygon(factory, numXPoints, numYPoints);
  }

  @Override
  public String toString() {
    return this.name;
  }
}
