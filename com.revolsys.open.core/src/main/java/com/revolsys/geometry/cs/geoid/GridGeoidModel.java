package com.revolsys.geometry.cs.geoid;

import com.revolsys.geometry.cs.gridshift.VerticalShiftOperation;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.grid.Grid;

public class GridGeoidModel extends AbstractGeoidModel {

  private String geoidName;

  protected Grid grid;

  public GridGeoidModel(final String geoidName, final Grid grid) {
    super(geoidName);
    this.grid = grid;
  }

  @Override
  public VerticalShiftOperation geodeticToOrthometricHeightOperation() {
    return this::geodeticToOrthometricHeight;
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.grid.getBoundingBox();
  }

  @Override
  public double getGeoidHeight(final double x, final double y) {
    return this.grid.getValueBicubic(x, y);
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.grid.getGeometryFactory();
  }

  @Override
  public VerticalShiftOperation orthometricToGeodeticHeightOperation() {
    return this::orthometricToGeodeticHeight;
  }

  @Override
  public String toString() {
    return this.geoidName.toString();
  }
}
