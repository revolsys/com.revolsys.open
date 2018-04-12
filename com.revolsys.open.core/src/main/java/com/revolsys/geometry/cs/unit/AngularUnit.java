package com.revolsys.geometry.cs.unit;

import static tec.uom.se.AbstractUnit.ONE;

import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Angle;

import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.cs.Authority;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;
import com.revolsys.util.Md5;

import si.uom.NonSI;
import tec.uom.se.unit.AlternateUnit;
import tec.uom.se.unit.Units;

public class AngularUnit implements UnitOfMeasure {

  private static final Map<String, Unit<Angle>> UNIT_BY_NAME = Maps
    .<String, Unit<Angle>> buildHash()
    .add("radian", Units.RADIAN)
    .add("degree", NonSI.DEGREE_ANGLE)
    .add("degree minute", NonSI.DEGREE_ANGLE)
    .add("degree minute second", NonSI.DEGREE_ANGLE)
    .add("degree minute second hemisphere", NonSI.DEGREE_ANGLE)
    .add("degree hemisphere", NonSI.DEGREE_ANGLE)
    .add("degree minute hemisphere", NonSI.DEGREE_ANGLE)
    .add("degree (supplier to define representation)", NonSI.DEGREE_ANGLE)
    .add("hemisphere degree", NonSI.DEGREE_ANGLE)
    .add("hemisphere degree minute", NonSI.DEGREE_ANGLE)
    .add("hemisphere degree minute second", NonSI.DEGREE_ANGLE)
    .add("sexagesimal dms.s", NonSI.DEGREE_ANGLE)
    .add("sexagesimal dms", NonSI.DEGREE_ANGLE)
    .add("sexagesimal dm", NonSI.DEGREE_ANGLE)
    .add("grad", CustomUnits.GRAD)
    .getMap();

  private final Authority authority;

  private final AngularUnit baseUnit;

  private final double conversionFactor;

  private final boolean deprecated;

  public CoordinatesOperation fromRadiansOperation = this::fromRadians;

  private String name;

  private Unit<Angle> unit;

  public AngularUnit(final String name, final AngularUnit baseUnit, final double conversionFactor,
    final Authority authority, final boolean deprecated) {
    this.name = name;
    if (name.equals("degree (supplier to define representation)")) {
      this.name = "degree";
    }
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

  public AngularUnit(final String name, final double conversionFactor, final Authority authority) {
    this(name, null, conversionFactor, authority, false);
  }

  public void addConversionOperation(final List<CoordinatesOperation> operations,
    final AngularUnit targetAngularUnit) {
    if (this != targetAngularUnit) {
      if (targetAngularUnit instanceof Radian) {
        addToRadiansOperation(operations);
      } else if (targetAngularUnit instanceof Degree) {
        addToDegreesOperation(operations);
      } else {
        operations.add(point -> {
          final double x = toRadians(point.x);
          point.x = targetAngularUnit.fromRadians(x);
          final double y = toRadians(point.y);
          point.y = targetAngularUnit.fromRadians(y);
        });
      }
    }
  }

  public void addFromDegreesOperation(final List<CoordinatesOperation> operations) {
    operations.add(this::fromDegrees);
  }

  public void addFromRadiansOperation(final List<CoordinatesOperation> operations) {
    operations.add(this::fromRadians);
  }

  public void addToDegreesOperation(final List<CoordinatesOperation> operations) {
    operations.add(this::toDegrees);
  }

  public void addToRadiansOperation(final List<CoordinatesOperation> operations) {
    operations.add(this::toRadians);
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof AngularUnit) {
      final AngularUnit unit = (AngularUnit)object;
      if (!this.name.equals(unit.name)) {
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

  protected void fromDegrees(final CoordinatesOperationPoint point) {
    point.x = fromDegrees(point.x);
    point.y = fromDegrees(point.y);
  }

  public double fromDegrees(final double value) {
    final double radians = toRadians(value);
    return Math.toDegrees(radians);
  }

  protected void fromRadians(final CoordinatesOperationPoint point) {
    point.x = fromRadians(point.x);
    point.y = fromRadians(point.y);
  }

  public double fromRadians(final double value) {
    final double baseValue;
    if (Double.isFinite(this.conversionFactor)) {
      baseValue = value / this.conversionFactor;
    } else {
      baseValue = value;
    }
    if (this.baseUnit == null) {
      return baseValue;
    } else {
      return this.baseUnit.fromRadians(baseValue);
    }
  }

  public Authority getAuthority() {
    return this.authority;
  }

  public AngularUnit getBaseUnit() {
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
    return UnitOfMeasureType.ANGULAR;
  }

  public Unit<Angle> getUnit() {
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

  protected void toDegrees(final CoordinatesOperationPoint point) {
    point.x = toDegrees(point.x);
    point.y = toDegrees(point.y);
  }

  public double toDegrees(final double value) {
    final double baseValue;
    if (Double.isFinite(this.conversionFactor)) {
      baseValue = value * this.conversionFactor;
    } else {
      baseValue = value;
    }
    if (this.baseUnit == null) {
      return Math.toDegrees(baseValue);
    } else {
      return this.baseUnit.toDegrees(baseValue);
    }
  }

  /**
   * Same as toDegrees
   */
  @Override
  public double toNormal(final double value) {
    final double baseValue;
    if (Double.isFinite(this.conversionFactor)) {
      baseValue = value * this.conversionFactor;
    } else {
      baseValue = value;
    }
    if (this.baseUnit == null) {
      return Math.toDegrees(baseValue);
    } else {
      return this.baseUnit.toDegrees(baseValue);
    }
  }

  protected void toRadians(final CoordinatesOperationPoint point) {
    point.x = toRadians(point.x);
    point.y = toRadians(point.y);
  }

  public double toRadians(final double value) {
    final double baseValue;
    if (Double.isFinite(this.conversionFactor)) {
      baseValue = value * this.conversionFactor;
    } else {
      baseValue = value;
    }
    if (this.baseUnit == null) {
      return baseValue;
    } else {
      return this.baseUnit.toRadians(baseValue);
    }
  }

  @Override
  public String toString() {
    return this.name;
  }

  public void updateDigest(final MessageDigest digest) {
    digest.update((byte)'A');
    Md5.update(digest, Math.round(toBase(1) * 1e6) / 1e6);
  }
}
