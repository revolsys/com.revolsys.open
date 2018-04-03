package com.revolsys.geometry.cs.unit;

import static tec.uom.se.unit.Units.METRE;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import tec.uom.se.AbstractSystemOfUnits;
import tec.uom.se.AbstractUnit;
import tec.uom.se.format.SimpleUnitFormat;
import tec.uom.se.unit.Units;

public class CustomUnits extends AbstractSystemOfUnits {
  private static final CustomUnits INSTANCE = new CustomUnits();

  public static final Unit<Length> PIXEL = addUnit(METRE.multiply(0.0002636), "Pixel", "pnt");

  public static final Unit<Length> KILOMETRE = addUnit(Units.METRE.multiply(1000), "km", "km");

  public static final Unit<Length> CENTIMETRE = addUnit(Units.METRE.divide(100), "cm", "cm");

  public static final Unit<Length> MILLIMETRE = addUnit(Units.METRE.divide(1000), "mm", "mm");

  private static <U extends Unit<?>> U addUnit(final U unit, final String name, final String text) {
    SimpleUnitFormat.getInstance().label(unit, text);
    if (name != null && unit instanceof AbstractUnit) {
      return Helper.addUnit(INSTANCE.units, unit, name);
    } else {
      INSTANCE.units.add(unit);
    }
    return unit;
  }

  public static CustomUnits getInstance() {
    return INSTANCE;
  }

  private CustomUnits() {
  }

  @Override
  public String getName() {
    return "rs-custom";
  }

}
