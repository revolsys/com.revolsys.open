package com.revolsys.swing.map.layer.record.style.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.revolsys.awt.WebColors;
import com.revolsys.logging.Logs;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.marker.Marker;

public class MarkerStylePreview extends JPanel {
  private static final long serialVersionUID = 1L;

  private final MarkerStyle markerStyle;

  public MarkerStylePreview(final MarkerStyle markerStyle) {
    final Dimension size = new Dimension(101, 101);
    setPreferredSize(size);
    setMinimumSize(size);
    setMaximumSize(size);
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    this.markerStyle = markerStyle;
  }

  @Override
  protected void paintComponent(final Graphics g) {
    super.paintComponent(g);
    final Graphics2D graphics = (Graphics2D)g;

    graphics.setPaint(WebColors.LightGray);
    graphics.drawLine(50, 0, 50, 100);
    graphics.drawLine(0, 50, 100, 50);
    graphics.translate(50, 50);
    final Marker marker = this.markerStyle.getMarker();
    try {
      marker.render(null, graphics, this.markerStyle, 0, 0, 0);
    } catch (final Throwable e) {
      Logs.error(getClass(), e);
    }
    graphics.translate(-50, -50);
  }
}
