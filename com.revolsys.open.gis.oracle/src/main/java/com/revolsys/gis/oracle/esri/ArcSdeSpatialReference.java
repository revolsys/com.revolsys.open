package com.revolsys.gis.oracle.esri;

import com.revolsys.gis.cs.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

public class ArcSdeSpatialReference {
  private String csWkt;

  private int esriSrid;

  private final GeometryFactory geometryFactory;

  private Double mOffset;

  private PrecisionModel mPrecisionModel;

  private Double mScale;

  private String name;

  private int srid;

  private Double xOffset;

  private Double xyScale;

  private Double yOffset;

  private Double zOffset;

  private PrecisionModel zPrecisionModel;

  private Double zScale;

  public ArcSdeSpatialReference(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public String getCsWkt() {
    return csWkt;
  }

  public int getEsriSrid() {
    return esriSrid;
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public Double getMOffset() {
    return mOffset;
  }

  public PrecisionModel getMPrecisionModel() {
    return mPrecisionModel;
  }

  public Double getMScale() {
    return mScale;
  }

  public String getName() {
    return name;
  }

  public int getSrid() {
    return srid;
  }

  public Double getXOffset() {
    return xOffset;
  }

  public PrecisionModel getXyPrecisionModel() {
    return geometryFactory.getPrecisionModel();
  }

  public Double getXyScale() {
    return xyScale;
  }

  public Double getYOffset() {
    return yOffset;
  }

  public Double getZOffset() {
    return zOffset;
  }

  public PrecisionModel getZPrecisionModel() {
    return zPrecisionModel;
  }

  public Double getZScale() {
    return zScale;
  }

  public void setCsWkt(final String csWkt) {
    this.csWkt = csWkt;
  }

  public void setEsriSrid(final int esriSrid) {
    this.esriSrid = esriSrid;
  }

  public void setMOffset(final Double mOffset) {
    this.mOffset = mOffset;
  }

  public void setMOffset(final Number mOffset) {
    if (mOffset == null) {
      this.mOffset = null;
    } else {
      this.mOffset = mOffset.doubleValue();
    }
  }

  public void setMScale(final Double mScale) {
    this.mScale = mScale;
    if (mScale != null) {
      mPrecisionModel = new PrecisionModel(mScale);
    }
  }

  public void setMScale(final Number mScale) {
    if (mScale == null) {
      this.mScale = null;
      mPrecisionModel = new PrecisionModel(mPrecisionModel);
    } else {
      setMScale(mScale.doubleValue());
    }
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setSrid(final int srid) {
    this.srid = srid;
  }

  public void setXOffset(final Double xOffset) {
    this.xOffset = xOffset;
  }

  public void setXOffset(final Number xOffset) {
    if (xOffset == null) {
      this.xOffset = null;
    } else {
      this.xOffset = xOffset.doubleValue();
    }
  }

  public void setXyScale(final Double xyScale) {
    this.xyScale = xyScale;
  }

  public void setXyScale(final Number xyScale) {
    if (xyScale == null) {
      this.xyScale = null;
    } else {
      setXyScale(xyScale.doubleValue());
    }
  }

  public void setYOffset(final Double yOffset) {
    this.yOffset = yOffset;
  }

  public void setYOffset(final Number yOffset) {
    if (yOffset == null) {
      this.yOffset = null;
    } else {
      this.yOffset = yOffset.doubleValue();
    }
  }

  public void setZOffset(final Double zOffset) {
    this.zOffset = zOffset;
  }

  public void setZOffset(final Number zOffset) {
    if (zOffset == null) {
      this.zOffset = null;
    } else {
      this.zOffset = zOffset.doubleValue();
    }
  }

  public void setZScale(final Double zScale) {
    this.zScale = zScale;
    if (zScale != null) {
      zPrecisionModel = new PrecisionModel(zScale);
    }
  }

  public void setZScale(final Number zScale) {
    if (zScale == null) {
      this.zScale = null;
      zPrecisionModel = null;
    } else {
      setZScale(zScale.doubleValue());
    }
  }

  @Override
  public String toString() {
    return esriSrid + "=" + srid;

  }
}
