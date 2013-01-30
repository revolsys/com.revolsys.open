package com.revolsys.swing.map.style;

import java.awt.BasicStroke;

public enum LineCap {
  BUTT(BasicStroke.CAP_BUTT), ROUND(BasicStroke.CAP_ROUND), SQUARE(
    BasicStroke.CAP_SQUARE);

  private int awtValue;

  private LineCap(int awtValue) {
    this.awtValue = awtValue;
  }

  public int getAwtValue() {
    return awtValue;
  }
}
