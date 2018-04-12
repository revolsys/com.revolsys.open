package com.revolsys.geometry.cs.unit;

import static tec.uom.se.AbstractUnit.ONE;

import java.util.List;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.cs.Authority;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;
import com.revolsys.util.number.Doubles;

import systems.uom.common.USCustomary;
import tec.uom.se.unit.AlternateUnit;
import tec.uom.se.unit.Units;

public class LinearUnit implements UnitOfMeasure {

  private static final Map<String, Unit<Length>> UNIT_BY_NAME = Maps
    .<String, Unit<Length>> buildHash()
    .add("metre", Units.METRE)
    .add("meter", Units.METRE)
    .add("millimetre", CustomUnits.MILLIMETRE)
    .add("centimetre", CustomUnits.CENTIMETRE)
    .add("kilometre", CustomUnits.KILOMETRE)
    .add("foot", USCustomary.FOOT)
    .add("foot", USCustomary.FOOT)
    .add("yard", USCustomary.YARD)
    .add("us survey foot", USCustomary.FOOT_SURVEY)
    .add("foot_us", USCustomary.FOOT_SURVEY)
    .add("link_clarke", CustomUnits.CLARKES_LINK)
    .add("foot_clarke", CustomUnits.CLARKES_FOOT)
    .add("chain_clarke", CustomUnits.CLARKES_CHAIN)
    .add("yard_clarke", CustomUnits.CLARKES_YARD)
    .add("foot_sears", CustomUnits.SEARS_FOOT)
    .add("chain_sears", CustomUnits.SEARS_CHAIN)
    .add("yard_sears", CustomUnits.SEARS_YARD)
    .add("chain_sears_1922_truncated", CustomUnits.SEARS_CHAIN_TRUNCATED)
    .add("yard_indian", CustomUnits.INDIAN_YARD)
    .add("chain_benoit_1895_b", CustomUnits.BRITISH_CHAIN_BENOIT_1895_B)
    .add("yard_indian_1937", CustomUnits.INDIAN_YARD_1937)
    .add("foot_british_1936", CustomUnits.BRITISH_FOOT_1936)
    .add("foot_gold_coast", CustomUnits.GOLD_COAST_FOOT)
    .add("50_kilometers", Units.METRE.multiply(50000))
    .add("150_kilometers", Units.METRE.multiply(150000))
    .getMap();

  private final Authority authority;

  private final LinearUnit baseUnit;

  private final double conversionFactor;

  private final boolean deprecated;

  private final String name;

  private Unit<Length> unit;

  public CoordinatesOperation fromMetresOperation = this::fromMetres;

  public CoordinatesOperation toMetresOperation = this::toMetres;

  public LinearUnit(final String name, final double conversionFactor) {
    this(name, null, conversionFactor, null, false);
  }

  public LinearUnit(final String name, final double conversionFactor, final Authority authority) {
    this(name, null, conversionFactor, authority, false);
  }

  public LinearUnit(final String name, final LinearUnit baseUnit, final double conversionFactor,
    final Authority authority, final boolean deprecated) {
    this.name = name;
    this.baseUnit = baseUnit;
    this.conversionFactor = conversionFactor;
    this.authority = authority;
    this.deprecated = deprecated;
    this.unit = UNIT_BY_NAME.get(name.toLowerCase());
    if (this.unit == null) {
      if (baseUnit == null) {
        if (conversionFactor == 1) {
          this.unit = new AlternateUnit<>(ONE, name);
        } else {
          System.err.println("Invalid conversion factor for " + name);
        }
      } else if (Double.isFinite(conversionFactor)) {
        this.unit = baseUnit.getUnit().multiply(conversionFactor);
      } else {
        this.unit = baseUnit.getUnit();
      }
    }
  }

  public void addFromMetresOperation(final List<CoordinatesOperation> operations) {
    operations.add(this.fromMetresOperation);
  }

  public void addToMetresOperation(final List<CoordinatesOperation> operations) {
    operations.add(this.toMetresOperation);
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

  public void fromMetres(final CoordinatesOperationPoint point) {
    point.x = fromMetres(point.x);
    point.y = fromMetres(point.y);
  }

  public double fromMetres(final double value) {
    final double baseValue;
    if (Double.isFinite(this.conversionFactor)) {
      baseValue = value / this.conversionFactor;
    } else {
      baseValue = value;
    }
    if (this.baseUnit == null) {
      return baseValue;
    } else {
      return this.baseUnit.fromMetres(baseValue);
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

  @Override
  public UnitOfMeasureType getType() {
    return UnitOfMeasureType.LINEAR;
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
  public double toBase(final double value) {
    final double baseValue;
    if (Double.isFinite(this.conversionFactor)) {
      baseValue = value * this.conversionFactor;
    } else {
      baseValue = value;
    }
    if (this.baseUnit == null) {
      return baseValue;
    } else {
      return this.baseUnit.toBase(baseValue);
    }
  }

  public void toMetres(final CoordinatesOperationPoint point) {
    point.x = toMetres(point.x);
    point.y = toMetres(point.y);
  }

  public double toMetres(final double value) {
    final double baseValue;
    if (Double.isFinite(this.conversionFactor)) {
      baseValue = value * this.conversionFactor;
    } else {
      baseValue = value;
    }
    if (this.baseUnit == null) {
      return baseValue;
    } else {
      return this.baseUnit.toMetres(baseValue);
    }
  }

  @Override
  public double toNormal(final double value) {
    final double baseValue;
    if (Double.isFinite(this.conversionFactor)) {
      baseValue = value * this.conversionFactor;
    } else {
      baseValue = value;
    }
    if (this.baseUnit == null) {
      return Doubles.makePrecise(1.0e12, baseValue);
    } else {
      return this.baseUnit.toBase(baseValue);
    }
  }

  @Override
  public String toString() {
    return this.name;
  }
}
