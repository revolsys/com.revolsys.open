package com.revolsys.swing.map.symbolizer;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;

public class Stroke implements PropertyChangeListener {
  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  /** SVG stroke. */
  private CharSequence colorString;

  /** SVG stroke-opacity. */
  private Number opacity = 1;

  /** SVG stroke-dasharray. */
  private List<Measure<Length>> dashArray;

  /** SVG stroke-dashoffset. */
  private Measure<Length> dashOffset =Measure.valueOf(0.0, NonSI.PIXEL);

  private Fill fillPattern;

  /** SVG stroke-linecap. */
  private CharSequence lineCap = "butt";

  /** SVG stroke-linejoin. */
  private CharSequence lineJoin = "miter";

  /** SVG stroke-mitrelimit. */
  private double mitreLimit = 10;

  /** SVG stroke-width. */
  private Measure<Length> width = Measure.valueOf(1, NonSI.PIXEL);

  public Stroke() {
    this(null, null, null, 10, null, null, null);
  }

  public Stroke(final Color color, double width) {
    this.color = color;
    this.width = Measure.valueOf(width, NonSI.PIXEL);
  }

  public Stroke(final CharSequence color, final Measure<Length> width) {
    this(color, width, null, 10, null, null, null);

  }

  public Stroke(final CharSequence color, final Measure<Length> width,
    final CharSequence lineCap, final double mitreLimit,
    final CharSequence lineJoin, final List<Measure<Length>> dash,
    final Measure<Length> dashOffset) {
    setColor(color);
    setWidth(width);
    setLineCap(lineCap);
    setMitreLimit(mitreLimit);
    setLineJoin(lineJoin);
    this.dashArray = dash;
    setDashOffset(dashOffset);
  }

  public Stroke(final Stroke stroke) {
    this(stroke.getColorString(), stroke.getWidth(), stroke.getLineCap(),
      stroke.getMitreLimit(), stroke.getLineJoin(), stroke.getDashArray(),
      stroke.getDashOffset());
  }

  /**
   * Add the property change listener.
   * 
   * @param listener The listener.
   */
  public void addPropertyChangeListener(final PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Add the property change listener from the specified property.
   * 
   * @param propertyName The property name.
   * @param listener The listener.
   */
  public void addPropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  @Override
  public Stroke clone() {
    return new Stroke(this);
  }

  /**
   * Return the opacity as an alpha value in the range 0-255.
   * 
   * @return The alpha.
   */
  public Number getAlpha() {
    return (int)(opacity.floatValue() * 255 + 0.5);
  }

  public CharSequence getColorString() {
    return colorString;
  }

  private Color color;

  public Color getColor() {
    if (color == null) {
      color = CssUtil.getColor(getColorString(), getAlpha());
    }
    return color;
  }

  public List<Measure<Length>> getDashArray() {
    return dashArray;
  }

  public Measure<Length> getDashOffset() {
    return dashOffset;
  }

  public Fill getFillPattern() {
    return fillPattern;
  }

  public CharSequence getLineCap() {
    return lineCap;
  }

  public CharSequence getLineJoin() {
    return lineJoin;
  }

  public double getMitreLimit() {
    return mitreLimit;
  }

  public Number getOpacity() {
    return opacity;
  }

  public Measure<Length> getWidth() {
    return width;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    propertyChangeSupport.firePropertyChange(evt);
  }

  /**
   * Remove the property change listener.
   * 
   * @param listener The listener.
   */
  public void removePropertyChangeListener(final PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   * Remove the property change listener from the specified property.
   * 
   * @param propertyName The property name.
   * @param listener The listener.
   */
  public void removePropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }

  public void setColor(final CharSequence color) {
    final CharSequence oldValue = this.colorString;
    if (color == null) {
      this.colorString = "#000000";
    } else {
      this.colorString = color;
    }
    propertyChangeSupport.firePropertyChange("color", oldValue,
      this.colorString);
  }

  public void setDashArray(final List<Measure<Length>> dashArray) {
    final Object oldValue = this.dashArray;
    this.dashArray = dashArray;
    propertyChangeSupport.firePropertyChange("dashArray", oldValue, dashArray);
  }

  public void setDashOffset(final Measure<Length> dashOffset) {
    final Object oldValue = this.dashOffset;
    if (dashOffset == null) {
      this.dashOffset = Measure.valueOf(0.0, NonSI.PIXEL);
    } else {
      this.dashOffset = dashOffset;
    }
    propertyChangeSupport.firePropertyChange("dashOffset", oldValue,
      this.dashOffset);
  }

  public void setFillPattern(final Fill fill) {

    if (fill != this.fillPattern) {
      final Fill oldValue = this.fillPattern;
      if (oldValue != null) {
        oldValue.removePropertyChangeListener(this);
      }

      this.fillPattern = fill;
      if (fill != null) {
        fill.addPropertyChangeListener(this);
      }
      propertyChangeSupport.firePropertyChange("fillPattern", oldValue, fill);
    }
  }

  public void setLineCap(final CharSequence lineCap) {
    final Object oldValue = this.lineCap;
    if (lineCap == null) {
      this.lineCap = "butt";
    } else {
      this.lineCap = lineCap;
    }
    propertyChangeSupport.firePropertyChange("lineCap", oldValue, this.lineCap);
  }

  public void setLineJoin(final CharSequence lineJoin) {
    final Object oldValue = this.lineJoin;
    if (lineJoin == null) {
      this.lineJoin = "miter";
    } else {
      this.lineJoin = lineJoin;
    }
    propertyChangeSupport.firePropertyChange("lineJoin", oldValue, lineJoin);
  }

  public void setMitreLimit(final double mitreLimit) {
    final Object oldValue = this.mitreLimit;
    this.mitreLimit = mitreLimit;
    propertyChangeSupport.firePropertyChange("mitreLimit", oldValue, mitreLimit);
  }

  public void setOpacity(final Number opacity) {
    final Object oldValue = this.opacity;
    if (opacity == null || opacity.floatValue() > 1) {
      this.opacity = 1;
    } else if (opacity.floatValue() < 0) {
      this.opacity = 0;
    } else {
      this.opacity = opacity;
    }
    propertyChangeSupport.firePropertyChange("opacity", oldValue, this.opacity);
  }

  public void setWidth(final Measure<Length> width) {
    final Object oldValue = this.width;
    if (width == null) {
      this.width = Measure.valueOf(1.0, NonSI.PIXEL);
    } else {
      this.width = width;
    }
    propertyChangeSupport.firePropertyChange("width", oldValue, this.width);
  }
}
