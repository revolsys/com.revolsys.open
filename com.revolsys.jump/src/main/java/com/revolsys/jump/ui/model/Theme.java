package com.revolsys.jump.ui.model;

import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;

public interface Theme {
  String getLabel();

  void setLabel(String label);

  BasicStyle getBasicStyle();

  void setBasicStyle(BasicStyle style);

  boolean isVisible();

  void setVisible(boolean visible);
}
