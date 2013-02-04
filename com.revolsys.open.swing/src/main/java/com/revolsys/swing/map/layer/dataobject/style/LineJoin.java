package com.revolsys.swing.map.layer.dataobject.style;

import java.awt.BasicStroke;

public enum LineJoin {
  MITER(BasicStroke.JOIN_MITER), ROUND(BasicStroke.JOIN_ROUND), BEVEL(
    BasicStroke.JOIN_BEVEL);

  private int awtValue;

  private LineJoin(final int awtValue) {
    this.awtValue = awtValue;
  }

  public int getAwtValue() {
    return awtValue;
  }
}
