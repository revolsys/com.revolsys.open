package com.revolsys.gis.esri.gdb.xml.parser;

public class GeometryDef {
  private int avgNumPoints;

  private String geometryType;

  private boolean hasM;

  private boolean hasZ;

  private SpatialReference spatialReference;

  private double gridSize0;

  private double gridSize1;

  private double gridSize2;

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

  public double getGridSize1() {
    return gridSize1;
  }

  public void setGridSize1(double gridSize1) {
    this.gridSize1 = gridSize1;
  }

  public double getGridSize2() {
    return gridSize2;
  }

  public void setGridSize2(double gridSize2) {
    this.gridSize2 = gridSize2;
  }

}
