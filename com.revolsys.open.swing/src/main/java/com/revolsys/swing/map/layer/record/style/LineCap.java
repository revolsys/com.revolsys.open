package com.revolsys.swing.map.layer.record.style;

import java.awt.BasicStroke;

public enum LineCap {
  BUTT(BasicStroke.CAP_BUTT), ROUND(BasicStroke.CAP_ROUND), SQUARE(BasicStroke.CAP_SQUARE);

  private int awtValue;

  private LineCap(final int awtValue) {
    this.awtValue = awtValue;
  }

  public int getAwtValue() {
    return this.awtValue;
  }
}
