package com.revolsys.io.esri.gdb.xml.model;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;

public class SpatialReference {

  private static final double FLOATING_SCALE = 11258999068426.238;

  public static SpatialReference get(final GeometryFactory geometryFactory,
    final String wkt) {
    if (geometryFactory != null) {
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      if (coordinateSystem instanceof com.revolsys.gis.cs.GeographicCoordinateSystem) {
        return new GeographicCoordinateSystem(geometryFactory, wkt);
      } else if (coordinateSystem instanceof com.revolsys.gis.cs.ProjectedCoordinateSystem) {
        return new ProjectedCoordinateSystem(geometryFactory, wkt);
      }
    }
    return null;
  }

  private String wkt;

  private double xOrigin;

  private double yOrigin;

  private double xYScale;

  private double zOrigin;

  private double zScale;

  private double mOrigin;

  private double mScale;

  private double xYTolerance;

  private double zTolerance;

  private double mTolerance;

  private boolean highPrecision;

  private double leftLongitude;

  private int wkid;

  private int latestWKID;

  private CoordinateSystem coordinateSystem;

  private GeometryFactory geometryFactory;

  public SpatialReference() {
  }

  protected SpatialReference(final GeometryFactory geometryFactory,
    final String wkt) {
    this.geometryFactory = geometryFactory;
    if (geometryFactory != null) {
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      if (coordinateSystem != null) {
        final CoordinateSystem esriCoordinateSystem = EsriCoordinateSystems.getCoordinateSystem(coordinateSystem.getId());
        if (esriCoordinateSystem != null) {
          final BoundingBox areaBoundingBox = coordinateSystem.getAreaBoundingBox();
          this.wkt = wkt;
          xOrigin = areaBoundingBox.getMinX();
          yOrigin = areaBoundingBox.getMinY();
          xYScale = geometryFactory.getScaleXY();
          if (xYScale == 0) {
            if (this instanceof ProjectedCoordinateSystem) {
              xYScale = 1000;
            } else {
              xYScale = 1000000;
            }
          }
          zOrigin = -100000;
          zScale = geometryFactory.getScaleZ();
          if (zScale == 0) {
            zScale = 1000;
          }
          mOrigin = -100000;
          mScale = 1000;
          xYTolerance = 1.0 / xYScale;
          zTolerance = 1.0 / zScale;
          mTolerance = 1.0 / mScale;
          highPrecision = true;
          wkid = coordinateSystem.getId();
        }
      }
    }
  }

  public CoordinateSystem getCoordinateSystem() {
    if (coordinateSystem == null) {
      coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(latestWKID);
      if (coordinateSystem == null) {
        coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(wkid);
      }
    }
    return coordinateSystem;
  }

  public GeometryFactory getGeometryFactory() {
    if (geometryFactory == null) {
      final CoordinateSystem coordinateSystem = getCoordinateSystem();
      if (coordinateSystem != null) {
        if (xYScale == FLOATING_SCALE) {
          geometryFactory = GeometryFactory.getFactory(
            coordinateSystem.getId(), 0, zScale);
        } else {
          geometryFactory = GeometryFactory.getFactory(
            coordinateSystem.getId(), xYScale, zScale);
        }
      }
    }
    return geometryFactory;
  }

  public int getLatestWKID() {
    return latestWKID;
  }

  public double getLeftLongitude() {
    return leftLongitude;
  }

  public double getMOrigin() {
    return mOrigin;
  }

  public double getMScale() {
    return mScale;
  }

  public double getMTolerance() {
    return mTolerance;
  }

  public int getWKID() {
    return wkid;
  }

  public String getWKT() {
    return wkt;
  }

  public double getXOrigin() {
    return xOrigin;
  }

  public double getXYScale() {
    return xYScale;
  }

  public double getXYTolerance() {
    return xYTolerance;
  }

  public double getYOrigin() {
    return yOrigin;
  }

  public double getZOrigin() {
    return zOrigin;
  }

  public double getZScale() {
    return zScale;
  }

  public double getZTolerance() {
    return zTolerance;
  }

  public boolean isHighPrecision() {
    return highPrecision;
  }

  public void setHighPrecision(final boolean highPrecision) {
    this.highPrecision = highPrecision;
  }

  public void setLatestWKID(final int latestWKID) {
    this.latestWKID = latestWKID;
  }

  public void setLeftLongitude(final double leftLongitude) {
    this.leftLongitude = leftLongitude;
  }

  public void setMOrigin(final double mOrigin) {
    this.mOrigin = mOrigin;
  }

  public void setMScale(final double mScale) {
    this.mScale = mScale;
  }

  public void setMTolerance(final double mTolerance) {
    this.mTolerance = mTolerance;
  }

  public void setWKID(final int wkid) {
    this.wkid = wkid;
  }

  public void setWKT(final String wkt) {
    this.wkt = wkt;
  }

  public void setXOrigin(final double xOrigin) {
    this.xOrigin = xOrigin;
  }

  public void setXYScale(final double xYScale) {
    this.xYScale = xYScale;
  }

  public void setXYTolerance(final double xYTolerance) {
    this.xYTolerance = xYTolerance;
  }

  public void setYOrigin(final double yOrigin) {
    this.yOrigin = yOrigin;
  }

  public void setZOrigin(final double zOrigin) {
    this.zOrigin = zOrigin;
  }

  public void setZScale(final double zScale) {
    this.zScale = zScale;
  }

  public void setZTolerance(final double zTolerance) {
    this.zTolerance = zTolerance;
  }

  @Override
  public String toString() {
    return wkt;
  }
}
