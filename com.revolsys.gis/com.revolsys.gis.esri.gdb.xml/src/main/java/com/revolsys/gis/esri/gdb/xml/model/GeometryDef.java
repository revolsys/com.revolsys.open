package com.revolsys.gis.esri.gdb.xml.model;

import com.revolsys.gis.cs.GeometryFactory;

public class GeometryDef {
  private int avgNumPoints;

  private String geometryType;

  private boolean hasM;

  private boolean hasZ;

  private SpatialReference spatialReference;

  private double gridSize0;

  private Double gridSize1;

  private Double gridSize2;

  public GeometryDef() {
  }

  public GeometryDef(String geometryType, SpatialReference spatialReference) {
    this.geometryType = geometryType;
    this.spatialReference = spatialReference;
    GeometryFactory geometryFactory = spatialReference.getGeometryFactory();
    this.hasZ = geometryFactory.hasZ();
    this.hasM = geometryFactory.hasM();
  }

  public int getAvgNumPoints() {
    return avgNumPoints;
  }

  public void setAvgNumPoints(int avgNumPoints) {
    this.avgNumPoints = avgNumPoints;
  }

  public String getGeometryType() {
    return geometryType;
  }

  public void setGeometryType(String geometryType) {
    this.geometryType = geometryType;
  }

  public boolean isHasM() {
    return hasM;
  }

  public void setHasM(boolean hasM) {
    this.hasM = hasM;
  }

  public boolean isHasZ() {
    return hasZ;
  }

  public void setHasZ(boolean hasZ) {
    this.hasZ = hasZ;
  }

  public SpatialReference getSpatialReference() {
    return spatialReference;
  }

  public void setSpatialReference(SpatialReference spatialReference) {
    this.spatialReference = spatialReference;
  }

  public double getGridSize0() {
    return gridSize0;
  }

  public void setGridSize0(double gridSize0) {
    this.gridSize0 = gridSize0;
  }

  public Double getGridSize1() {
    return gridSize1;
  }

  public void setGridSize1(Double gridSize1) {
    this.gridSize1 = gridSize1;
  }

  public Double getGridSize2() {
    return gridSize2;
  }

  public void setGridSize2(Double gridSize2) {
    this.gridSize2 = gridSize2;
  }

}
