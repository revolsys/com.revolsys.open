package com.revolsys.swing.map.layer.record.style.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.revolsys.awt.WebColors;
import com.revolsys.swing.map.layer.record.renderer.TextStyleRenderer;
import com.revolsys.swing.map.layer.record.style.TextStyle;
import com.revolsys.util.Exceptions;

public class TextStylePreview extends JPanel {
  private static final long serialVersionUID = 1L;

  private final TextStyle textStyle;

  public TextStylePreview(final TextStyle textStyle) {
    final Dimension size = new Dimension(101, 101);
    setPreferredSize(size);
    setMinimumSize(size);
    setMaximumSize(size);
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    this.textStyle = textStyle;
  }

  @Override
  protected void paintComponent(final Graphics g) {
    super.paintComponent(g);
    final Graphics2D graphics = (Graphics2D)g;

    graphics.setPaint(WebColors.LightGray);
    graphics.drawLine(50, 0, 50, 100);
    graphics.drawLine(0, 50, 100, 50);
    graphics.translate(50, 50);
    try {
      TextStyleRenderer.renderText(null, graphics, "Text", null, this.textStyle);
    } catch (final Throwable e) {
      Exceptions.error(getClass(), e);
    }
    graphics.translate(-50, -50);
  }
}
