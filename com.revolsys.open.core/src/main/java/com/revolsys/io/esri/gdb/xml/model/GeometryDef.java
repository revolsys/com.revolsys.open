package com.revolsys.io.esri.gdb.xml.model;

import com.revolsys.io.esri.gdb.xml.model.enums.GeometryType;
import com.revolsys.jts.geom.GeometryFactory;

public class GeometryDef {
  private int avgNumPoints;

  private GeometryType geometryType;

  private boolean hasM;

  private boolean hasZ;

  private SpatialReference spatialReference;

  private double gridSize0;

  private Double gridSize1;

  private Double gridSize2;

  public GeometryDef() {
  }

  public GeometryDef(final GeometryType geometryType,
    final SpatialReference spatialReference) {
    this.geometryType = geometryType;
    this.spatialReference = spatialReference;
    final GeometryFactory geometryFactory = spatialReference.getGeometryFactory();
    this.hasZ = geometryFactory.hasZ();
    this.hasM = geometryFactory.hasM();
  }

  public int getAvgNumPoints() {
    return avgNumPoints;
  }

  public GeometryType getGeometryType() {
    return geometryType;
  }

  public double getGridSize0() {
    return gridSize0;
  }

  public Double getGridSize1() {
    return gridSize1;
  }

  public Double getGridSize2() {
    return gridSize2;
  }

  public SpatialReference getSpatialReference() {
    return spatialReference;
  }

  public boolean isHasM() {
    return hasM;
  }

  public boolean isHasZ() {
    return hasZ;
  }

  public void setAvgNumPoints(final int avgNumPoints) {
    this.avgNumPoints = avgNumPoints;
  }

  public void setGeometryType(final GeometryType geometryType) {
    this.geometryType = geometryType;
  }

  public void setGridSize0(final double gridSize0) {
    this.gridSize0 = gridSize0;
  }

  public void setGridSize1(final Double gridSize1) {
    this.gridSize1 = gridSize1;
  }

  public void setGridSize2(final Double gridSize2) {
    this.gridSize2 = gridSize2;
  }

  public void setHasM(final boolean hasM) {
    this.hasM = hasM;
  }

  public void setHasZ(final boolean hasZ) {
    this.hasZ = hasZ;
  }

  public void setSpatialReference(final SpatialReference spatialReference) {
    this.spatialReference = spatialReference;
  }

}
