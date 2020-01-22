package com.revolsys.swing.map.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.swing.border.AbstractBorder;

import org.jeometry.common.awt.WebColors;
import org.jeometry.coordinatesystem.model.GeographicCoordinateSystem;
import org.jeometry.coordinatesystem.model.HorizontalCoordinateSystem;
import org.jeometry.coordinatesystem.model.unit.Degree;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.util.Property;

import tech.units.indriya.quantity.Quantities;

public class MapRulerBorder extends AbstractBorder {

  private static final Color COLOR_OUTSIDE_AREA = new Color(239, 239, 239);

  private static final double[] GEOGRAPHICS_STEPS = {
    30, 10, 1, 1e-1, 1e-2, 1e-3, 1e-4, 1e-5, 1e-6, 1e-7, 1e-8, 1e-9, 1e-10, 1e-11, 1e-12, 1e-13,
    1e-14, 1e-15
  };

  private static final double[] STEPS = {
    1e8, 1e7, 1e6, 1e5, 1e4, 1e3, 1e2, 1e1, 1, 1e-1, 1e-2, 1e-3, 1e-4, 1e-5, 1e-6, 1e-7, 1e-8, 1e-9,
    1e-10, 1e-11, 1e-12, 1e-13, 1e-14, 1e-15
  };

  /**
   *
   */
  private static final long serialVersionUID = -3070841484052913548L;

  /**
   * Construct a new list of steps in measurable units from the double array.
   *
   * @param <U> The type of unit (e.g. {@link Angle} or {@link Length}).
   * @param unit The unit of measure.
   * @param steps The list of steps.
   * @return The list of step measures.
   */
  public static <U extends Quantity<U>> List<Quantity<U>> newSteps(final Unit<U> unit,
    final double... steps) {
    final List<Quantity<U>> stepList = new ArrayList<>();
    for (final double step : steps) {
      stepList.add(Quantities.getQuantity(step, unit));
    }
    return stepList;
  }

  private double areaMaxX;

  private double areaMaxY;

  private double areaMinX;

  private double areaMinY;

  private int labelHeight;

  private GeometryFactory rulerGeometryFactory;

  private final int rulerSize = 25;

  private final Viewport2D viewport;

  private double unitsPerPixel;

  private int stepLevel;

  private double step;

  private double[] steps = STEPS;

  private String unitLabel;

  private BoundingBox boundingBox;

  private double pixelsPerUnit;

  public MapRulerBorder(final Viewport2D viewport) {
    this.viewport = viewport;
    updateValues();
    Property.addListenerRunnable(viewport, "boundingBox", this::updateValues);
    Property.addListenerRunnable(viewport, "unitsPerPixel", this::updateValues);
  }

  private <Q extends Quantity<Q>> void drawLabel(final Graphics2D graphics, final int textX,
    final int textY, final double displayValue, final double stepSize) {
    DecimalFormat format;
    if (displayValue - Math.floor(displayValue) == 0) {
      format = new DecimalFormat("#,###,###,###");
    } else {
      final StringBuilder formatString = new StringBuilder("#,###,###,###.");
      final int numZeros = (int)Math.abs(Math.round(Math.log10(stepSize % 1.0)));
      for (int j = 0; j < numZeros; j++) {
        formatString.append("0");
      }
      format = new DecimalFormat(formatString.toString());
    }
    final String label = String.valueOf(format.format(displayValue) + this.unitLabel);
    graphics.setColor(Color.BLACK);
    graphics.drawString(label, textX, textY);
  }

  /**
   * Returns the insets of the border.
   * @param c the component for which this border insets value applies
   */
  @Override
  public Insets getBorderInsets(final Component c) {
    return new Insets(this.rulerSize, this.rulerSize, this.rulerSize, this.rulerSize);
  }

  /**
   * Reinitialize the insets parameter with this Border's current Insets.
   * @param c the component for which this border insets value applies
   * @param insets the object to be reinitialized
   */
  @Override
  public Insets getBorderInsets(final Component c, final Insets insets) {
    insets.left = this.rulerSize;
    insets.top = this.rulerSize;
    insets.right = this.rulerSize;
    insets.bottom = this.rulerSize;
    return insets;
  }

  private <Q extends Quantity<Q>> int getStepLevel(final double[] steps) {
    final double sizeOf6Pixels = this.unitsPerPixel * 6;
    int i = 0;
    for (final double step : steps) {
      if (sizeOf6Pixels > step) {
        if (i == 0) {
          return 0;
        } else {
          return i - 1;
        }
      }
      i++;
    }
    return i - 1;
  }

  private void paintBackground(final Graphics2D g, final int x, final int y, final int width,
    final int height) {
    g.setColor(Color.WHITE);
    g.fillRect(x, y, this.rulerSize - 1, height); // left
    g.fillRect(x + width - this.rulerSize + 1, y, this.rulerSize - 1, height - 1); // right
    g.fillRect(x + this.rulerSize - 1, y, width - 2 * this.rulerSize + 2, this.rulerSize - 1); // top
    g.fillRect(x + this.rulerSize - 1, y + height - this.rulerSize + 1,
      width - 2 * this.rulerSize + 2, this.rulerSize - 1); // bottom
  }

  @Override
  public void paintBorder(final Component c, final Graphics g, final int x, final int y,
    final int width, final int height) {
    final Graphics2D graphics = (Graphics2D)g;
    if (width > 0 && this.unitsPerPixel > 0) {

      paintBackground(graphics, x, y, width, height);

      if (this.boundingBox.getWidth() > 0) {
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        final FontMetrics fontMetrics = graphics.getFontMetrics();
        this.labelHeight = fontMetrics.getHeight();

        paintRuler(graphics, true, x, y, width, height);
        graphics.setColor(Color.BLACK);
        graphics.drawRect(this.rulerSize - 1, this.rulerSize - 1, width - 2 * this.rulerSize + 1,
          height - 2 * this.rulerSize + 1);
      }
      g.setColor(WebColors.DarkGray);
      g.drawRect(0, 0, width - 1, height - 1);
    }
  }

  private <Q extends Quantity<Q>> void paintHorizontalRuler(final Graphics2D g, final int x,
    final int y, final int width, final int height, final boolean top) {
    final double viewSize = this.viewport.getViewWidthPixels();
    if (viewSize > 0) {
      final AffineTransform transform = g.getTransform();
      final Shape clip = g.getClip();
      try {
        int textY;

        final double minX = this.boundingBox.getMinX();
        double maxX = this.boundingBox.getMaxX();
        if (top) {
          g.translate(this.rulerSize, 0);
          textY = this.labelHeight;
        } else {
          g.translate(this.rulerSize, height - this.rulerSize);
          textY = this.rulerSize - 3;
        }

        g.setClip(0, 0, width - 2 * this.rulerSize, this.rulerSize);

        final long maxIndex = (long)Math.floor(maxX / this.step);
        long startIndex = (long)Math.floor(minX / this.step);
        if (minX < this.areaMinX) {
          startIndex = (long)Math.floor(this.areaMinX / this.step);
          final double delta = this.areaMinX - minX;
          final int minPixel = (int)Math.floor(delta * this.pixelsPerUnit);
          g.setColor(COLOR_OUTSIDE_AREA);
          g.fillRect(0, 0, minPixel, this.rulerSize);
        }
        if (maxX > this.areaMaxX) {
          final double delta = this.areaMaxX - minX;
          final int maxPixel = (int)Math.floor(delta * this.pixelsPerUnit);
          g.setColor(COLOR_OUTSIDE_AREA);
          g.fillRect(maxPixel, 0, width - 2 * this.rulerSize - maxPixel, this.rulerSize);
          maxX = this.areaMaxX;
        }

        for (long index = startIndex; index <= maxIndex; index++) {
          final double value = this.step * index;
          final int pixel = (int)((value - minX) * this.pixelsPerUnit);
          boolean found = false;
          int barSize = 4;

          g.setColor(Color.LIGHT_GRAY);
          for (int i = 0; !found && i < this.stepLevel; i++) {
            final double stepResolution = this.steps[i];
            final double diff = Math.abs(value % stepResolution);
            if (diff < 0.000001) {
              barSize = 4
                + (int)((this.rulerSize - 4) * (((double)this.stepLevel - i) / this.stepLevel));
              found = true;
              drawLabel(g, pixel + 3, textY, value, stepResolution);
            }

          }

          if (top) {
            g.drawLine(pixel, this.rulerSize - 1 - barSize, pixel, this.rulerSize - 1);
          } else {
            g.drawLine(pixel, 0, pixel, barSize);
          }

        }
      } finally {
        g.setTransform(transform);
        g.setClip(clip);
      }
    }
  }

  private <Q extends Quantity<Q>> void paintRuler(final Graphics2D g, final boolean horizontal,
    final int x, final int y, final int width, final int height) {
    paintHorizontalRuler(g, x, y, width, height, true);
    paintHorizontalRuler(g, x, y, width, height, false);

    paintVerticalRuler(g, x, y, width, height, true);
    paintVerticalRuler(g, x, y, width, height, false);

  }

  private <Q extends Quantity<Q>> void paintVerticalRuler(final Graphics2D g, final int x,
    final int y, final int width, final int height, final boolean left) {
    final double viewSize = this.viewport.getViewHeightPixels();
    if (viewSize > 0) {
      final AffineTransform transform = g.getTransform();
      final Shape clip = g.getClip();
      try {
        int textX;
        final double minY = this.boundingBox.getMinY();
        double maxY = this.boundingBox.getMaxY();
        if (left) {
          g.translate(0, this.rulerSize);
          textX = this.labelHeight;
        } else {
          g.translate(width - this.rulerSize, this.rulerSize);
          textX = this.rulerSize - 3;
        }

        final int rulerHeight = height - 2 * this.rulerSize;
        g.setClip(0, 0, this.rulerSize, rulerHeight);

        if (minY < this.areaMinY) {
          final double delta = this.areaMinY - minY;
          final int minPixel = (int)Math.floor(delta * this.pixelsPerUnit);
          g.setColor(COLOR_OUTSIDE_AREA);
          g.fillRect(0, rulerHeight - minPixel, this.rulerSize, minPixel);
        }
        if (maxY > this.areaMaxY) {
          final double delta = this.areaMaxY - minY;
          final int maxPixel = (int)Math.floor(delta * this.pixelsPerUnit);
          g.setColor(COLOR_OUTSIDE_AREA);
          g.fillRect(0, 0, this.rulerSize, rulerHeight - maxPixel);
          maxY = this.areaMaxY;
        }

        final long minIndex = (long)Math.ceil(this.areaMinY / this.step);
        final long maxIndex = (long)Math.ceil(maxY / this.step);
        long startIndex = (long)Math.floor(minY / this.step);
        if (startIndex < minIndex) {
          startIndex = minIndex;
        }
        for (long index = startIndex; index <= maxIndex; index++) {
          final double value = this.step * index;
          final int pixel = (int)((value - minY) * this.pixelsPerUnit);
          boolean found = false;
          int barSize = 4;

          g.setColor(Color.LIGHT_GRAY);
          for (int i = 0; !found && i < this.stepLevel; i++) {
            final double stepResolution = this.steps[i];
            final double diff = Math.abs(value % stepResolution);
            if (diff < 0.000001) {
              barSize = 4
                + (int)((this.rulerSize - 4) * (((double)this.stepLevel - i) / this.stepLevel));
              found = true;
              final AffineTransform transform2 = g.getTransform();
              try {
                g.translate(textX, rulerHeight - pixel - 3);
                g.rotate(-Math.PI / 2);
                drawLabel(g, 0, 0, value, value);
              } finally {
                g.setTransform(transform2);
              }
            }

          }

          if (left) {
            g.drawLine(this.rulerSize - 1 - barSize, rulerHeight - pixel, this.rulerSize - 1,
              rulerHeight - pixel);
          } else {
            g.drawLine(0, rulerHeight - pixel, barSize, rulerHeight - pixel);
          }

        }

      } finally {
        g.setTransform(transform);
        g.setClip(clip);
      }
    }
  }

  private void setRulerGeometryFactory(final GeometryFactory rulerGeometryFactory) {
    if (this.rulerGeometryFactory != rulerGeometryFactory) {
      if (rulerGeometryFactory == null) {
        this.rulerGeometryFactory = this.viewport.getGeometryFactory();
      } else {
        this.rulerGeometryFactory = rulerGeometryFactory;
      }
      this.steps = STEPS;
      if (this.rulerGeometryFactory.isHasHorizontalCoordinateSystem()) {
        final HorizontalCoordinateSystem rulerCoordinateSystem = this.rulerGeometryFactory
          .getHorizontalCoordinateSystem();
        this.unitLabel = rulerCoordinateSystem.getUnitLabel();
        if (this.rulerGeometryFactory.isGeographic()) {
          final GeographicCoordinateSystem geoCs = (GeographicCoordinateSystem)rulerCoordinateSystem;
          if (geoCs.getAngularUnit() instanceof Degree) {
            this.areaMinX = -180;
            this.areaMaxX = 180;
            this.areaMinY = -90;
            this.areaMaxY = 90;
            this.steps = GEOGRAPHICS_STEPS;
          }
        } else {
          final BoundingBox areaBoundingBox = this.rulerGeometryFactory.getAreaBoundingBox();

          this.areaMinX = areaBoundingBox.getMinX();
          this.areaMaxX = areaBoundingBox.getMaxX();
          this.areaMinY = areaBoundingBox.getMinY();
          this.areaMaxY = areaBoundingBox.getMaxY();
        }
      }
    }
  }

  private void updateValues() {
    this.boundingBox = this.viewport.getBoundingBox();
    this.unitsPerPixel = this.viewport.getUnitsPerPixel();
    this.pixelsPerUnit = this.viewport.getPixelsPerXUnit();
    final GeometryFactory geometryFactory = this.boundingBox.getGeometryFactory();
    setRulerGeometryFactory(geometryFactory);
    this.stepLevel = getStepLevel(this.steps);
    this.step = this.steps[this.stepLevel];
    this.viewport.update();
  }
}
