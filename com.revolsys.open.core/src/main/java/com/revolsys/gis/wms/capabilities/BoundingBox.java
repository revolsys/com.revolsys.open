package com.revolsys.gis.wms.capabilities;

import com.vividsolutions.jts.geom.Envelope;

public class BoundingBox {
  private String srs;

  private Envelope envelope;

  private double resX;

  private double resY;

  public Envelope getEnvelope() {
    return envelope;
  }

  public double getResX() {
    return resX;
  }

  public double getResY() {
    return resY;
  }

  public String getSrs() {
    return srs;
  }

  public void setEnvelope(final Envelope envelope) {
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
