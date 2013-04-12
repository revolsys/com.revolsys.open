package com.revolsys.gis.cs.epsg;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class EpsgUtil {

  private static NumberFormat FORMAT = new DecimalFormat(
    "#0.00000##########################");

  public static double toDecimalFromSexagesimalDegrees(final double sexagesimal) {
    final String string = FORMAT.format(sexagesimal);
    final int dotIndex = string.indexOf('.');

    final int degrees = Integer.parseInt(string.substring(0, dotIndex));
    final int minutes = Integer.parseInt(string.substring(dotIndex + 1,
      dotIndex + 3));
    final double seconds = Double.parseDouble(string.substring(dotIndex + 3,
      dotIndex + 5) + "." + string.substring(dotIndex + 5));
    double decimal;
    if (sexagesimal < 0) {
      decimal = degrees - minutes / 60.0 - seconds / 3600.0;
    } else {
      decimal = degrees + minutes / 60.0 + seconds / 3600.0;
    }
    return decimal;
  }

}
