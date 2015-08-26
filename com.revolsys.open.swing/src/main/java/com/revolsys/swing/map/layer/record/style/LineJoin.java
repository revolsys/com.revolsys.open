package com.revolsys.swing.map.layer.record.style;

import java.awt.BasicStroke;

public enum LineJoin {
  BEVEL(BasicStroke.JOIN_BEVEL), MITER(BasicStroke.JOIN_MITER), ROUND(BasicStroke.JOIN_ROUND);

  private int awtValue;

  private LineJoin(final int awtValue) {
    this.awtValue = awtValue;
  }

  public int getAwtValue() {
    return this.awtValue;
  }

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
