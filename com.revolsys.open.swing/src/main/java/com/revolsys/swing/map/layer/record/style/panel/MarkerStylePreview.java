package com.revolsys.swing.map.layer.record.style.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import org.jeometry.common.awt.WebColors;

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRenderer;
import com.revolsys.swing.map.view.graphics.Graphics2DViewport;

public class MarkerStylePreview extends JPanel {
  private static final long serialVersionUID = 1L;

  private final MarkerStyle markerStyle;

  private final Point point = new PointDoubleXY(50, 51);

  public MarkerStylePreview(final MarkerStyle markerStyle) {
    final Dimension size = new Dimension(101, 101);
    setPreferredSize(size);
    setMinimumSize(size);
    setMaximumSize(size);
    setBackground(Color.WHITE);
    this.markerStyle = markerStyle;
  }

  @Override
  protected void paintComponent(final Graphics g) {
    super.paintComponent(g);
    final Graphics2D graphics = (Graphics2D)g;
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setPaint(WebColors.LightGray);
    graphics.drawRect(0, 0, 100, 100);
    graphics.drawLine(50, 0, 50, 100);
    graphics.drawLine(0, 50, 100, 50);

    try (
      final Graphics2DViewport viewport = new Graphics2DViewport(graphics, 101, 101)) {
      final Graphics2DViewRenderer view = viewport.newViewRenderer();
      view.renderMarker(this.markerStyle, this.point);
    }
  }
}
