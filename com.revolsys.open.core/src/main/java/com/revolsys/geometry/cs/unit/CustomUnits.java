package com.revolsys.geometry.cs.unit;

import static tec.uom.se.unit.Units.METRE;
import static tec.uom.se.unit.Units.RADIAN;

import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import tec.uom.se.AbstractSystemOfUnits;
import tec.uom.se.AbstractUnit;
import tec.uom.se.format.SimpleUnitFormat;
import tec.uom.se.function.PiMultiplierConverter;
import tec.uom.se.function.RationalConverter;
import tec.uom.se.unit.TransformedUnit;
import tec.uom.se.unit.Units;

public class CustomUnits extends AbstractSystemOfUnits {

  private static final CustomUnits _INSTANCE = new CustomUnits();

  public static final Unit<Length> CENTIMETRE = addUnit(Units.METRE.divide(100), "cm", "cm");

  public static final Unit<Length> CLARKES_CHAIN = addUnit(Units.METRE.multiply(20.1166195164),
    "Clarke's chain", "chain");

  public static final Unit<Length> BRITISH_FOOT_1936 = addUnit(Units.METRE.multiply(0.3048007491),
    "British foot (1936)", "ft");

  public static final Unit<Length> CLARKES_FOOT = addUnit(Units.METRE.multiply(0.3047972654),
    "Clarke's foot", "ft");

  public static final Unit<Length> CLARKES_LINK = addUnit(Units.METRE.multiply(0.201166195164),
    "Clarke's link", "link");

  public static final Unit<Length> CLARKES_YARD = addUnit(Units.METRE.multiply(0.9143917962),
    "Clarke's yard", "yd");

  public static final Unit<Length> SEARS_CHAIN = addUnit(
    Units.METRE.multiply(792).divide(39.370147), "British chain (Sears 1922)", "chain");

  public static final Unit<Length> SEARS_CHAIN_TRUNCATED = addUnit(
    Units.METRE.multiply(792).divide(39.370147), "British chain (Sears 1922)", "chain");

  public static final Unit<Length> SEARS_FOOT = addUnit(Units.METRE.multiply(12).divide(39.370147),
    "British chain (Sears 1922 truncated)", "ft");

  public static final Unit<Length> SEARS_YARD = addUnit(Units.METRE.multiply(36).divide(39.370147),
    "British yard (Sears 1922)", "yd");

  public static final Unit<Length> GOLD_COAST_FOOT = addUnit(
    Units.METRE.multiply(6378300).divide(20926201), "Gold Coast foot", "ft");

  public static final Unit<Angle> GRAD = addUnit(new TransformedUnit<>(RADIAN,
    new PiMultiplierConverter().concatenate(new RationalConverter(1, 200))), "Grad", "grad");

  public static final Unit<Length> INDIAN_YARD = addUnit(Units.METRE.multiply(36).divide(39.370142),
    "Indian yard", "yd");

  public static final Unit<Length> INDIAN_YARD_1937 = addUnit(Units.METRE.multiply(0.91439523),
    "Indian yard (1937)", "yd");

  public static final Unit<Length> KILOMETRE = addUnit(Units.METRE.multiply(1000), "km", "km");

  public static final Unit<Length> MILLIMETRE = addUnit(Units.METRE.divide(1000), "mm", "mm");

  public static final Unit<Length> PIXEL = addUnit(METRE.multiply(0.0002636), "Pixel", "pnt");

  public static final Unit<Length> BRITISH_CHAIN_BENOIT_1895_B = addUnit(
    METRE.multiply(792).divide(39.370113), "British chain (Benoit 1895 B)", "chain");

  private static <U extends Unit<?>> U addUnit(final U unit, final String name, final String text) {
    SimpleUnitFormat.getInstance().label(unit, text);
    if (name != null && unit instanceof AbstractUnit) {
      return Helper.addUnit(_INSTANCE.units, unit, name);
    } else {
      _INSTANCE.units.add(unit);
    }
    return unit;
  }

  public static CustomUnits getInstance() {
    return _INSTANCE;
  }

  private CustomUnits() {
  }

  @Override
  public String getName() {
    return "rs-custom";
  }

}
