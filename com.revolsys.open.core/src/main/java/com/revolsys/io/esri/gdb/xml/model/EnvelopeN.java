package com.revolsys.io.esri.gdb.xml.model;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.jts.geom.BoundingBox;

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
      this.xMin = boundingBox.getMinX();
      this.yMin = boundingBox.getMinY();
      this.xMax = boundingBox.getMaxX();
      this.yMax = boundingBox.getMaxY();
      this.zMin = boundingBox.getMin(2);
      this.zMax = boundingBox.getMax(2);
      this.spatialReference = spatialReference;
    }
  }

  public double getMMax() {
    return this.mMax;
  }

  public double getMMin() {
    return this.mMin;
  }

  public SpatialReference getSpatialReference() {
    return this.spatialReference;
  }

  public double getXMax() {
    return this.xMax;
  }

  public double getXMin() {
    return this.xMin;
  }

  public double getYMax() {
    return this.yMax;
  }

  public double getYMin() {
    return this.yMin;
  }

  public double getZMax() {
    return this.zMax;
  }

  public double getZMin() {
    return this.zMin;
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
