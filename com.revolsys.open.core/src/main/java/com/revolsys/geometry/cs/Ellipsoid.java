package com.revolsys.geometry.cs;

import java.io.Serializable;
import java.security.MessageDigest;

import com.revolsys.datatype.DataType;
import com.revolsys.util.Md5;
import com.revolsys.util.number.Doubles;

public class Ellipsoid implements Serializable {
  private static final long serialVersionUID = -8349864136575195872L;

  private final Authority authority;

  private final boolean deprecated;

  private final double eccentricity;

  private final double eccentricitySquared;

  private double inverseFlattening;

  private final String name;

  /** Radius of earth at equator. */
  private final double semiMajorAxis;

  private final double semiMajorAxisSq;

  /** Radius of earth at poles. */
  private double semiMinorAxis;

  private final double semiMinorAxisSq;

  private final double flattening;

  public Ellipsoid(final String name, final double semiMajorAxis, final double inverseFlattening,
    final Authority authority) {
    this(name, semiMajorAxis, Double.NaN, inverseFlattening, authority, false);
  }

  public Ellipsoid(final String name, final double semiMajorAxis, final double semiMinorAxis,
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
    this.flattening = 1.0 / this.inverseFlattening;

    if (Double.isNaN(semiMinorAxis)) {
      this.semiMinorAxis = semiMajorAxis - semiMajorAxis * this.flattening;
    }
    this.semiMajorAxisSq = semiMajorAxis * semiMajorAxis;
    this.semiMinorAxisSq = this.semiMinorAxis * this.semiMinorAxis;
    // eccentricitySquared = 1.0 - b2 / a2;

    this.eccentricitySquared = this.flattening + this.flattening
      - this.flattening * this.flattening;
    this.eccentricity = Math.sqrt(this.eccentricitySquared);
  }

  public double azimuthBackwards(double lon1, double lat1, double lon2, double lat2) {
    final double f = this.flattening;

    lon1 = Math.toRadians(lon1);
    lon2 = Math.toRadians(lon2);

    lat1 = Math.toRadians(lat1);
    lat2 = Math.toRadians(lat2);

    final double deltaLon = lon2 - lon1;
    final double tanU1 = (1 - f) * Math.tan(lat1), cosU1 = 1 / Math.sqrt(1 + tanU1 * tanU1),
        sinU1 = tanU1 * cosU1;
    final double tanU2 = (1 - f) * Math.tan(lat2), cosU2 = 1 / Math.sqrt(1 + tanU2 * tanU2),
        sinU2 = tanU2 * cosU2;

    double lon = deltaLon;
    double lastLon;
    double iterationLimit = 100;
    double cosSqAlpha;
    double sinSigma;
    double cos2SigmaM;
    double sigma;
    double cosSigma;
    double sinlon;
    double coslon;

    do {
      sinlon = Math.sin(lon);
      coslon = Math.cos(lon);
      final double sinSqSigma = cosU2 * sinlon * (cosU2 * sinlon)
        + (cosU1 * sinU2 - sinU1 * cosU2 * coslon) * (cosU1 * sinU2 - sinU1 * cosU2 * coslon);
      sinSigma = Math.sqrt(sinSqSigma);
      if (sinSigma == 0) { // co-incident points
        return 0;
      }
      cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * coslon;
      sigma = Math.atan2(sinSigma, cosSigma);
      final double sinAlpha = cosU1 * cosU2 * sinlon / sinSigma;
      cosSqAlpha = 1 - sinAlpha * sinAlpha;
      cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
      if (!Double.isFinite(cos2SigmaM)) {// equatorial line: cosSqAlpha=0 (§6)
        cos2SigmaM = 0;
      }
      final double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
      lastLon = lon;
      lon = deltaLon + (1 - C) * f * sinAlpha
        * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
    } while (Math.abs(lon - lastLon) > 1e-12 && --iterationLimit > 0);
    if (iterationLimit == 0) {
      throw new IllegalStateException("Formula failed to converge");
    }
    return Math.atan2(cosU1 * sinlon, -sinU1 * cosU2 + cosU1 * sinU2 * coslon);
  }

  public double azimuthForwards(double lon1, double lat1, double lon2, double lat2) {
    final double f = this.flattening;

    lon1 = Math.toRadians(lon1);
    lon2 = Math.toRadians(lon2);

    lat1 = Math.toRadians(lat1);
    lat2 = Math.toRadians(lat2);

    final double deltaLon = lon2 - lon1;
    final double tanU1 = (1 - f) * Math.tan(lat1), cosU1 = 1 / Math.sqrt(1 + tanU1 * tanU1),
        sinU1 = tanU1 * cosU1;
    final double tanU2 = (1 - f) * Math.tan(lat2), cosU2 = 1 / Math.sqrt(1 + tanU2 * tanU2),
        sinU2 = tanU2 * cosU2;

    double lon = deltaLon;
    double lastLon;
    double iterationLimit = 100;
    double cosSqAlpha;
    double sinSigma;
    double cos2SigmaM;
    double sigma;
    double cosSigma;
    double sinlon;
    double coslon;

    do {
      sinlon = Math.sin(lon);
      coslon = Math.cos(lon);
      final double sinSqSigma = cosU2 * sinlon * (cosU2 * sinlon)
        + (cosU1 * sinU2 - sinU1 * cosU2 * coslon) * (cosU1 * sinU2 - sinU1 * cosU2 * coslon);
      sinSigma = Math.sqrt(sinSqSigma);
      if (sinSigma == 0) { // co-incident points
        return 0;
      }
      cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * coslon;
      sigma = Math.atan2(sinSigma, cosSigma);
      final double sinAlpha = cosU1 * cosU2 * sinlon / sinSigma;
      cosSqAlpha = 1 - sinAlpha * sinAlpha;
      cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
      if (!Double.isFinite(cos2SigmaM)) {// equatorial line: cosSqAlpha=0 (§6)
        cos2SigmaM = 0;
      }
      final double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
      lastLon = lon;
      lon = deltaLon + (1 - C) * f * sinAlpha
        * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
    } while (Math.abs(lon - lastLon) > 1e-12 && --iterationLimit > 0);
    if (iterationLimit == 0) {
      throw new IllegalStateException("Formula failed to converge");
    }
    return Math.atan2(cosU2 * sinlon, cosU1 * sinU2 - sinU1 * cosU2 * coslon);
  }

  /**
   * https://www.movable-type.co.uk/scripts/latlong-vincenty.html
   * @author Paul Austin <paul.austin@revolsys.com>
   * @param lon1
   * @param lat1
   * @param lon2
   * @param lat2
   * @return
   */
  public double distanceMetres(double lon1, double lat1, double lon2, double lat2) {
    final double f = this.flattening;
    final double a = this.semiMajorAxis;
    final double b = this.semiMinorAxis;

    lon1 = Math.toRadians(lon1);
    lon2 = Math.toRadians(lon2);

    lat1 = Math.toRadians(lat1);
    lat2 = Math.toRadians(lat2);

    final double deltaLon = lon2 - lon1;
    final double tanU1 = (1 - f) * Math.tan(lat1), cosU1 = 1 / Math.sqrt(1 + tanU1 * tanU1),
        sinU1 = tanU1 * cosU1;
    final double tanU2 = (1 - f) * Math.tan(lat2), cosU2 = 1 / Math.sqrt(1 + tanU2 * tanU2),
        sinU2 = tanU2 * cosU2;

    double lon = deltaLon;
    double lastLon;
    double iterationLimit = 100;
    double cosSqAlpha;
    double sinSigma;
    double cos2SigmaM;
    double sigma;
    double cosSigma;
    double sinlon;
    double coslon;

    do {
      sinlon = Math.sin(lon);
      coslon = Math.cos(lon);
      final double sinSqSigma = cosU2 * sinlon * (cosU2 * sinlon)
        + (cosU1 * sinU2 - sinU1 * cosU2 * coslon) * (cosU1 * sinU2 - sinU1 * cosU2 * coslon);
      sinSigma = Math.sqrt(sinSqSigma);
      if (sinSigma == 0) { // co-incident points
        return 0;
      }
      cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * coslon;
      sigma = Math.atan2(sinSigma, cosSigma);
      final double sinAlpha = cosU1 * cosU2 * sinlon / sinSigma;
      cosSqAlpha = 1 - sinAlpha * sinAlpha;
      cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
      if (!Double.isFinite(cos2SigmaM)) {// equatorial line: cosSqAlpha=0 (§6)
        cos2SigmaM = 0;
      }
      final double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
      lastLon = lon;
      lon = deltaLon + (1 - C) * f * sinAlpha
        * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
    } while (Math.abs(lon - lastLon) > 1e-12 && --iterationLimit > 0);
    if (iterationLimit == 0) {
      throw new IllegalStateException("Formula failed to converge");
    }

    final double uSq = cosSqAlpha * (a * a - b * b) / (b * b);
    final double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
    final double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
    final double deltaSigmaSigma = B * sinSigma
      * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM
        * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));

    final double s = b * A * (sigma - deltaSigmaSigma);
    return s;
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    }
    if (object == this) {
      return true;
    } else if (object instanceof Ellipsoid) {
      final Ellipsoid ellipsoid = (Ellipsoid)object;
      if (Doubles.makePrecise(1000000.0, this.inverseFlattening) != Doubles.makePrecise(1000000.0,
        ellipsoid.inverseFlattening)) {
        return false;
      } else if (this.semiMajorAxis != ellipsoid.semiMajorAxis) {
        return false;
      }
      return true;
    } else {
      return false;
    }

  }

  public boolean equalsExact(final Ellipsoid ellipsoid) {
    if (!DataType.equal(this.authority, ellipsoid.authority)) {
      return false;
      // } else if (deprecated != spheroid.deprecated) {
      // return false;
    } else if (this.inverseFlattening != ellipsoid.inverseFlattening) {
      return false;
      // } else if (!Equals.equal(name, spheroid.name)) {
      // return false;
    } else if (this.semiMajorAxis != ellipsoid.semiMajorAxis) {
      return false;
    } else if (this.semiMinorAxis != ellipsoid.semiMinorAxis) {
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

  public double getRadiusFromDegrees(final double lat) {
    final double phi = Math.toRadians(lat);
    return getRadiusFromRadians(phi);
  }

  /**
   * R(phi)=sqrt(((a² cos(phi))²+(b² sin(phi))²)/((a cos(phi))²+(b sin(phi))²))
   */
  public double getRadiusFromRadians(final double lat) {
    final double cosLat = Math.cos(lat);
    final double sinLat = Math.sin(lat);
    final double aCosLat = this.semiMajorAxis * cosLat;
    final double aSqCosLat = this.semiMajorAxisSq * cosLat;
    final double bSinLat = this.semiMinorAxis * sinLat;
    final double bSqSinLat = this.semiMinorAxisSq * sinLat;
    return Math.sqrt( //
      (aSqCosLat * aSqCosLat + bSqSinLat * bSinLat) / //
        (aCosLat * aCosLat + bSinLat * bSinLat)//
    );
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
    temp = Double.doubleToLongBits(Doubles.makePrecise(1000000.0, this.inverseFlattening));
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

  public void updateDigest(final MessageDigest digest) {
    Md5.update(digest, this.semiMajorAxis);
    Md5.update(digest, this.inverseFlattening);
  }
}
