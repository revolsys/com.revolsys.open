package com.revolsys.swing.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

import com.revolsys.awt.WebColors;

public class TitledBorder extends AbstractBorder {

  private final String title;

  public TitledBorder(final String title) {
    this.title = title;
  }

  @Override
  public Insets getBorderInsets(final Component c) {
    return new Insets(25, 5, 5, 5);
  }

  @Override
  public Insets getBorderInsets(final Component c, final Insets insets) {
    insets.left = 5;
    insets.right = 5;
    insets.top = 25;
    insets.bottom = 5;
    return insets;
  }

  @Override
  public void paintBorder(final Component c, final Graphics g, final int x,
    final int y, final int width, final int height) {
    final Graphics2D graphics = (Graphics2D)g;
    graphics.setPaint(WebColors.White);
    graphics.fillRoundRect(x, y, width - 1, height - 1, 10, 10);

    final Color titleBackground = new Color(222, 237, 247);
    graphics.setPaint(titleBackground);
    graphics.fillRoundRect(x, y, width - 1, 21, 10, 10);

    graphics.setPaint(titleBackground);
    graphics.fillRect(x, y + 11, width - 1, 10);

    final Color borderColor = new Color(174, 208, 234);
    graphics.setColor(borderColor);
    graphics.drawRoundRect(x, y, width - 1, height - 1, 10, 10);

    graphics.drawLine(0, 21, width - 1, 21);

    final Font font = new Font("Arial", Font.BOLD, 14);
    graphics.setFont(font);
    graphics.setColor(new Color(0, 112, 163));
    graphics.drawString(title, 10, 16);
  }
}
