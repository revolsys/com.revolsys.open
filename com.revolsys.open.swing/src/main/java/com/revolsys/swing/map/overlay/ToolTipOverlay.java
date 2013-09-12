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
    this.label.setText(text.toString());
    this.label.setVisible(true);
    final Dimension preferredSize = this.label.getPreferredSize();
    this.label.setSize(preferredSize);

    final int width = preferredSize.width;
    final int height = preferredSize.height;
    final int offset = 20;
    final int overlayWidth = getWidth() - offset;
    final int overlayHeight = getHeight() - offset;
    int x = (int)point.getX();
    int y = (int)point.getY();
    if (x > 20) {
      x += offset;
    }
    if (x + width > overlayWidth) {
      if (x > width + offset) {
        x = x - 20 - width;
      }
    }

    if (y > 20) {
      y -= offset;
    }
    if (y + height > overlayHeight) {
      y = Math.max(offset, overlayHeight - height);
    }
    this.label.setLocation(new Point(x, y));

    getMap().moveToFront(this);
    repaint();
  }
}
