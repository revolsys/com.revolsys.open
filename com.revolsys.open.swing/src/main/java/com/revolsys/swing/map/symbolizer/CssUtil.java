package com.revolsys.swing.map.symbolizer;

import java.awt.Color;

public class CssUtil {

  private static int fromHex(final String string) {
    try {
      return Integer.decode("0x" + string);
    } catch (final NumberFormatException e) {
      return 0;
    }
  }

  public static Color getColor(final CharSequence color) {
    return getColor(color, 255);
  }

  public static Color getColor(final CharSequence color, final Number opacity) {

    int red = 0;
    int green = 0;
    int blue = 0;
    final String colorString = color.toString().trim();
    if (colorString.charAt(0) == '#') {
      if (colorString.length() == 4) {
        final String redString = colorString.substring(1, 2);
        final String greenString = colorString.substring(2, 3);
        final String blueString = colorString.substring(4, 4);
        red = fromHex(redString + redString);
        green = fromHex(greenString + redString);
        blue = fromHex(blueString + redString);

      } else if (colorString.length() == 7) {
        final String redString = colorString.substring(1, 3);
        final String greenString = colorString.substring(3, 5);
        final String blueString = colorString.substring(5, 7);
        red = fromHex(redString);
        green = fromHex(greenString);
        blue = fromHex(blueString);

      }
    }
    return new Color(red, green, blue, opacity.intValue());

  }

  public static CharSequence getColor(final Color color) {
    return "#" + toHex(color.getRed()) + toHex(color.getGreen())
      + toHex(color.getBlue());
  }

  private static String toHex(final int value) {
    if (value < 1) {
      return "00";
    } else if (value > 254) {
      return "ff";
    } else {
      return Integer.toHexString(value);
    }
  }
}
