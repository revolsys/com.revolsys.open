package com.revolsys.gis.esri.gdb.xml.model;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.cs.esri.EsriCsWktWriter;
import com.revolsys.gis.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.xml.XsiConstants;

public class DEFeatureClass extends DETable {
  private String featureType = "FEATURE_TYPE_SIMPLE";

  private String shapeType;

  private String shapeFieldName;

  private boolean hasM;

  private boolean hasZ;

  private boolean hasSpatialIndex;

  private String areaFieldName;

  private String lengthFieldName;

  private Envelope extent;

  private SpatialReference spatialReference;

  public DEFeatureClass() {
    setDatasetType(EsriGeodatabaseXmlConstants.DATASET_TYPE_FEATURE_CLASS);
  }

  public String getFeatureType() {
    return featureType;
  }

  public void setFeatureType(String featureType) {
    this.featureType = featureType;
  }

  public String getShapeType() {
    return shapeType;
  }

  public void setShapeType(String shapeType) {
    this.shapeType = shapeType;
  }

  public String getShapeFieldName() {
    return shapeFieldName;
  }

  public void setShapeFieldName(String shapeFieldName) {
    this.shapeFieldName = shapeFieldName;
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

  public boolean isHasSpatialIndex() {
    return hasSpatialIndex;
  }

  public void setHasSpatialIndex(boolean hasSpatialIndex) {
    this.hasSpatialIndex = hasSpatialIndex;
  }

  public String getAreaFieldName() {
    return areaFieldName;
  }

  public void setAreaFieldName(String areaFieldName) {
    this.areaFieldName = areaFieldName;
  }

  public String getLengthFieldName() {
    return lengthFieldName;
  }

  public void setLengthFieldName(String lengthFieldName) {
    this.lengthFieldName = lengthFieldName;
  }

  public Envelope getExtent() {
    return extent;
  }

  public void setExtent(Envelope extent) {
    this.extent = extent;
  }

  public SpatialReference getSpatialReference() {
    return spatialReference;
  }

  public void setSpatialReference(SpatialReference spatialReference) {
    this.spatialReference = spatialReference;
  }
}
