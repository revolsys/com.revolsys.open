package com.revolsys.converter.string;

import java.awt.Color;

import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class ColorStringConverter implements StringConverter<Color> {

  private static int fromHex(final String string, final int start,
    final int end, final int defaultValue) {
    if (string.length() < end) {
      try {
        String text = string.substring(start, end);
        if (text.length() == 1) {
          text += text;
        }
        return Integer.decode("0x" + text);
      } catch (final NumberFormatException e) {

      }
    }
    return defaultValue;
  }

  public static Color getColor(final CharSequence color) {
    int red = 0;
    int green = 0;
    int blue = 0;
    int opacity = 255;
    final String colorString = color.toString().trim();
    final int length = colorString.length();
    if (length > 1 && length < 6) {
      red = fromHex(colorString, 1, 2, 0);
      green = fromHex(colorString, 2, 3, red);
      blue = fromHex(colorString, 3, 4, green);
      opacity = fromHex(colorString, 4, 5, 255);
    } else if (length < 10) {
      red = fromHex(colorString, 1, 3, 0);
      green = fromHex(colorString, 4, 5, red);
      blue = fromHex(colorString, 5, 7, green);
      opacity = fromHex(colorString, 7, 9, 255);
    }
    return new Color(red, green, blue, opacity);
  }

  @Override
  public Class<Color> getConvertedClass() {
    return Color.class;
  }

  private Color getRgbaColor(final String string) {
    try {
      final String[] values = string.replaceAll("[^0-9,.]", "").split(",");
      final int red = Integer.valueOf(values[0]);
      final int green = Integer.valueOf(values[1]);
      final int blue = Integer.valueOf(values[2]);
      final int alpha = (int)(Double.valueOf(values[3]) * 255);
      final Color color = new Color(red, green, blue, alpha);
      return color;
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error(
        "Not a valid CSS color " + string, e);
      return null;
    }
  }

  private Color getRgbColor(final String string) {
    try {
      final String[] values = string.replaceAll("[^0-9,]", "").split(",");
      final int red = Integer.valueOf(values[0]);
      final int green = Integer.valueOf(values[1]);
      final int blue = Integer.valueOf(values[2]);
      final Color color = new Color(red, green, blue, 255);
      return color;
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error(
        "Not a valid CSS color " + string, e);
      return null;
    }
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
        return getRgbColor(string);
      } else if (string.startsWith("rgba(")) {
        return getRgbaColor(string);
      } else {
        LoggerFactory.getLogger(getClass()).error(
          "Not a valid CSS color " + string);
      }
    }
    return null;
  }

  @Override
  public String toString(final Object value) {
    if (value instanceof Color) {
      final Color color = (Color)value;
      // TODO Auto-generated method stub
      return "rgba(" + color.getRed() + "," + color.getGreen() + ","
        + color.getBlue() + "," + color.getAlpha() / 255.0 + ")";
    } else {
      return null;
    }
  }
}
