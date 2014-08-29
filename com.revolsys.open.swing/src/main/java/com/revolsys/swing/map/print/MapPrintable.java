package com.revolsys.swing.map.print;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.measure.Measure;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;

public class MapPrintable implements Printable {
  private double millimetre = Measure.valueOf(1, SI.MILLIMETRE).doubleValue(
    NonSI.INCH.divide(72));

  private final Rectangle2D contentRect;

  private final Project map;

  private final BoundingBox boundingBox;

  private double minorDivisions = 200;

  private double majorDivisions = 1000;

  private final double rulerSizePixels;

  private final int column;

  private final int row;

  private final int dpi;

  private final double scale;

  public MapPrintable(final Project map, final int column, final int row,
    final BoundingBox boundingBox, final Rectangle2D contentRect,
    final int dpi, final double rulerSizePixels, final double minorDivisions,
    final double scale) {
    this.map = map;
    this.column = column;
    this.row = row;
    this.boundingBox = boundingBox;
    this.contentRect = contentRect;
    this.dpi = dpi;
    this.rulerSizePixels = rulerSizePixels;
    millimetre = Measure.valueOf(1, SI.MILLIMETRE).doubleValue(
      NonSI.INCH.divide(dpi));
    this.minorDivisions = minorDivisions;
    this.majorDivisions = minorDivisions * 5;
    this.scale = scale;
  }

  private void drawFooter(final Graphics2D graphics2d) {
    graphics2d.setFont(new Font("Arial", Font.PLAIN, 12));
    final String sheetName = (char)('A' + column) + "" + row;
    final String text = boundingBox.getCoordinateSystem().getName() + " - 1:"
      + scale + " - " + sheetName;

    graphics2d.drawString(text, 0,
      (float)(contentRect.getMaxY() + rulerSizePixels * 2));
  }

  private void drawRuler(final PrintViewport2D viewport,
    final Graphics2D graphics2d) {
    final double unit = viewport.getModelUnitsPerViewUnit();
    final float lineWidth = (float)(unit * millimetre / 10);
    // final boolean savedUseModelCoordinates = viewport.setUseModelCoordinates(
    // true, graphics2d);
    try {
      graphics2d.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_BEVEL));

      final double rulerHeight = unit * this.rulerSizePixels;

      final double minX = boundingBox.getMinX();
      final double maxX = boundingBox.getMaxX();
      final double minY = boundingBox.getMinY();
      final double maxY = boundingBox.getMaxY();

      final double width = maxX - minX;
      final double height = maxY - minY;

      final int startXIndex = (int)Math.ceil(minX / minorDivisions);
      final int endXIndex = (int)Math.ceil(maxX / minorDivisions);
      final AffineTransform modelToScreenTransform = viewport.getModelToScreenTransform();
      for (int i = startXIndex; i < endXIndex; i++) {
        final double x = i * minorDivisions;
        double currentRulerHeight;
        if (x % majorDivisions < minorDivisions) {
          graphics2d.setColor(Color.BLACK);
          currentRulerHeight = rulerHeight;
          // final boolean saved2 = viewport.setUseModelCoordinates(false,
          // graphics2d);
          try {
            graphics2d.setFont(new Font("Arial", Font.PLAIN, 12));
            final double[] coord = new double[2];
            modelToScreenTransform.transform(new double[] {
              x + unit * millimetre, minY - unit * millimetre * 4.5
            }, 0, coord, 0, 1);

            graphics2d.drawString(String.valueOf((int)x), (float)coord[0],
              (float)coord[1]);
            modelToScreenTransform.transform(new double[] {
              x + unit * millimetre, maxY + unit * millimetre * 2.25
            }, 0, coord, 0, 1);

            graphics2d.drawString(String.valueOf((int)x), (float)coord[0],
              (float)coord[1]);
          } finally {
            // viewport.setUseModelCoordinates(saved2, graphics2d);
          }
        } else {
          graphics2d.setColor(Color.LIGHT_GRAY);
          currentRulerHeight = rulerHeight / 3;
        }
        graphics2d.draw(new Line2D.Double(x, minY - currentRulerHeight, x, minY
          - lineWidth));
        graphics2d.draw(new Line2D.Double(x, maxY + lineWidth, x, maxY
          + currentRulerHeight));
      }

      final int startYIndex = (int)Math.ceil(minY / minorDivisions);
      final int endYIndex = (int)Math.ceil(maxY / minorDivisions);
      for (int i = startYIndex; i < endYIndex; i++) {
        final double y = i * minorDivisions;
        double currentRulerHeight;
        if (y % majorDivisions < minorDivisions) {
          graphics2d.setColor(Color.BLACK);
          currentRulerHeight = rulerHeight;
          graphics2d.setFont(new Font("Arial", Font.PLAIN, 12));
          final double[] coord = new double[2];
          // final boolean saved2 = viewport.setUseModelCoordinates(false,
          // graphics2d);
          try {
            modelToScreenTransform.transform(new double[] {
              minX - unit * millimetre * 2.25, y + unit * millimetre
            }, 0, coord, 0, 1);
            drawString(graphics2d, String.valueOf((int)y), coord[0], coord[1],
              -Math.PI / 2);

            modelToScreenTransform.transform(new double[] {
              maxX + unit * millimetre * 4.5, y + unit * millimetre
            }, 0, coord, 0, 1);
            drawString(graphics2d, String.valueOf((int)y), coord[0], coord[1],
              -Math.PI / 2);
          } finally {
            // viewport.setUseModelCoordinates(saved2, graphics2d);
          }
        } else {
          graphics2d.setColor(Color.LIGHT_GRAY);
          currentRulerHeight = rulerHeight / 2;
        }
        graphics2d.draw(new Line2D.Double(minX - currentRulerHeight, y, minX
          - lineWidth, y));
        graphics2d.draw(new Line2D.Double(maxX + lineWidth, y, maxX
          + currentRulerHeight, y));
      }

      graphics2d.setColor(Color.BLACK);
      graphics2d.draw(new Rectangle2D.Double(minX - rulerHeight, minY
        - rulerHeight, width + 2 * rulerHeight, height + 2 * rulerHeight));
      graphics2d.draw(new Rectangle2D.Double(minX - lineWidth,
        minY - lineWidth, width + lineWidth * 2, height + lineWidth * 2));
    } finally {
      // viewport.setUseModelCoordinates(savedUseModelCoordinates, graphics2d);
    }
  }

  private void drawString(final Graphics2D graphics2d, final String label,
    final double x, final double y, final double rotation) {
    final AffineTransform savedTransform = graphics2d.getTransform();
    graphics2d.translate(x, y);
    graphics2d.rotate(rotation);
    graphics2d.drawString(label, 0, 0);
    graphics2d.setTransform(savedTransform);
  }

  @Override
  public int print(final Graphics graphics, final PageFormat pageFormat,
    final int pageIndex) throws PrinterException {
    final Graphics2D graphics2d = (Graphics2D)graphics;
    final PrintViewport2D viewport = new PrintViewport2D(map, graphics2d,
      pageFormat, boundingBox, contentRect, dpi);
    graphics2d.translate(pageFormat.getImageableX() * dpi / 72.0,
      pageFormat.getImageableX() * dpi / 72.0);
    drawFooter(graphics2d);

    graphics2d.translate(contentRect.getMinX(), contentRect.getMinY());
    drawRuler(viewport, graphics2d);
    graphics2d.clip(new Rectangle2D.Double(0, 0, contentRect.getWidth(),
      contentRect.getHeight()));

    final LayerGroup map = viewport.getProject();

    map.getRenderer().render(viewport);

    final double unit = viewport.getModelUnitsPerViewUnit();
    final float lineWidth = (float)(unit * millimetre / 5);
    // final boolean saved = viewport.setUseModelCoordinates(true, graphics2d);
    try {
      graphics2d.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_BEVEL));

      final double minX = boundingBox.getMinX();
      final double maxX = boundingBox.getMaxX();
      final double minY = boundingBox.getMinY();
      final double maxY = boundingBox.getMaxY();

      final int startXIndex = (int)Math.ceil(minX / majorDivisions);
      final int endXIndex = (int)Math.ceil(maxX / majorDivisions);
      graphics2d.setColor(Color.GRAY);
      for (int i = startXIndex; i < endXIndex; i++) {
        final double x = i * majorDivisions;
        graphics2d.draw(new Line2D.Double(x, minY, x, maxY));
      }

      final int startYIndex = (int)Math.ceil(minY / majorDivisions);
      final int endYIndex = (int)Math.ceil(maxY / majorDivisions);
      for (int i = startYIndex; i < endYIndex; i++) {
        final double y = i * majorDivisions;
        graphics2d.draw(new Line2D.Double(minX, y, maxX, y));
      }
    } finally {
      // viewport.setUseModelCoordinates(saved, graphics2d);
    }
    return PAGE_EXISTS;
  }
}
