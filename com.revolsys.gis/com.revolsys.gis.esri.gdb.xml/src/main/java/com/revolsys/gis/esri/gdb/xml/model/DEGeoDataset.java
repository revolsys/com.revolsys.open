package com.revolsys.gis.esri.gdb.xml.model;

public class DEGeoDataset extends DEDataset {

  private Envelope extent;

  private SpatialReference spatialReference;

  public Envelope getExtent() {
    return extent;
  }

  public SpatialReference getSpatialReference() {
    return spatialReference;
  }

  public void setExtent(final Envelope extent) {
    this.extent = extent;
  }

  public void setSpatialReference(final SpatialReference spatialReference) {
    this.spatialReference = spatialReference;
  }

}
