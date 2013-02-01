package com.revolsys.converter.string;

import java.awt.Color;

import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class ColorStringConverter implements StringConverter<Color> {

  @Override
  public Class<Color> getConvertedClass() {
    return Color.class;
  }

  @Override
  public boolean requiresQuotes() {
    return false;
  }

  @Override
  public Color toObject(final Object value) {
    if (value instanceof Color) {
      final Color color = (Color)value;
      return color;
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  @Override
  public Color toObject(final String string) {
    if (StringUtils.hasText(string)) {
      if (string.startsWith("#")) {
        // TODO hex
        return null;
      } else if (string.startsWith("rgb(")) {
        try {
          String[] values = string.replaceAll("[^0-9,]", "").split(",");
          int red = Integer.valueOf(values[0]);
          int green = Integer.valueOf(values[1]);
          int blue = Integer.valueOf(values[2]);
          Color color = new Color(red, green, blue, 255);
          return color;
        } catch (Throwable e) {
          LoggerFactory.getLogger(getClass()).error(
            "Not a valid CSS color " + string, e);
        }
      } else if (string.startsWith("rgba(")) {
        try {
          String[] values = string.replaceAll("[^0-9,.]", "").split(",");
          int red = Integer.valueOf(values[0]);
          int green = Integer.valueOf(values[1]);
          int blue = Integer.valueOf(values[2]);
          int alpha = (int)(Double.valueOf(values[3]) * 255);
          Color color = new Color(red, green, blue, alpha);
          return color;
        } catch (Throwable e) {
          LoggerFactory.getLogger(getClass()).error(
            "Not a valid CSS color " + string, e);
        }
      } else {
        LoggerFactory.getLogger(getClass()).error(
          "Not a valid CSS color " + string);
      }
    }
    return null;
  }

  @Override
  public String toString(Object value) {
    if (value instanceof Color) {
      Color color = (Color)value;
      // TODO Auto-generated method stub
      return "rgba(" + color.getRed() + "," + color.getGreen() + ","
        + color.getBlue() + "," + color.getAlpha() / 255.0 + ")";
    } else {
      return null;
    }
  }
}
