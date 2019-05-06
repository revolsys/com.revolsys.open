package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Graphics2D;

import javax.swing.Icon;

import com.revolsys.io.map.MapSerializer;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRenderer;

public interface Marker extends MapSerializer {
  default Icon getIcon() {
    return null;
  }

  default String getName() {
    return null;
  }

  default String getTitle() {
    return null;
  }

  default boolean isUseMarkerName() {
    return false;
  }

  Icon newIcon(MarkerStyle style);

  void render(Graphics2DViewRenderer view, Graphics2D graphics, MarkerStyle style, double modelX,
    double modelY, double orientation);
}
