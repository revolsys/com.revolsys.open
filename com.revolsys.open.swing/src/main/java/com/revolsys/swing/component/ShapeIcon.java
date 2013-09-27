package com.revolsys.swing.component;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import javax.swing.Icon;

import com.revolsys.awt.WebColors;

public class ShapeIcon implements Icon {

  private final int iconHeight;

  private final int iconWidth;

  private final Shape shape;

  public ShapeIcon(final Shape shape, final int iconWidth, final int iconHeight) {
    this.shape = shape;
    this.iconWidth = iconWidth;
    this.iconHeight = iconHeight;
  }

  @Override
  public int getIconHeight() {
    return iconHeight;
  }

  @Override
  public int getIconWidth() {
    return iconWidth;
  }

  @Override
  public void paintIcon(final Component c, final Graphics g, final int x,
    final int y) {
    final Graphics2D graphics = (Graphics2D)g;
    final AffineTransform savedTransform = graphics.getTransform();
    try {
      graphics.translate(x, y);
      final Rectangle bounds = shape.getBounds();
      final AffineTransform shapeTransform = AffineTransform.getScaleInstance(
        iconWidth / bounds.width, iconHeight / bounds.height);
      final Shape newShape = new GeneralPath(shape).createTransformedShape(shapeTransform);
      graphics.setPaint(WebColors.Gray);
      graphics.fill(newShape);
      graphics.setColor(WebColors.Black);
      graphics.draw(newShape);
    } finally {
      graphics.setTransform(savedTransform);
    }
  }

}
