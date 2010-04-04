package com.revolsys.gis.grid;

import com.revolsys.gis.cs.BoundingBox;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

public class SimpleRectangularMapTile implements RectangularMapTile {

  private final BoundingBox boundingBox;

  private final RectangularMapGrid grid;

  private final String name;

  private final String formattedName;

  public String getFormattedName() {
    return formattedName;
  }

  public SimpleRectangularMapTile(
    final RectangularMapGrid grid,
    final String formattedName,
    final String name,
    final BoundingBox boundingBox) {
    this.grid = grid;
    this.name = name;
    this.formattedName = formattedName;
    this.boundingBox = boundingBox;
  }

  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  public RectangularMapGrid getGrid() {
    return grid;
  }

  public String getName() {
    return name;
  }

  public Polygon getPolygon(
    final int numPoints) {
    final GeometryFactory factory = new GeometryFactory(new PrecisionModel(),
      4326);
    final double width = boundingBox.getWidth();
    final double height = boundingBox.getHeight();
    final double xStep = width / numPoints;
    final double yStep = height / numPoints;

    final double minX = boundingBox.getMinX();
    final double maxX = boundingBox.getMaxX();
    final double minY = boundingBox.getMinY();
    final double maxY = boundingBox.getMaxY();

    final Coordinate coordinates[] = new Coordinate[numPoints * 4 + 1];
    for (int i = 0; i < numPoints; i++) {
      coordinates[i] = new Coordinate(minX, minY + i * yStep);
      coordinates[numPoints * 2 + i] = new Coordinate(maxX, minY
        + (numPoints - i) * yStep);
    }

    for (int i = 0; i < numPoints; i++) {
      coordinates[numPoints + i] = new Coordinate(minX + i * xStep, maxY);
      coordinates[numPoints * 3 + i] = new Coordinate(minX + (numPoints - i)
        * xStep, minY);
    }

    coordinates[coordinates.length - 1] = new Coordinate(minX, minY);
    final com.vividsolutions.jts.geom.LinearRing ring = factory.createLinearRing(coordinates);
    return factory.createPolygon(ring, null);
  }

  @Override
  public String toString() {
    return name;
  }
}
