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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.BaseUnit;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.swing.border.AbstractBorder;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.swing.map.Viewport2D;

public class MapRulerBorder extends AbstractBorder implements
  PropertyChangeListener {
  private final int rulerSize = 25;

  /**
   * 
   */
  private static final long serialVersionUID = -3070841484052913548L;

  private static final List<Unit<Angle>> METRIC_GEOGRAPHICS_STEPS = createSteps(
    NonSI.DEGREE_ANGLE, 30, 10, 1, 1e-1, 1e-2, 1e-3, 1e-4, 1e-5, 1e-6, 1e-7,
    1e-8, 1e-9, 1e-10, 1e-11, 1e-12, 1e-13, 1e-14, 1e-15);

  private static final List<Unit<Length>> METRIC_PROJECTED_STEPS = createSteps(
    SI.METRE, 1e8, 1e7, 1e6, 1e5, 1e4, 1e3, 1e2, 1e1, 1, 1e-1, 1e-2, 1e-3,
    1e-4, 1e-5, 1e-6, 1e-7, 1e-8, 1e-9, 1e-10, 1e-11, 1e-12, 1e-13, 1e-14,
    1e-15);

  private static final List<Unit<Length>> IMPERIAL_PROJECTED_STEPS = createSteps(
    NonSI.MILE.times(1000), NonSI.MILE.times(100), NonSI.MILE.times(10),
    NonSI.MILE, NonSI.MILE.divide(16), NonSI.MILE.divide(32), NonSI.FOOT,
    NonSI.INCH);

  private static final List<Unit<Length>> IMPERIAL_MILE_STEPS = createSteps(
    NonSI.MILE.times(1000), NonSI.MILE.times(100), NonSI.MILE.times(10),
    NonSI.MILE, NonSI.MILE.divide(10), NonSI.MILE.divide(100));

  private static final List<Unit<Length>> IMPERIAL_FOOT_STEPS = createSteps(
    NonSI.FOOT.times(1000000), NonSI.FOOT.times(100000),
    NonSI.FOOT.times(10000), NonSI.FOOT.times(1000), NonSI.FOOT.times(100),
    NonSI.FOOT.times(10), NonSI.FOOT);

  public static <U extends Quantity> List<Unit<U>> createSteps(
    final Unit<U>... steps) {
    final List<Unit<U>> stepList = new ArrayList<Unit<U>>();
    for (final Unit<U> step : steps) {
      stepList.add(step);
    }
    return stepList;
  }

  /**
   * Create a list of steps in measurable units from the double array.
   * 
   * @param <U> The type of unit (e.g. {@link Angle} or {@link Length}).
   * @param unit The unit of measure.
   * @param steps The list of steps.
   * @return The list of step measures.
   */
  public static <U extends Quantity> List<Unit<U>> createSteps(
    final Unit<U> unit, final double... steps) {
    final List<Unit<U>> stepList = new ArrayList<Unit<U>>();
    for (final double step : steps) {
      if (step == 1) {
        stepList.add(unit);
      } else {
        stepList.add(unit.times(step));
      }
    }
    return stepList;
  }

  private final Viewport2D viewport;

  private int labelHeight;

  private GeometryFactory rulerGeometryFactory;

  private CoordinateSystem rulerCoordinateSystem;

  @SuppressWarnings("rawtypes")
  private Unit baseUnit;

  private double areaMinX;

  private double areaMaxX;

  private double areaMinY;

  private double areaMaxY;

  public MapRulerBorder(final Viewport2D viewport) {
    this.viewport = viewport;
    final GeometryFactory geometryFactory = viewport.getGeometryFactory();
    setRulerGeometryFactory(geometryFactory);
    viewport.addPropertyChangeListener("geometryFactory", this);
  }

  private <Q extends Quantity> void drawLabel(final Graphics2D graphics,
    final int textX, final int textY, final Unit<Q> displayUnit,
    final double displayValue, final Unit<Q> scaleUnit) {
    DecimalFormat format;
    if (displayValue - Math.floor(displayValue) == 0) {
      format = new DecimalFormat("#,###,###,###");
    } else {
      final StringBuffer formatString = new StringBuffer("#,###,###,###.");
      final double stepSize = Measure.valueOf(1, scaleUnit).doubleValue(
        displayUnit);
      final int numZeros = (int)Math.abs(Math.round(Math.log10(stepSize % 1.0)));
      for (int j = 0; j < numZeros; j++) {
        formatString.append("0");
      }
      format = new DecimalFormat(formatString.toString());
    }
    final String label = String.valueOf(format.format(displayValue)
      + displayUnit);
    graphics.setColor(Color.BLACK);
    graphics.drawString(label, textX, textY);
  }

  /**
   * Returns the insets of the border.
   * @param c the component for which this border insets value applies
   */
  @Override
  public Insets getBorderInsets(final Component c) {
    return new Insets(rulerSize, rulerSize, rulerSize, rulerSize);
  }

  /** 
   * Reinitialize the insets parameter with this Border's current Insets. 
   * @param c the component for which this border insets value applies
   * @param insets the object to be reinitialized
   */
  @Override
  public Insets getBorderInsets(final Component c, final Insets insets) {
    insets.left = rulerSize;
    insets.top = rulerSize;
    insets.right = rulerSize;
    insets.bottom = rulerSize;
    return insets;
  }

  public GeometryFactory getRulerGeometryFactory() {
    return rulerGeometryFactory;
  }

  private <Q extends Quantity> int getStepLevel(final List<Unit<Q>> steps,
    final Measure<Q> modelUnitsPer10ViewUnits) {
    for (int i = 0; i < steps.size(); i++) {
      final Unit<Q> stepUnit = steps.get(i);
      final Measure<Q> step = Measure.valueOf(1, stepUnit);
      final int compare = modelUnitsPer10ViewUnits.compareTo(step);
      if (compare > 0) {
        if (i == 0) {
          return 0;
        } else {
          return i - 1;
        }
      }
    }
    return steps.size() - 1;
  }

  private void paintBackground(final Graphics2D g, final int x, final int y,
    final int width, final int height) {
    g.setColor(Color.WHITE);
    g.fillRect(x, y, rulerSize - 1, height); // left
    g.fillRect(x + width - rulerSize + 1, y, rulerSize - 1, height - 1); // right
    g.fillRect(x + rulerSize - 1, y, width - 2 * rulerSize + 2, rulerSize - 1); // top
    g.fillRect(x + rulerSize - 1, y + height - rulerSize + 1, width - 2
      * rulerSize + 2, rulerSize - 1); // bottom
  }

  @Override
  public void paintBorder(final Component c, final Graphics g, final int x,
    final int y, final int width, final int height) {
    final Graphics2D graphics = (Graphics2D)g;

    graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
    final FontMetrics fontMetrics = graphics.getFontMetrics();
    labelHeight = fontMetrics.getHeight();

    paintBackground(graphics, x, y, width, height);

    final BoundingBox boundingBox = viewport.getBoundingBox();
    if (rulerCoordinateSystem instanceof GeographicCoordinateSystem) {
      final Unit<Angle> displayUnit = NonSI.DEGREE_ANGLE;
      paintRuler(graphics, boundingBox, displayUnit, METRIC_GEOGRAPHICS_STEPS,
        true, x, y, width, height);
    } else if (rulerCoordinateSystem instanceof ProjectedCoordinateSystem) {
      if (baseUnit.equals(NonSI.FOOT)) {
        final Unit<Length> displayUnit = NonSI.FOOT;
        paintRuler(graphics, boundingBox, displayUnit, IMPERIAL_FOOT_STEPS,
          true, x, y, width, height);
      } else {
        final BaseUnit<Length> displayUnit = SI.METRE;
        paintRuler(graphics, boundingBox, displayUnit, METRIC_PROJECTED_STEPS,
          true, x, y, width, height);
      }
    }
    graphics.setColor(Color.BLACK);
    graphics.drawRect(rulerSize - 1, rulerSize - 1, width - 2 * rulerSize + 1,
      height - 2 * rulerSize + 1);

  }

  private <Q extends Quantity> void paintHorizontalRuler(final Graphics2D g,
    final BoundingBox boundingBox, final Unit<Q> displayUnit,
    final List<Unit<Q>> steps, final int x, final int y, final int width,
    final int height, final boolean top) {

    final AffineTransform transform = g.getTransform();
    final Shape clip = g.getClip();
    try {
      int textY;
      LineSegment line;
      if (top) {
        g.translate(rulerSize, 0);
        textY = labelHeight;
        line = boundingBox.getNorthLine();
      } else {
        g.translate(rulerSize, height - rulerSize);
        textY = rulerSize - 3;
        line = boundingBox.getSouthLine();
      }
      line = line.convert(rulerGeometryFactory);

      g.setClip(0, 0, width - 2 * rulerSize, rulerSize);

      final double mapSize = boundingBox.getWidth();
      final double viewSize = viewport.getViewWidthPixels();
      final double minX = line.getX(0);
      double maxX = line.getX(1);
      if (maxX > areaMaxX) {
        maxX = areaMaxX;
      }

      if (mapSize > 0) {
        final Unit<Q> screenToModelUnit = viewport.getViewToModelUnit(baseUnit);
        final Measure<Q> modelUnitsPer6ViewUnits = Measure.valueOf(6,
          screenToModelUnit);
        final int stepLevel = getStepLevel(steps, modelUnitsPer6ViewUnits);
        final Unit<Q> stepUnit = steps.get(stepLevel);
        final double step = toBaseUnit(Measure.valueOf(1, stepUnit));

        final double pixelsPerUnit = viewSize / mapSize;

        final long minIndex = (long)Math.floor(areaMinX / step);
        final long maxIndex = (long)Math.floor(maxX / step);
        long startIndex = (long)Math.floor(minX / step);
        if (startIndex < minIndex) {
          startIndex = minIndex;
        }
        for (long index = startIndex; index < maxIndex; index++) {
          final Measure<Q> measureValue = Measure.valueOf(index, stepUnit);
          final double value = toBaseUnit(measureValue);
          final double displayValue = measureValue.doubleValue(displayUnit);
          final int pixel = (int)((value - minX) * pixelsPerUnit);
          boolean found = false;
          int barSize = 4;

          g.setColor(Color.LIGHT_GRAY);
          for (int i = 0; !found && i < stepLevel; i++) {
            final Unit<Q> scaleUnit = steps.get(i);
            final double stepValue = measureValue.doubleValue(scaleUnit);

            if (Math.abs(stepValue - Math.round(stepValue)) < 0.000001) {
              barSize = 4 + (int)((rulerSize - 4) * (((double)stepLevel - i) / stepLevel));
              found = true;
              drawLabel(g, pixel + 3, textY, displayUnit, displayValue,
                scaleUnit);
            }

          }

          if (top) {
            g.drawLine(pixel, rulerSize - 1 - barSize, pixel, rulerSize - 1);
          } else {
            g.drawLine(pixel, 0, pixel, barSize);
          }

        }

      }
    } finally {
      g.setTransform(transform);
      g.setClip(clip);
    }
  }

  private <Q extends Quantity> void paintRuler(final Graphics2D g,
    final BoundingBox boundingBox, final Unit<Q> displayUnit,
    final List<Unit<Q>> steps, final boolean horizontal, final int x,
    final int y, final int width, final int height) {
    paintHorizontalRuler(g, boundingBox, displayUnit, steps, x, y, width,
      height, true);
    paintHorizontalRuler(g, boundingBox, displayUnit, steps, x, y, width,
      height, false);

    paintVerticalRuler(g, boundingBox, displayUnit, steps, x, y, width, height,
      true);
    paintVerticalRuler(g, boundingBox, displayUnit, steps, x, y, width, height,
      false);

  }

  private <Q extends Quantity> void paintVerticalRuler(final Graphics2D g,
    final BoundingBox boundingBox, final Unit<Q> displayUnit,
    final List<Unit<Q>> steps, final int x, final int y, final int width,
    final int height, final boolean left) {

    final AffineTransform transform = g.getTransform();
    final Shape clip = g.getClip();
    try {
      int textX;
      LineSegment line;
      if (left) {
        g.translate(0, -rulerSize);
        textX = labelHeight;
        line = boundingBox.getWestLine();
      } else {
        g.translate(width - rulerSize, -rulerSize);
        textX = rulerSize - 3;
        line = boundingBox.getEastLine();
      }
      line = line.convert(rulerGeometryFactory);

      g.setClip(0, rulerSize * 2, rulerSize, height - 2 * rulerSize);

      final double mapSize = boundingBox.getHeight();
      final double viewSize = viewport.getViewHeightPixels();
      final double minY = line.getY(0);
      double maxY = line.getY(1);
      if (maxY > areaMaxY) {
        maxY = areaMaxY;
      }

      if (mapSize > 0) {
        final Unit<Q> screenToModelUnit = viewport.getViewToModelUnit(baseUnit);
        final Measure<Q> modelUnitsPer6ViewUnits = Measure.valueOf(6,
          screenToModelUnit);
        final int stepLevel = getStepLevel(steps, modelUnitsPer6ViewUnits);
        final Unit<Q> stepUnit = steps.get(stepLevel);
        final double step = toBaseUnit(Measure.valueOf(1, stepUnit));

        final double pixelsPerUnit = viewSize / mapSize;

        final long minIndex = (long)Math.ceil(areaMinY / step);
        final long maxIndex = (long)Math.ceil(maxY / step);
        long startIndex = (long)Math.floor(minY / step);
        if (startIndex < minIndex) {
          startIndex = minIndex;
        }
        for (long index = startIndex; index < maxIndex; index++) {
          final Measure<Q> measureValue = Measure.valueOf(index, stepUnit);
          final double value = toBaseUnit(measureValue);
          final double displayValue = measureValue.doubleValue(displayUnit);
          final int pixel = (int)((value - minY) * pixelsPerUnit);
          boolean found = false;
          int barSize = 4;

          g.setColor(Color.LIGHT_GRAY);
          for (int i = 0; !found && i < stepLevel; i++) {
            final Unit<Q> scaleUnit = steps.get(i);
            final double stepValue = measureValue.doubleValue(scaleUnit);

            if (Math.abs(stepValue - Math.round(stepValue)) < 0.000001) {
              barSize = 4 + (int)((rulerSize - 4) * (((double)stepLevel - i) / stepLevel));
              found = true;
              final AffineTransform transform2 = g.getTransform();
              try {
                g.translate(textX, height - pixel - 3);
                g.rotate(-Math.PI / 2);
                drawLabel(g, 0, 0, displayUnit, displayValue, scaleUnit);
              } finally {
                g.setTransform(transform2);
              }
            }

          }

          if (left) {
            g.drawLine(rulerSize - 1 - barSize, height - pixel, rulerSize - 1,
              height - pixel);
          } else {
            g.drawLine(0, height - pixel, barSize, height - pixel);
          }

        }

      }
    } finally {
      g.setTransform(transform);
      g.setClip(clip);
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final GeometryFactory geometryFactory = (GeometryFactory)event.getNewValue();
    setRulerGeometryFactory(geometryFactory);
  }

  public void setRulerGeometryFactory(final GeometryFactory rulerGeometryFactory) {
    this.rulerGeometryFactory = rulerGeometryFactory;
    if (rulerGeometryFactory == null) {
      this.rulerGeometryFactory = viewport.getGeometryFactory();
    } else {
      this.rulerGeometryFactory = rulerGeometryFactory;
    }
    rulerCoordinateSystem = this.rulerGeometryFactory.getCoordinateSystem();
    baseUnit = rulerCoordinateSystem.getUnit();

    final BoundingBox areaBoundingBox = rulerCoordinateSystem.getAreaBoundingBox();

    areaMinX = areaBoundingBox.getMinX();
    areaMaxX = areaBoundingBox.getMaxX();
    areaMinY = areaBoundingBox.getMinY();
    areaMaxY = areaBoundingBox.getMaxY();
  }

  @SuppressWarnings("unchecked")
  private <Q extends Quantity> double toBaseUnit(final Measurable<Q> value) {
    return value.doubleValue(baseUnit);
  }

  @SuppressWarnings("unchecked")
  private <Q extends Quantity> double toBaseUnit(final Measure<Q> value) {
    return value.doubleValue(baseUnit);
  }

}
