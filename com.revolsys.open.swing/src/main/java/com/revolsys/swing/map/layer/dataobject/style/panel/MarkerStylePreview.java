package com.revolsys.swing.map.layer.dataobject.style.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.revolsys.swing.map.layer.dataobject.style.MarkerStyle;
import com.revolsys.swing.map.layer.dataobject.style.marker.Marker;

public class MarkerStylePreview extends JPanel {
  private static final long serialVersionUID = 1L;

  private MarkerStyle markerStyle;

  public MarkerStylePreview(MarkerStyle markerStyle) {
    Dimension size = new Dimension(100, 100);
    setPreferredSize(size);
    setMinimumSize(size);
    setMaximumSize(size);
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    this.markerStyle = markerStyle;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D graphics = (Graphics2D)g;
    final Marker marker = markerStyle.getMarker();
    marker.render(null, graphics, markerStyle, 49, 49, 0);
  }
}
