package com.revolsys.swing.map.overlay;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import org.jdesktop.swingx.color.ColorUtil;

import com.revolsys.awt.WebColors;
import com.revolsys.swing.map.MapPanel;

@SuppressWarnings("serial")
public class ToolTipOverlay extends AbstractOverlay {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private final JLabel label = new JLabel();

  public ToolTipOverlay(final MapPanel map) {
    super(map);
    setLayout(null);
    this.label.setOpaque(true);
    this.label.setBorder(BorderFactory.createLineBorder(ColorUtil.setAlpha(
      WebColors.Black, 127)));
    this.label.setBackground(ColorUtil.setAlpha(WebColors.Yellow, 127));
    add(this.label);
    clearText();
  }

  public void clearText() {
    this.label.setText("");
    this.label.setVisible(false);
    repaint();
  }

  public void setText(final Point point, final CharSequence text) {
    this.label.setBackground(ColorUtil.setAlpha(WebColors.Yellow, 191));
    this.label.setText(text.toString());
    this.label.setLocation(point);
    this.label.setVisible(true);
    final Dimension preferredSize = this.label.getPreferredSize();
    this.label.setSize(preferredSize);
    repaint();
  }
}
