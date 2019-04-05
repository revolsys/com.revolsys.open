package com.revolsys.swing.map.layer.record.style.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jeometry.common.logging.Logs;

import com.revolsys.awt.WebColors;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.swing.map.Graphics2DViewport;
import com.revolsys.swing.map.ImageViewport;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.marker.Marker;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRender;

public class MarkerStylePreview extends JPanel {
  private static final long serialVersionUID = 1L;

  private final MarkerStyle markerStyle;

  private final Graphics2DViewRender view;

  public MarkerStylePreview(final MarkerStyle markerStyle) {
    final Dimension size = new Dimension(101, 101);
    setPreferredSize(size);
    setMinimumSize(size);
    setMaximumSize(size);
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    this.markerStyle = markerStyle;
    final Graphics2DViewport viewport = new Graphics2DViewport(null, 101, 101,
      new BoundingBoxDoubleXY(0, 0, 101, 101));
    this.view = viewport.newViewRenderer();
  }

  @Override
  protected void paintComponent(final Graphics g) {
    super.paintComponent(g);
    final Graphics2D graphics = (Graphics2D)g;
    graphics.setPaint(WebColors.LightGray);
    graphics.drawLine(50, 0, 50, 100);
    graphics.drawLine(0, 50, 100, 50);

    try (
      final ImageViewport viewport = new ImageViewport(101, 101)) {
      final Graphics2DViewRender view = viewport.newViewRenderer();
      final Graphics2D viewGraphics = view.getGraphics();
      final Marker marker = this.markerStyle.getMarker();
      try {
        marker.render(view, viewGraphics, this.markerStyle, 50, 50, 0);
      } catch (final Throwable e) {
        Logs.error(this, e);
      }
      viewGraphics.translate(-50, -50);
      viewGraphics.dispose();
      graphics.drawImage(viewport.getImage(), 0, 101, null);

    }
  }
}
