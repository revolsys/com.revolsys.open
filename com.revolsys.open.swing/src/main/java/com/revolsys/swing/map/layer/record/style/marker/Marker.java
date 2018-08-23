package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Graphics2D;

import javax.swing.Icon;

import com.revolsys.io.map.MapSerializer;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRender;

public interface Marker extends MapSerializer {
  default String getMarkerType() {
    return null;
  }

  default boolean isUseMarkerType() {
    return false;
  }

  Icon newIcon(MarkerStyle style);

  void render(Graphics2DViewRender view, Graphics2D graphics, MarkerStyle style, double modelX,
    double modelY, double orientation);
}
