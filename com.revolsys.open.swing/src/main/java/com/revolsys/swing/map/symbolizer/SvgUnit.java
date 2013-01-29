package com.revolsys.swing.map.symbolizer;

import java.util.HashMap;
import java.util.Map;

public class SvgUnit {

  private static final Map<String, SvgUnit> NAME_UNIT_MAP = new HashMap<String, SvgUnit>();

  public static final SvgUnit PERCENT = new SvgUnit("%");

  public static final SvgUnit POINT = new SvgUnit("pt", 1.25);

  public static final SvgUnit PC = new SvgUnit("pc", 15);

  public static final SvgUnit MILLIMETRE = new SvgUnit("mm", 3.543307);

  public static final SvgUnit CENTIMETRE = new SvgUnit("cm", 35.43307);

  public static final SvgUnit INCH = new SvgUnit("in", 90);

  public static SvgUnit getUnit(final String name) {
    return NAME_UNIT_MAP.get(name);
  }

  private final String name;

  private double scaleFactor;

  private boolean proportional;

  public SvgUnit(final String name) {
    this.name = name;
    this.proportional = true;
    if (NAME_UNIT_MAP.containsKey(name)) {
      NAME_UNIT_MAP.put(name, this);
    }
  }

  public SvgUnit(final String name, final double scaleFactor) {
    this.name = name;
    this.scaleFactor = scaleFactor;
    if (NAME_UNIT_MAP.containsKey(name)) {
      NAME_UNIT_MAP.put(name, this);
    }
  }

  public double toModelUnits(final double value) {
    return value * scaleFactor;
  }

  public double toModelUnits(
    final double width,
    final double height,
    final double value) {
    if (proportional) {
      final double factor = Math.sqrt(width * width + height * height)
        / Math.sqrt(2);
      return value / 100 * factor;
    } else {
      return toModelUnits(value);
    }
  }
}
