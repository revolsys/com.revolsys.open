/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.3
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.capi.swig;

public class SpatialReference {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  public SpatialReference(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(SpatialReference obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        EsriFileGdbJNI.delete_SpatialReference(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public SpatialReference() {
    this(EsriFileGdbJNI.new_SpatialReference(), true);
  }

  public int SetSpatialReferenceText(String spatialReference) {
    return EsriFileGdbJNI.SpatialReference_SetSpatialReferenceText(swigCPtr, this, spatialReference);
  }

  public int SetSpatialReferenceID(int wkid) {
    return EsriFileGdbJNI.SpatialReference_SetSpatialReferenceID(swigCPtr, this, wkid);
  }

  public int SetFalseOriginAndUnits(double falseX, double falseY, double xyUnits) {
    return EsriFileGdbJNI.SpatialReference_SetFalseOriginAndUnits(swigCPtr, this, falseX, falseY, xyUnits);
  }

  public int SetZFalseOriginAndUnits(double falseZ, double zUnits) {
    return EsriFileGdbJNI.SpatialReference_SetZFalseOriginAndUnits(swigCPtr, this, falseZ, zUnits);
  }

  public int SetMFalseOriginAndUnits(double falseM, double mUnits) {
    return EsriFileGdbJNI.SpatialReference_SetMFalseOriginAndUnits(swigCPtr, this, falseM, mUnits);
  }

  public int SetXYTolerance(double xyTolerance) {
    return EsriFileGdbJNI.SpatialReference_SetXYTolerance(swigCPtr, this, xyTolerance);
  }

  public int SetZTolerance(double zTolerance) {
    return EsriFileGdbJNI.SpatialReference_SetZTolerance(swigCPtr, this, zTolerance);
  }

  public int SetMTolerance(double mTolerance) {
    return EsriFileGdbJNI.SpatialReference_SetMTolerance(swigCPtr, this, mTolerance);
  }

  public int getId() {
    return EsriFileGdbJNI.SpatialReference_getId(swigCPtr, this);
  }

  public String getText() {
    return EsriFileGdbJNI.SpatialReference_getText(swigCPtr, this);
  }

  public double getXFalseOrigin() {
    return EsriFileGdbJNI.SpatialReference_getXFalseOrigin(swigCPtr, this);
  }

  public double getYFalseOrigin() {
    return EsriFileGdbJNI.SpatialReference_getYFalseOrigin(swigCPtr, this);
  }

  public double getXYUnits() {
    return EsriFileGdbJNI.SpatialReference_getXYUnits(swigCPtr, this);
  }

  public double getMFalseOrigin() {
    return EsriFileGdbJNI.SpatialReference_getMFalseOrigin(swigCPtr, this);
  }

  public double getMUnits() {
    return EsriFileGdbJNI.SpatialReference_getMUnits(swigCPtr, this);
  }

  public double getMTolerance() {
    return EsriFileGdbJNI.SpatialReference_getMTolerance(swigCPtr, this);
  }

  public double getXYTolerance() {
    return EsriFileGdbJNI.SpatialReference_getXYTolerance(swigCPtr, this);
  }

  public double getXUnits() {
    return EsriFileGdbJNI.SpatialReference_getXUnits(swigCPtr, this);
  }

  public double getZTolerance() {
    return EsriFileGdbJNI.SpatialReference_getZTolerance(swigCPtr, this);
  }

}
