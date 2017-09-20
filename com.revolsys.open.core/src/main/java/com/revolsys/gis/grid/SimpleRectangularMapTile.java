package com.revolsys.gis.grid;

import com.revolsys.datatype.DataType;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.record.RecordState;

public class SimpleRectangularMapTile implements RectangularMapTile {

  private final BoundingBox boundingBox;

  private final String formattedName;

  private final RectangularMapGrid grid;

  private final String name;

  public SimpleRectangularMapTile(final RectangularMapGrid grid, final String formattedName,
    final String name, final BoundingBox boundingBox) {
    this.grid = grid;
    this.name = name;
    this.formattedName = formattedName;
    this.boundingBox = boundingBox;
  }

  @Override
  public SimpleRectangularMapTile clone() {
    try {
      return (SimpleRectangularMapTile)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    } else if (object == null) {
      return false;
    } else if (object instanceof SimpleRectangularMapTile) {
      final SimpleRectangularMapTile tile = (SimpleRectangularMapTile)object;
      if (DataType.equal(this.boundingBox, tile.boundingBox)) {
        if (DataType.equal(this.grid, tile.grid)) {
          if (DataType.equal(this.name, tile.name)) {
            return true;
          }
        }
      }
    }
    return false;
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
  public Polygon getPolygon(final GeometryFactory factory, final int numXPoints,
    final int numYPoints) {
    return this.boundingBox.toPolygon(factory, numXPoints, numYPoints);
  }

  @Override
  public Polygon getPolygon(final int numPoints) {
    final GeometryFactory factory = GeometryFactory.floating3d(4326);
    return getPolygon(factory, numPoints);
  }

  @Override
  public Polygon getPolygon(final int numXPoints, final int numYPoints) {
    final GeometryFactory factory = GeometryFactory.floating3d(4326);
    return getPolygon(factory, numXPoints, numYPoints);
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }

  @Override
  public boolean isState(final RecordState state) {
    return RecordState.NEW == state;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
