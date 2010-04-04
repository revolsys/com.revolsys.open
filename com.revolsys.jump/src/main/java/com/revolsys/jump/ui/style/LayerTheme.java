package com.revolsys.jump.ui.style;

import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public interface LayerTheme {
  String getLabel();

  void setLabel(String label);

  Style getStyle();

  void setStyle(Style style);

  boolean isVisible();

  void setVisible(boolean visible);
}
