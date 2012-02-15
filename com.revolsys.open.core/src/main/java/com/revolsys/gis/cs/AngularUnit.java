package com.revolsys.gis.cs;

import java.io.Serializable;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

public class AngularUnit implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -3508138430785747634L;

  /**
   * Get the angular unit representing the conversion factor from
   * {@link SI#RADIAN}.
   * 
   * @param conversionFactor The conversion factor.
   * @return The angular unit.
   */
  public static Unit<Angle> getUnit(final double conversionFactor) {
    return getUnit(null, conversionFactor);
  }

  /**
   * Get the angular unit representing the conversion factor from the specified
   * base angular unit.
   * 
   * @param baseUnit The base unit.
   * @param conversionFactor The conversion factor.
   * @return The angular unit.
   */
  public static Unit<Angle> getUnit(
    final Unit<Angle> baseUnit,
    final double conversionFactor) {
    Unit<Angle> unit;
    if (baseUnit == null) {
      unit = SI.RADIAN;
    } else {
      unit = baseUnit;
    }
    if (conversionFactor != 1) {
      unit = unit.times(conversionFactor);
      // Normalize the unit
      for (final Unit siUnit : SI.getInstance().getUnits()) {
        if (siUnit.equals(unit)) {
          return siUnit;
        }
      }
      for (final Unit nonSiUnit : NonSI.getInstance().getUnits()) {
        if (nonSiUnit.equals(unit)) {
          return nonSiUnit;
        }
      }
    }
    return unit;

  }

  private final Authority authority;

  private final AngularUnit baseUnit;

  private final double conversionFactor;

  private final boolean deprecated;

  private final String name;

  private Unit<Angle> unit;

  public AngularUnit(final String name, final AngularUnit baseUnit,
    final double conversionFactor, final Authority authority) {
    this(name, baseUnit, conversionFactor, authority, false);
  }

  public AngularUnit(final String name, final AngularUnit baseUnit,
    final double conversionFactor, final Authority authority,
    final boolean deprecated) {
    this.name = name;
    this.baseUnit = baseUnit;
    this.conversionFactor = conversionFactor;
    this.authority = authority;
    this.deprecated = deprecated;
    if (baseUnit == null) {
      this.unit = getUnit(conversionFactor);
    } else {
      this.unit = getUnit(baseUnit.getUnit(), conversionFactor);
    }
  }

  public AngularUnit(final String name, final double conversionFactor,
    final Authority authority) {
    this(name, null, conversionFactor, authority, false);
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof AngularUnit) {
      final AngularUnit unit = (AngularUnit)object;
      if (name == null && unit.name != null || !name.equals(unit.name)) {
        return false;
      } else if (Math.abs(conversionFactor - unit.conversionFactor) > 1.0e-10) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public Authority getAuthority() {
    return authority;
  }

  public AngularUnit getBaseUnit() {
    return baseUnit;
  }

  public double getConversionFactor() {
    return conversionFactor;
  }

  public String getName() {
    return name;
  }

  public Unit<Angle> getUnit() {
    return unit;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + name.hashCode();
    final long temp = Double.doubleToLongBits(conversionFactor);
    result = prime * result + (int)(temp ^ (temp >>> 32));
    return result;
  }

  public boolean isDeprecated() {
    return deprecated;
  }

  @Override
  public String toString() {
    return name;
  }
}
