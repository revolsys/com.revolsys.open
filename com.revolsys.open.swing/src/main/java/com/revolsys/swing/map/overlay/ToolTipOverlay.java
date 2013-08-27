package com.revolsys.swing.map.overlay;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import com.revolsys.awt.WebColors;
import com.revolsys.swing.map.MapPanel;

public class ToolTipOverlay extends AbstractOverlay {
  private static final long serialVersionUID = 1L;

  private final JLabel label = new JLabel();

  public ToolTipOverlay(final MapPanel map) {
    super(map);
    setLayout(null);
    this.label.setOpaque(true);
    this.label.setBorder(BorderFactory.createLineBorder(WebColors.Black));
    this.label.setBackground(WebColors.Yellow);
    add(this.label);
    clearText();
  }

  public void clearText() {
    this.label.setText("");
    this.label.setVisible(false);
    repaint();
  }

  public void setText(final Point2D point, final CharSequence text) {
    this.label.setBackground(WebColors.Yellow);
    this.label.setText(text.toString());
    this.label.setLocation(new Point((int)point.getX(), (int)point.getY()));
    this.label.setVisible(true);
    final Dimension preferredSize = this.label.getPreferredSize();
    this.label.setSize(preferredSize);
    repaint();
  }
}
