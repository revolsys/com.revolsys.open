package com.revolsys.gis.wms.capabilities;


public class BoundingBox {
  private String srs;

  private com.revolsys.jts.geom.BoundingBox envelope;

  private double resX;

  private double resY;

  public com.revolsys.jts.geom.BoundingBox getEnvelope() {
    return this.envelope;
  }

  public double getResX() {
    return this.resX;
  }

  public double getResY() {
    return this.resY;
  }

  public String getSrs() {
    return this.srs;
  }

  public void setEnvelope(final com.revolsys.jts.geom.BoundingBox envelope) {
    this.envelope = envelope;
  }

  public void setResX(final double resX) {
    this.resX = resX;
  }

  public void setResY(final double resY) {
    this.resY = resY;
  }

  public void setSrs(final String srs) {
    this.srs = srs;
  }

}
