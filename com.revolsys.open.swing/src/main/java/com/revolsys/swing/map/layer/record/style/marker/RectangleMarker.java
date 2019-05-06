package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.swing.map.ImageViewport;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRenderer;

public class RectangleMarker extends AbstractMarker {

  public RectangleMarker() {
    super("rectangle");
  }

  public RectangleMarker(final String name) {
    super(name);
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof RectangleMarker) {
      final RectangleMarker marker = (RectangleMarker)object;
      return getName().equals(marker.getName());
    } else {
      return false;
    }
  }

  @Override
  public String getTypeName() {
    return "markerRectangle";
  }

  @Override
  public boolean isUseMarkerName() {
    return true;
  }

  @Override
  public Icon newIcon(final MarkerStyle style) {

    try (
      final ImageViewport viewport = new ImageViewport(16, 16)) {
      final Graphics2DViewRenderer view = viewport.newViewRenderer();
      final Graphics2D graphics = view.getGraphics();
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      if (style.setMarkerFillStyle(view, graphics)) {
        graphics.fillRect(0, 0, 15, 15);
      }
      if (style.setMarkerLineStyle(view, graphics)) {
        graphics.drawRect(0, 0, 15, 15);
      }
      graphics.dispose();
      return new ImageIcon(viewport.getImage());
    }
  }

  @Override
  public void render(final Graphics2DViewRenderer view, final Graphics2D graphics,
    final MarkerStyle style, final double modelX, final double modelY, final double orientation) {
    view.renderRectangle(style, modelX, modelY, orientation);
  }
}
