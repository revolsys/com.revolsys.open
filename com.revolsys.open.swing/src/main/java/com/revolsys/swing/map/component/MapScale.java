package com.revolsys.swing.map.component;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import org.springframework.util.StringUtils;

import com.revolsys.swing.map.Viewport2D;

@SuppressWarnings("serial")
public class MapScale extends JLabel implements PropertyChangeListener {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public static final DecimalFormat FORMAT = new DecimalFormat("#,###");

  static {
    final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
    formatSymbols.setGroupingSeparator(' ');
    FORMAT.setDecimalFormatSymbols(formatSymbols);
  }

  public static String formatScale(final Object scale) {
    double scaleDouble;
    if (scale instanceof Number) {
      final Number number = (Number)scale;
      scaleDouble = number.doubleValue();
    } else {
      if (scale == null) {
        return "Unknown";
      } else {
        final String string = scale.toString();
        if (StringUtils.hasText(string)) {
          return string;
        } else {
          return "Unknown";
        }
      }
    }
    return "1:" + FORMAT.format(scaleDouble);
  }

  private final Viewport2D viewport;

  public MapScale(final Viewport2D viewport) {
    this.viewport = viewport;
    setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

    setPreferredSize(new Dimension(100, 20));
    viewport.addPropertyChangeListener("scale", this);
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
