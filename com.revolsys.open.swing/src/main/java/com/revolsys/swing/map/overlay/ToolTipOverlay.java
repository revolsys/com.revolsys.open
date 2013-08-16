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

  private final JLabel label = new JLabel();

  public ToolTipOverlay(final MapPanel map) {
    super(map);
    setLayout(null);
    label.setOpaque(true);
    label.setBorder(BorderFactory.createLineBorder(ColorUtil.setAlpha(
      WebColors.Black, 127)));
    label.setBackground(ColorUtil.setAlpha(WebColors.Yellow, 127));
    add(label);
    clearText();
  }

  public void clearText() {
    label.setText("");
    label.setVisible(false);
    repaint();
  }

  public void setText(final Point point, final CharSequence text) {
    label.setBackground(ColorUtil.setAlpha(WebColors.Yellow, 191));
    label.setText(text.toString());
    label.setLocation(point);
    label.setVisible(true);
    final Dimension preferredSize = label.getPreferredSize();
    label.setSize(preferredSize);
    repaint();
  }
}
