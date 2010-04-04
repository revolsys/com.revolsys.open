package com.revolsys.jump.ui.info;

import java.awt.Color;

import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;

public final class InfoModelUtil {
  private InfoModelUtil() {
  }

  public static Color getColor(final Layer layer) {

    Color basicColor;
    if (layer.getBasicStyle().isRenderingFill()) {
      basicColor = layer.getBasicStyle().getFillColor();
    } else {
      basicColor = layer.getBasicStyle().getLineColor();
    }
    int alpha = layer.getBasicStyle().getAlpha();
    Color alphaColor = GUIUtil.alphaColor(basicColor, alpha);
    return GUIUtil.toSimulatedTransparency(alphaColor);
  }
}
