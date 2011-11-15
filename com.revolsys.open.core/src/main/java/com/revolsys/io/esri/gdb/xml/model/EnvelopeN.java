package com.revolsys.io.esri.gdb.xml.model;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;

public class EnvelopeN extends Envelope {
  private double xMin;

  private double yMin;

  private double xMax;

  private double yMax;

  private double zMin;

  private double zMax;

  private double mMin;

  private double mMax;

  private SpatialReference spatialReference;

  public EnvelopeN() {
  }

  public EnvelopeN(final SpatialReference spatialReference) {
    final CoordinateSystem coordinateSystem = spatialReference.getCoordinateSystem();
    if (coordinateSystem != null) {
      final BoundingBox boundingBox = coordinateSystem.getAreaBoundingBox();
      xMin = boundingBox.getMinX();
      yMin = boundingBox.getMinY();
      xMax = boundingBox.getMaxX();
      yMax = boundingBox.getMaxY();
      zMin = boundingBox.getMinZ();
      zMax = boundingBox.getMaxZ();
      this.spatialReference = spatialReference;
    }
  }

  public double getMMax() {
    return mMax;
  }

  public double getMMin() {
    return mMin;
  }

  public SpatialReference getSpatialReference() {
    return spatialReference;
  }

  public double getXMax() {
    return xMax;
  }

  public double getXMin() {
    return xMin;
  }

  public double getYMax() {
    return yMax;
  }

  public double getYMin() {
    return yMin;
  }

  public double getZMax() {
    return zMax;
  }

  public double getZMin() {
    return zMin;
  }

  public void setMMax(final double mMax) {
    this.mMax = mMax;
  }

  public void setMMin(final double mMin) {
    this.mMin = mMin;
  }

  public void setSpatialReference(final SpatialReference spatialReference) {
    this.spatialReference = spatialReference;
  }

  public void setXMax(final double xMax) {
    this.xMax = xMax;
  }

  public void setXMin(final double xMin) {
    this.xMin = xMin;
  }

  public void setYMax(final double yMax) {
    this.yMax = yMax;
  }

  public void setYMin(final double yMin) {
    this.yMin = yMin;
  }

  public void setZMax(final double zMax) {
    this.zMax = zMax;
  }

  public void setZMin(final double zMin) {
    this.zMin = zMin;
  }

}
