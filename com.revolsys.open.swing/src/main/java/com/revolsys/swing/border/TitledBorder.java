package com.revolsys.swing.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

import org.jeometry.common.awt.WebColors;

public class TitledBorder extends AbstractBorder {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final String title;

  public TitledBorder(final String title) {
    this.title = title.replaceAll("</[^>]+>", "").replaceAll("<[^>]+>", "");
  }

  @Override
  public Insets getBorderInsets(final Component c) {
    return new Insets(19, 5, 5, 5);
  }

  @Override
  public Insets getBorderInsets(final Component c, final Insets insets) {
    insets.left = 5;
    insets.right = 5;
    insets.top = 19;
    insets.bottom = 5;
    return insets;
  }

  @Override
  public void paintBorder(final Component c, final Graphics g, final int x, final int y,
    final int width, final int height) {
    final Graphics2D graphics = (Graphics2D)g;
    final int boxX = x + 2;
    final int boxY = y + 2;
    final int boxWidth = width - 5;
    final int boxHeight = height - 5;
    final int topHeight = 14;

    graphics.setPaint(WebColors.White);
    graphics.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);

    final Color titleBackground = new Color(222, 237, 247);
    graphics.setPaint(titleBackground);
    graphics.fillRoundRect(boxX, boxY, boxWidth, topHeight, 10, 10);

    graphics.setPaint(titleBackground);
    graphics.fillRect(boxX, boxY + topHeight - 5, boxWidth, 5);

    final Color borderColor = new Color(174, 208, 234);
    graphics.setColor(borderColor);
    graphics.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);

    graphics.drawLine(boxX, boxY + topHeight, boxX + boxWidth, boxY + topHeight);

    final Font font = new Font("Arial", Font.BOLD, 12);
    graphics.setFont(font);
    graphics.setColor(new Color(0, 112, 163));
    graphics.drawString(this.title, 8, 14);
  }
}
