package com.revolsys.swing.map.overlay;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import org.jeometry.common.awt.WebColors;

import com.revolsys.swing.map.MapPanel;

public class ToolTipOverlay extends AbstractOverlay {
  private static final long serialVersionUID = 1L;

  private final JLabel label = new JLabel();

  public ToolTipOverlay(final MapPanel map) {
    super(map);
    setLayout(null);
    this.label.setOpaque(true);
    this.label.setBorder(BorderFactory.createLineBorder(WebColors.Black));
    this.label.setBackground(WebColors.newAlpha(WebColors.Yellow, 170));
    add(this.label);
    clearText();
  }

  @Override
  protected void cancel() {
  }

  public void clearText() {
    this.label.setText("");
    this.label.setVisible(false);
    repaint();
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    clearText();
  }

  public void setText(int x, int y, final CharSequence text) {
    this.label.setText(text.toString());
    this.label.setVisible(true);
    final Dimension preferredSize = this.label.getPreferredSize();
    this.label.setSize(preferredSize);

    final int width = preferredSize.width;
    final int height = preferredSize.height;
    final int offset = 50;
    final int overlayWidth = getWidth() - offset;
    final int overlayHeight = getHeight() - offset;
    if (x > offset) {
      x += offset;
    }
    if (x + width > overlayWidth) {
      if (x > width + offset) {
        x = x - offset * 2 - width;
      }
    }

    if (y > offset) {
      y -= offset;
    }
    if (y + height > overlayHeight) {
      y = Math.max(offset, overlayHeight - height);
    }
    this.label.setLocation(new Point(x, y));

    getMap().moveToFront(this);
    repaint();
  }

  public void setText(final Point2D point, final CharSequence text) {
    final int x = (int)point.getX();
    final int y = (int)point.getY();
    setText(x, y, text);
  }
}
