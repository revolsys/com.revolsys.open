package com.revolsys.gis.cs;

import java.io.Serializable;

import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.jts.geom.PrecisionModel;

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

  public Spheroid(final String name, final double semiMajorAxis,
    final double inverseFlattening, final Authority authority) {
    this(name, semiMajorAxis, Double.NaN, inverseFlattening, authority, false);
  }

  public Spheroid(final String name, final double semiMajorAxis,
    final double semiMinorAxis, final double inverseFlattening,
    final Authority authority, final boolean deprecated) {
    this.name = name;
    this.semiMajorAxis = semiMajorAxis;
    this.inverseFlattening = inverseFlattening;
    this.semiMinorAxis = semiMinorAxis;
    this.authority = authority;
    this.deprecated = deprecated;

    if (Double.isNaN(inverseFlattening)) {
      this.inverseFlattening = semiMajorAxis
        / (semiMajorAxis - this.semiMinorAxis);
    }
    final double f = 1.0 / this.inverseFlattening;

    if (Double.isNaN(semiMinorAxis)) {
      this.semiMinorAxis = semiMajorAxis - semiMajorAxis * f;
    }

    // double a2 = this.semiMajorAxis * this.semiMajorAxis;
    // double b2 = this.semiMinorAxis * this.semiMinorAxis;
    // eccentricitySquared = 1.0 - b2 / a2;

    eccentricitySquared = f + f - f * f;
    eccentricity = Math.sqrt(eccentricitySquared);
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof Spheroid) {
      final Spheroid spheroid = (Spheroid)object;
      final PrecisionModel precision = new PrecisionModel(1000000);
      if (Double.doubleToLongBits(precision.makePrecise(inverseFlattening)) != Double.doubleToLongBits(precision.makePrecise(spheroid.inverseFlattening))) {
        return false;
      } else if (Double.doubleToLongBits(semiMajorAxis) != Double.doubleToLongBits(spheroid.semiMajorAxis)) {
        return false;
      }
      return true;
    } else {
      return false;
    }

  }

  public boolean equalsExact(final Spheroid spheroid) {
    if (!EqualsRegistry.equal(authority, spheroid.authority)) {
      return false;
      // } else if (deprecated != spheroid.deprecated) {
      // return false;
    } else if (inverseFlattening != spheroid.inverseFlattening) {
      return false;
      // } else if (!EqualsRegistry.equal(name, spheroid.name)) {
      // return false;
    } else if (semiMajorAxis != spheroid.semiMajorAxis) {
      return false;
    } else if (semiMinorAxis != spheroid.semiMinorAxis) {
      return false;
    } else {
      return true;
    }
  }

  public Authority getAuthority() {
    return authority;
  }

  public double getEccentricity() {
    return eccentricity;
  }

  public double getEccentricitySquared() {
    return eccentricitySquared;
  }

  public double getInverseFlattening() {
    return inverseFlattening;
  }

  public String getName() {
    return name;
  }

  public double getSemiMajorAxis() {
    return semiMajorAxis;
  }

  public double getSemiMinorAxis() {
    return semiMinorAxis;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    final PrecisionModel precision = new PrecisionModel(1000000);
    temp = Double.doubleToLongBits(precision.makePrecise(inverseFlattening));
    result = prime * result + (int)(temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(semiMajorAxis);
    result = prime * result + (int)(temp ^ (temp >>> 32));
    return result;
  }

  public boolean isDeprecated() {
    return deprecated;
  }

  public double meridianRadiusOfCurvature(final double latitude) {
    final double er = 1.0 - eccentricitySquared * Math.sin(latitude)
      * Math.sin(latitude);
    final double el = Math.pow(er, 1.5);
    final double m0 = (semiMajorAxis * (1.0 - eccentricitySquared)) / el;
    return m0;
  }

  public double primeVerticalRadiusOfCurvature(final double latitude) {
    final double t1 = semiMajorAxis * semiMajorAxis;
    final double t2 = t1 * Math.cos(latitude) * Math.cos(latitude);
    final double t3 = semiMinorAxis * semiMinorAxis * Math.sin(latitude)
      * Math.sin(latitude);
    final double n0 = t1 / Math.sqrt(t2 + t3);
    return n0;
  }

  @Override
  public String toString() {
    return name;
  }
}
