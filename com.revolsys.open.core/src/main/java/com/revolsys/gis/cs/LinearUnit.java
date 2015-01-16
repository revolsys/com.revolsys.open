package com.revolsys.gis.cs;

import java.io.Serializable;

import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

public class LinearUnit implements Serializable {
  /**
   * Get the linear unit representing the conversion factor from
   * {@link SI#METER}.
   *
   * @param conversionFactor The conversion factor.
   * @return The linear unit.
   */
  public static Unit<Length> getUnit(final double conversionFactor) {
    return getUnit(null, conversionFactor);
  }

  /**
   * Get the linear unit representing the conversion factor from the specified
   * base linear unit.
   *
   * @param baseUnit The base unit.
   * @param conversionFactor The conversion factor.
   * @return The linear unit.
   */
  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public static Unit<Length> getUnit(final Unit<Length> baseUnit,
    final double conversionFactor) {
    Unit<Length> unit;
    if (baseUnit == null) {
      unit = SI.METRE;
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

  /**
   *
   */
  private static final long serialVersionUID = 4000991484199279234L;

  private final Authority authority;

  private final LinearUnit baseUnit;

  private final double conversionFactor;

  private final boolean deprecated;

  private final String name;

  private Unit<Length> unit;

  public LinearUnit(final String name, final double conversionFactor,
    final Authority authority) {
    this(name, null, conversionFactor, authority, false);
  }

  public LinearUnit(final String name, final LinearUnit baseUnit,
    final double conversionFactor, final Authority authority) {
    this(name, baseUnit, conversionFactor, authority, false);
  }

  public LinearUnit(final String name, final LinearUnit baseUnit,
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

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof LinearUnit) {
      final LinearUnit unit = (LinearUnit)object;
      if (this.name == null && unit.name != null || !this.name.equals(unit.name)) {
        return false;
      } else if (Math.abs(this.conversionFactor - unit.conversionFactor) > 1.0e-10) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public Authority getAuthority() {
    return this.authority;
  }

  public LinearUnit getBaseUnit() {
    return this.baseUnit;
  }

  public double getConversionFactor() {
    return this.conversionFactor;
  }

  public String getName() {
    return this.name;
  }

  public Unit<Length> getUnit() {
    return this.unit;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.name.hashCode();
    final long temp = Double.doubleToLongBits(this.conversionFactor);
    result = prime * result + (int)(temp ^ temp >>> 32);
    return result;
  }

  public boolean isDeprecated() {
    return this.deprecated;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
