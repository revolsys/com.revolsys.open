package com.revolsys.geometry.cs.geoid;

import com.revolsys.geometry.cs.gridshift.VerticalShiftOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.grid.Grid;
import com.revolsys.spring.resource.Resource;

public abstract class AbstractGeoidGrid implements GeoidGrid {

  protected final Resource resource;

  protected GeometryFactory geometryFactory;

  protected BoundingBox boundingBox;

  protected double gridCellWidth;

  protected int gridWidth;

  protected int gridHeight;

  protected double gridCellHeight;

  protected Grid grid;

  public AbstractGeoidGrid(final Object source) {
    this.resource = Resource.getResource(source);
    read();
  }

  @Override
  public boolean geodeticToOrthometricHeight(final CoordinatesOperationPoint point) {
    final double x = point.x;
    final double y = point.y;
    final double geoidHeight = this.grid.getValueBicubic(x, y);
    if (Double.isFinite(geoidHeight)) {
      point.z -= geoidHeight;
      return true;
    } else {
      return false;
    }
  }

  @Override
  public VerticalShiftOperation geodeticToOrthometricHeightOperation() {
    return this::geodeticToOrthometricHeight;
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  public double getGeoidHeight(final double x, final double y) {
    return this.grid.getValueBicubic(x, y);
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public boolean orthometricToGeodeticHeight(final CoordinatesOperationPoint point) {
    final double x = point.x;
    final double y = point.y;
    final double geoidHeight = this.grid.getValueBicubic(x, y);
    if (Double.isFinite(geoidHeight)) {
      point.z += geoidHeight;
      return true;
    } else {
      return false;
    }
  }

  @Override
  public VerticalShiftOperation orthometricToGeodeticHeightOperation() {
    return this::orthometricToGeodeticHeight;
  }

  protected abstract void read();

}
