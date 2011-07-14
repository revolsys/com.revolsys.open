package com.revolsys.gis.esri.gdb.xml.model;

public class DEGeoDataset extends DEDataset {

  private Envelope extent;

  private SpatialReference spatialReference;

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
