package com.revolsys.awt.gradient;

public interface LinearGradient extends Cloneable {

  static int NULL_COLOR = 0;

  LinearGradient clone();

  int getColorIntForValue(double elevation);

  void setValueMax(double valueMax);

  void setValueMin(double valueMin);

  void updateValues();
}
