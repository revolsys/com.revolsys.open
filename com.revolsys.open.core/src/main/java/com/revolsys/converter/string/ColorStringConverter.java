package com.revolsys.converter.string;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.awt.WebColors;
import com.revolsys.util.ExceptionUtil;

public class ColorStringConverter implements StringConverter<Color> {

  private static int fromHex(final String string, final int start,
    final int end, final int defaultValue) {
    if (end <= string.length()) {
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
    } else if (length == 7) {
      red = fromHex(colorString, 1, 3, 0);
      green = fromHex(colorString, 4, 5, red);
      blue = fromHex(colorString, 5, 7, green);
      opacity = 255;
    } else if (length == 9) {
      red = fromHex(colorString, 1, 3, 0);
      green = fromHex(colorString, 4, 5, red);
      blue = fromHex(colorString, 5, 7, green);
      opacity = fromHex(colorString, 7, 9, 255);
    }
    return new Color(red, green, blue, opacity);
  }

  public static Color getRgbaColor(final String string) {
    try {
      final String[] values = string.replaceAll("[^0-9,.]", "").split(",");
      final int red = Integer.valueOf(values[0]);
      final int green = Integer.valueOf(values[1]);
      final int blue = Integer.valueOf(values[2]);
      final int alpha = (int)(Double.valueOf(values[3]) * 255);
      final Color color = new Color(red, green, blue, alpha);
      return color;
    } catch (final Throwable e) {
      LoggerFactory.getLogger(WebColors.class).error(
        "Not a valid rgba color " + string, e);
      return Color.BLACK;
    }
  }

  public static Color getRgbColor(final String string) {
    try {
      final String[] values = string.replaceAll("[^0-9,]", "").split(",");
      final int red = Integer.valueOf(values[0]);
      final int green = Integer.valueOf(values[1]);
      final int blue = Integer.valueOf(values[2]);
      final Color color = new Color(red, green, blue, 255);
      return color;
    } catch (final Throwable e) {
      LoggerFactory.getLogger(WebColors.class).error(
        "Not a valid rgb color " + string, e);
      return Color.BLACK;
    }
  }

  public static Color getWebColor(final String colorName) {
    if (StringUtils.hasText(colorName)) {
      for (final Field field : WebColors.class.getFields()) {
        final String fieldName = field.getName();
        if (Modifier.isStatic(field.getModifiers())
          && Modifier.isPublic(field.getModifiers())) {
          if (fieldName.equalsIgnoreCase(colorName)) {
            try {
              return (Color)field.get(WebColors.class);
            } catch (final Throwable e) {
              ExceptionUtil.throwUncheckedException(e);
            }
          }
        }
      }
    }
    return Color.BLACK;
  }

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
        return getColor(string);
      } else if (string.startsWith("rgb(")) {
        return getRgbColor(string);
      } else if (string.startsWith("rgba(")) {
        return getRgbaColor(string);
      } else {
        final Color color = getWebColor(string);
        if (color == null) {
          LoggerFactory.getLogger(getClass()).error(
            "Not a valid color " + string);
          return Color.BLACK;
        } else {
          return color;
        }
      }
    }
    return Color.BLACK;
  }

  @Override
  public String toString(final Object value) {
    if (value instanceof Color) {
      final Color color = (Color)value;
      final String colorName = WebColors.getName(color);
      if (StringUtils.hasText(colorName)) {
        return colorName;
      } else {
        final int alpha = color.getAlpha();
        if (alpha == 255) {
          return "rgb(" + color.getRed() + "," + color.getGreen() + ","
            + color.getBlue() + ")";
        } else {
          return "rgba(" + color.getRed() + "," + color.getGreen() + ","
            + color.getBlue() + "," + alpha / 255.0 + ")";
        }
      }
    } else {
      return null;
    }
  }
}
