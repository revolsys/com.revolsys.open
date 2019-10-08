package com.revolsys.swing.map.layer.bing;

public enum ImagerySet {
  Aerial(19), //
  AerialWithLabels(19), //
  AerialWithLabelsOnDemand(19), //
  CanvasDark(20), //
  CanvasLight(20), //
  CanvasGray(20), //
  CollinsBart(23), //
  OrdnanceSurvey(23), //
  Road(20);

  private int maxLevelOfDetail;

  private ImagerySet(final int maxLevelOfDetail) {
    this.maxLevelOfDetail = maxLevelOfDetail;
  }

  public int getMaxLevelOfDetail() {
    return this.maxLevelOfDetail;
  }
}
