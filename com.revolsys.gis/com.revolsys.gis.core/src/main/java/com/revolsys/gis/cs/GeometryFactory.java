package com.revolsys.gis.cs;

import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesListFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

public class GeometryFactory extends
  com.vividsolutions.jts.geom.GeometryFactory {

  private final CoordinateSystem coordinateSystem;

  public GeometryFactory(
    CoordinateSystem coordinateSystem,
    PrecisionModel precisionModel) {
    super(precisionModel, coordinateSystem.getId(),
      new DoubleCoordinatesListFactory());
    this.coordinateSystem = coordinateSystem;
  }

  public GeometryFactory(
    CoordinateSystem coordinateSystem) {
    this(coordinateSystem, new PrecisionModel());
  }

  public CoordinateSystem getCoordinateSystem() {
    return coordinateSystem;
  }

  @Override
  public String toString() {
    return coordinateSystem.getName() + ", precision=" + getPrecisionModel();
  }
}
