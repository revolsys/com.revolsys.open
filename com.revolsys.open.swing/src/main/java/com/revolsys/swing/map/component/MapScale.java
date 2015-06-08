package com.revolsys.swing.map.component;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import com.revolsys.swing.map.Viewport2D;
import com.revolsys.util.Property;

public class MapScale extends JLabel implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  public static String formatScale(final Object scale) {
    double scaleDouble;
    if (scale instanceof Number) {
      final Number number = (Number)scale;
      scaleDouble = number.doubleValue();
      if (scaleDouble <= 0 || number.longValue() == Long.MAX_VALUE
        || scaleDouble == Double.MAX_VALUE) {
        return "Unlimited";
      } else if (Double.isNaN(scaleDouble) || Double.isInfinite(scaleDouble)) {
        return "Unknown";
      }
    } else {
      if (scale == null) {
        return "Unknown";
      } else {
        final String string = scale.toString();
        if (Property.hasValue(string)) {
          return string;
        } else {
          return "Unknown";
        }
      }
    }
    return "1:" + getFormat().format(scaleDouble);
  }

  public static DecimalFormat getFormat() {
    final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
    formatSymbols.setGroupingSeparator(' ');
    final DecimalFormat format = new DecimalFormat("#,###");
    format.setDecimalFormatSymbols(formatSymbols);
    return format;
  }

  private final Viewport2D viewport;

  public MapScale(final Viewport2D viewport) {
    this.viewport = viewport;
    setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

    setPreferredSize(new Dimension(100, 20));
    Property.addListener(viewport, "scale", this);
    setToolTipText("Map Scale");
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if ("scale".equals(event.getPropertyName())) {
      final double scale = this.viewport.getScale();
      setText(formatScale(scale));
    }
  }

}
