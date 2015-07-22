package com.revolsys.gis.cs;

import java.io.Serializable;

import com.revolsys.data.equals.Equals;
import com.revolsys.util.MathUtil;

public class Spheroid implements Serializable {
  /**
   *
   */
  private static final long serialVersionUID = -8349864136575195872L;

  private final Authority authority;

  private final boolean deprecated;

  private final double eccentricity;

  private final double eccentricitySquared;

  private double inverseFlattening;

  private final String name;

  private final double semiMajorAxis;

  private double semiMinorAxis;

  public Spheroid(final String name, final double semiMajorAxis, final double inverseFlattening,
    final Authority authority) {
    this(name, semiMajorAxis, Double.NaN, inverseFlattening, authority, false);
  }

  public Spheroid(final String name, final double semiMajorAxis, final double semiMinorAxis,
    final double inverseFlattening, final Authority authority, final boolean deprecated) {
    this.name = name;
    this.semiMajorAxis = semiMajorAxis;
    this.inverseFlattening = inverseFlattening;
    this.semiMinorAxis = semiMinorAxis;
    this.authority = authority;
    this.deprecated = deprecated;

    if (Double.isNaN(inverseFlattening)) {
      this.inverseFlattening = semiMajorAxis / (semiMajorAxis - this.semiMinorAxis);
    }
    final double f = 1.0 / this.inverseFlattening;

    if (Double.isNaN(semiMinorAxis)) {
      this.semiMinorAxis = semiMajorAxis - semiMajorAxis * f;
    }

    // double a2 = this.semiMajorAxis * this.semiMajorAxis;
    // double b2 = this.semiMinorAxis * this.semiMinorAxis;
    // eccentricitySquared = 1.0 - b2 / a2;

    this.eccentricitySquared = f + f - f * f;
    this.eccentricity = Math.sqrt(this.eccentricitySquared);
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof Spheroid) {
      final Spheroid spheroid = (Spheroid)object;
      if (Double.doubleToLongBits(MathUtil.makePrecise(1000000.0, this.inverseFlattening)) != Double
        .doubleToLongBits(MathUtil.makePrecise(1000000.0, spheroid.inverseFlattening))) {
        return false;
      } else if (Double.doubleToLongBits(this.semiMajorAxis) != Double
        .doubleToLongBits(spheroid.semiMajorAxis)) {
        return false;
      }
      return true;
    } else {
      return false;
    }

  }

  public boolean equalsExact(final Spheroid spheroid) {
    if (!Equals.equal(this.authority, spheroid.authority)) {
      return false;
      // } else if (deprecated != spheroid.deprecated) {
      // return false;
    } else if (this.inverseFlattening != spheroid.inverseFlattening) {
      return false;
      // } else if (!EqualsRegistry.equal(name, spheroid.name)) {
      // return false;
    } else if (this.semiMajorAxis != spheroid.semiMajorAxis) {
      return false;
    } else if (this.semiMinorAxis != spheroid.semiMinorAxis) {
      return false;
    } else {
      return true;
    }
  }

  public Authority getAuthority() {
    return this.authority;
  }

  public double getEccentricity() {
    return this.eccentricity;
  }

  public double getEccentricitySquared() {
    return this.eccentricitySquared;
  }

  public double getInverseFlattening() {
    return this.inverseFlattening;
  }

  public String getName() {
    return this.name;
  }

  public double getSemiMajorAxis() {
    return this.semiMajorAxis;
  }

  public double getSemiMinorAxis() {
    return this.semiMinorAxis;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(MathUtil.makePrecise(1000000.0, this.inverseFlattening));
    result = prime * result + (int)(temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(this.semiMajorAxis);
    result = prime * result + (int)(temp ^ temp >>> 32);
    return result;
  }

  public boolean isDeprecated() {
    return this.deprecated;
  }

  public double meridianRadiusOfCurvature(final double latitude) {
    final double er = 1.0 - this.eccentricitySquared * Math.sin(latitude) * Math.sin(latitude);
    final double el = Math.pow(er, 1.5);
    final double m0 = this.semiMajorAxis * (1.0 - this.eccentricitySquared) / el;
    return m0;
  }

  public double primeVerticalRadiusOfCurvature(final double latitude) {
    final double t1 = this.semiMajorAxis * this.semiMajorAxis;
    final double t2 = t1 * Math.cos(latitude) * Math.cos(latitude);
    final double t3 = this.semiMinorAxis * this.semiMinorAxis * Math.sin(latitude)
      * Math.sin(latitude);
    final double n0 = t1 / Math.sqrt(t2 + t3);
    return n0;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
