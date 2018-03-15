package com.revolsys.geometry.cs;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;

import com.revolsys.datatype.DataType;
import com.revolsys.math.Angle;
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

  public double astronomicAzimuth(final double lon1, final double lat1, final double h1, double xsi,
    double eta, final double lon2, final double lat2, final double h2) {
    final double a = this.semiMajorAxis;
    final double b = this.semiMinorAxis;

    final double phi1 = Math.toRadians(lat1);
    final double phi2 = Math.toRadians(lat2);
    eta = Math.toRadians(eta);
    xsi = Math.toRadians(xsi);

    final double phim = (phi1 + phi2) / 2;
    final double esq = (a * a - b * b) / (a * a);

    final double sinPh1 = Math.sin(phi1);
    final double d__1 = Math.sqrt(1. - esq * (sinPh1 * sinPh1));
    final double sinPhi2 = Math.sin(phi2);
    final double d__4 = Math.sqrt(1. - esq * (sinPhi2 * sinPhi2));
    final double mm = (a * (1. - esq) / (d__1 * (d__1 * d__1))
      + a * (1. - esq) / (d__4 * (d__4 * d__4))) / 2.;
    final double nm = (a / Math.sqrt(1 - esq * (sinPh1 * sinPh1))
      + a / Math.sqrt(1 - esq * (sinPhi2 * sinPhi2))) / 2;

    final double distance = distanceMetres(lon1, lat1, lon2, lat2);
    final double azimuth = azimuth(lon1, lat1, lon2, lat2);

    // c1 is always 0 as dh is 0

    final double cosPhi2 = Math.cos(phi2);
    final double c2 = h2 / mm * esq * Math.sin(azimuth) * Math.cos(azimuth) * (cosPhi2 * cosPhi2);

    final double cosPhim = Math.cos(phim);
    final double c3 = -esq * (distance * distance) * (cosPhim * cosPhim) * Math.sin(azimuth * 2)
      / (nm * nm * 12);

    double spaz = azimuth + eta * Math.tan(phi1) - c2 - c3;

    if (spaz < 0) {
      spaz = Angle.PI_TIMES_2 + spaz;
    }
    return Math.toDegrees(spaz);
  }

  public double azimuth(final double lon1, final double lat1, final double lon2,
    final double lat2) {
    final double lambda1 = Math.toRadians(lon1);
    final double phi1 = Math.toRadians(lat1);
    final double lambda2 = Math.toRadians(lon2);
    final double phi2 = Math.toRadians(lat2);
    return distanceMetresRadians(lambda1, phi1, lambda2, phi2);
  }

  public double azimuthRadians(final double lambda1, final double phi1, final double lambda2,
    final double phi2) {
    final double f = this.flattening;

    final double deltaLambda = lambda2 - lambda1;
    final double tanU1 = (1 - f) * Math.tan(phi1);
    final double cosU1 = 1 / Math.sqrt(1 + tanU1 * tanU1);
    final double sinU1 = tanU1 * cosU1;
    final double tanU2 = (1 - f) * Math.tan(phi2);
    final double cosU2 = 1 / Math.sqrt(1 + tanU2 * tanU2);
    final double sinU2 = tanU2 * cosU2;

    double lambda = deltaLambda;
    double lastLambda;
    double iterationLimit = 100;
    double cosSqAlpha;
    double sinSigma;
    double cos2SigmaM;
    double sigma;
    double cosSigma;
    double sinLambda;
    double cosLambda;

    do {
      sinLambda = Math.sin(lambda);
      cosLambda = Math.cos(lambda);
      final double sinSqSigma = cosU2 * sinLambda * (cosU2 * sinLambda)
        + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
      sinSigma = Math.sqrt(sinSqSigma);
      if (sinSigma == 0) { // co-incident points
        return 0;
      }
      cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
      sigma = Math.atan2(sinSigma, cosSigma);
      final double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
      cosSqAlpha = 1 - sinAlpha * sinAlpha;
      cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
      if (!Double.isFinite(cos2SigmaM)) {// equatorial line: cosSqAlpha=0 (§6)
        cos2SigmaM = 0;
      }
      final double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
      lastLambda = lambda;
      lambda = deltaLambda + (1 - C) * f * sinAlpha
        * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
    } while (Math.abs(lambda - lastLambda) > 1e-12 && --iterationLimit > 0);
    if (iterationLimit == 0) {
      throw new IllegalStateException("Formula failed to converge");
    }
    double azimuth = Math.atan2(cosU2 * sinLambda, cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
    if (azimuth > Angle.PI_TIMES_2) {
      azimuth -= Angle.PI_TIMES_2;
    } else if (azimuth < 0) {
      azimuth += Angle.PI_TIMES_2;
    }
    return azimuth;
  }

  /**
   * https://www.movable-type.co.uk/scripts/latlong-vincenty.html
   * @author Paul Austin <paul.austin@revolsys.com>
   * @param lambda1
   * @param phi1
   * @param lon2
   * @param lat2
   * @return
   */
  public double distanceMetres(final double lon1, final double lat1, final double lon2,
    final double lat2) {
    final double lambda1 = Math.toRadians(lon1);
    final double phi1 = Math.toRadians(lat1);
    final double lambda2 = Math.toRadians(lon2);
    final double phi2 = Math.toRadians(lat2);
    return distanceMetresRadians(lambda1, phi1, lambda2, phi2);
  }

  public double distanceMetres(final double lon1, final double lat1, final double h1,
    final double lon2, final double lat2, final double h2) {

    final double distance = distanceMetres(lon1, lat1, lon2, lat2);
    final double angleForwards = azimuth(lon1, lat1, lon2, lat2);
    final double angleBackwards = azimuth(lon2, lat2, lon1, lat1);

    final double r1 = radius(lat1, angleForwards);
    final double r2 = radius(lat2, angleBackwards);
    final double deltaH = h2 - h1;
    final double delhsq = deltaH * deltaH;
    final double twor = r1 + r2;
    final double lo = twor * Math.sin(distance / twor);
    final double losq = lo * lo;
    return Math.sqrt(losq * (h1 / r1 + 1.) * (h2 / r2 + 1.) + delhsq);
  }

  public double distanceMetresRadians(final double lambda1, final double phi1, final double lambda2,
    final double phi2) {
    final double f = this.flattening;
    final double a = this.semiMajorAxis;
    final double b = this.semiMinorAxis;

    final double deltaLambda = lambda2 - lambda1;
    final double tanU1 = (1 - f) * Math.tan(phi1);
    final double cosU1 = 1 / Math.sqrt(1 + tanU1 * tanU1);
    final double sinU1 = tanU1 * cosU1;
    final double tanU2 = (1 - f) * Math.tan(phi2);
    final double cosU2 = 1 / Math.sqrt(1 + tanU2 * tanU2);
    final double sinU2 = tanU2 * cosU2;

    double lambda = deltaLambda;
    double lastLambda;
    double iterationLimit = 100;
    double cosSqAlpha;
    double sinSigma;
    double cos2SigmaM;
    double sigma;
    double cosSigma;
    double sinLambda;
    double cosLambda;

    do {
      sinLambda = Math.sin(lambda);
      cosLambda = Math.cos(lambda);
      final double sinSqSigma = cosU2 * sinLambda * (cosU2 * sinLambda)
        + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
      sinSigma = Math.sqrt(sinSqSigma);
      if (sinSigma == 0) { // co-incident points
        return 0;
      }
      cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
      sigma = Math.atan2(sinSigma, cosSigma);
      final double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
      cosSqAlpha = 1 - sinAlpha * sinAlpha;
      cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
      if (!Double.isFinite(cos2SigmaM)) {// equatorial line: cosSqAlpha=0 (§6)
        cos2SigmaM = 0;
      }
      final double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
      lastLambda = lambda;
      lambda = deltaLambda + (1 - C) * f * sinAlpha
        * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
    } while (Math.abs(lambda - lastLambda) > 1e-12 && --iterationLimit > 0);
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

  public double ellipsoidDirection(final double lon1, final double lat1, final double h1,
    double xsi, double eta, final double lon2, final double lat2, final double h2, final double x0,
    final double y0, final double z0, double spatialDirection) {
    final double lambda1 = Math.toRadians(lon1);
    final double phi1 = Math.toRadians(lat1);
    final double lambda2 = Math.toRadians(lon2);
    final double phi2 = Math.toRadians(lat2);
    eta = Math.toRadians(eta);
    xsi = Math.toRadians(xsi);
    spatialDirection = Math.toRadians(spatialDirection);
    final double radians = ellipsoidDirectionRadians(lambda1, phi1, h1, xsi, eta, lambda2, phi2, h2,
      x0, y0, z0, spatialDirection);
    return Math.toDegrees(radians);
  }

  public double ellipsoidDirectionRadians(final double lambda1, final double phi1, final double h1,
    final double xsi, final double eta, final double lambda2, final double phi2, final double h2,
    final double x0, final double y0, final double z0, final double spatialDirection) {

    final double a = this.semiMajorAxis;
    final double b = this.semiMinorAxis;

    final double esq = (a * a - b * b) / (a * a);

    final double sinPhi1 = Math.sin(phi1);
    final double sinPhi2 = Math.sin(phi2);
    final double d__1 = Math.sqrt(1. - esq * (sinPhi1 * sinPhi1));
    final double d__4 = Math.sqrt(1 - esq * (sinPhi2 * sinPhi2));
    final double mm = (a * (1 - esq) / (d__1 * (d__1 * d__1))
      + a * (1 - esq) / (d__4 * (d__4 * d__4))) / 2;
    final double nm = (a / Math.sqrt(1 - esq * (sinPhi1 * sinPhi1))
      + a / Math.sqrt(1 - esq * (sinPhi2 * sinPhi2))) / 2;

    final double s12 = distanceMetresRadians(lambda1, phi1, lambda2, phi2);
    final double a12 = azimuthRadians(lambda1, phi1, lambda2, phi2);

    final double slopeDistance = this.slopeDistanceRadians(lambda1, phi1, h1, lambda2, phi2, h2, x0,
      y0, z0);

    final double dh = h2 - h1;
    final double c1 = (-xsi * Math.sin(a12) + eta * Math.cos(a12)) * dh
      / Math.sqrt(slopeDistance * slopeDistance - dh * dh);

    final double cosPhi2 = Math.cos(phi2);
    final double c2 = h2 * esq * Math.sin(a12) * Math.cos(a12) * cosPhi2 * cosPhi2 / mm;

    final double phim = (phi1 + phi2) / 2;
    final double cosPhim = Math.cos(phim);
    final double c3 = -esq * s12 * s12 * cosPhim * cosPhim * Math.sin(a12 * 2) / (nm * nm * 12);

    return spatialDirection + c1 + c2 + c3;
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

  public double geodeticAzimuth(final double lon1, final double lat1, final double h1, double xsi,
    double eta, final double lon2, final double lat2, final double h2, final double x0,
    final double y0, final double z0, double spaz) {
    final double lambda1 = Math.toRadians(lon1);
    final double phi1 = Math.toRadians(lat1);
    final double lambda2 = Math.toRadians(lon2);
    final double phi2 = Math.toRadians(lat2);
    spaz = Math.toRadians(spaz);
    xsi = Math.toRadians(xsi);
    eta = Math.toRadians(eta);
    final double radians = geodeticAzimuthRadians(lambda1, phi1, h1, xsi, eta, lambda2, phi2, h2,
      x0, y0, z0, spaz);
    return Math.toDegrees(radians);
  }

  public double geodeticAzimuthRadians(final double lambda1, final double phi1, final double h1,
    final double xsi, final double eta, final double lambda2, final double phi2, final double h2,
    final double x0, final double y0, final double z0, final double spaz) {
    final double a = this.semiMajorAxis;
    final double b = this.semiMinorAxis;

    final double phim = (phi1 + phi2) / 2.;
    final double esq = (a * a - b * b) / (a * a);
    final double sinPhi1 = Math.sin(phi1);
    final double mm1 = Math.sqrt(1. - esq * (sinPhi1 * sinPhi1));
    final double sinPhi2 = Math.sin(phi2);
    final double mm2 = Math.sqrt(1. - esq * (sinPhi2 * sinPhi2));
    final double mm = (a * (1 - esq) / (mm1 * (mm1 * mm1)) + a * (1 - esq) / (mm2 * (mm2 * mm2)))
      / 2;
    final double nm = (a / mm1 + a / mm2) / 2;

    final double a12 = azimuthRadians(lambda1, phi1, lambda2, phi2);

    final double s12 = distanceMetresRadians(lambda1, phi1, lambda2, phi2);

    // Always 0 as dh = 0
    final double c1 = 0;// (-(xsi) * Math.sin(a12) + eta * Math.cos(a12)) * 0 / sqrt(ssq - 0 * 0);

    final double cosPhi2 = Math.cos(phi2);
    final double c2 = h2 / mm * esq * Math.sin(a12) * Math.cos(a12) * (cosPhi2 * cosPhi2);

    final double cosPhim = Math.cos(phim);
    final double c3 = -esq * (s12 * s12) * (cosPhim * cosPhim) * Math.sin(a12 * 2) / (nm * nm * 12);

    double geodeticAzimuth = spaz - eta * Math.tan(phi1) + c1 + c2 + c3;
    if (geodeticAzimuth < 0) {
      geodeticAzimuth = Angle.PI_TIMES_2 + geodeticAzimuth;
    }
    return geodeticAzimuth;
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

  public double getFlattening() {
    return this.flattening;
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

  public double horizontalEllipsoidFactor(final double lon1, final double lat1, final double h1,
    final double lon2, final double lat2, final double h2, final double spatialDistance) {
    final double lambda1 = Math.toRadians(lon1);
    final double phi1 = Math.toRadians(lat1);
    final double lambda2 = Math.toRadians(lon2);
    final double phi2 = Math.toRadians(lat2);
    return this.horizontalEllipsoidFactorRadians(lambda1, phi1, h1, lambda2, phi2, h2,
      spatialDistance);
  }

  public double horizontalEllipsoidFactorRadians(final double lambda1, final double phi1,
    final double h1, final double lambda2, final double phi2, final double h2,
    final double spatialDistance) {
    final double a12 = azimuthRadians(lambda1, phi1, lambda2, phi2);
    final double a21 = azimuthRadians(lambda2, phi2, lambda1, phi1);
    final double r1 = radius(phi1, a12);
    final double r2 = radius(phi2, a21);

    final double deltaH = Math.abs(h2 - h1);
    if (deltaH > 30) {
      return r1 / (r1 + h1);
    } else {
      return 1 / Math.sqrt((h1 / r1 + 1) * (h2 / r2 + 1));
    }
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

  protected void print(final String fn, final double a12) {
    System.out
      .println(fn + "=" + new BigDecimal(a12).setScale(18, RoundingMode.HALF_UP).toPlainString());
  }

  public double radius(final double lat, final double alpha) {
    final double phi = Math.toRadians(lat);
    final double eccentricitySquared = this.eccentricitySquared;
    final double sinPhi = Math.sin(phi);
    final double denom = Math.sqrt(1 - eccentricitySquared * (sinPhi * sinPhi));
    final double a = this.semiMajorAxis;
    final double pvrad = a / denom;
    final double merrad = a * (1 - eccentricitySquared) / (denom * (denom * denom));
    final double cosAlpha = Math.cos(alpha);
    final double sinAlpha = Math.sin(alpha);
    return pvrad * merrad / (pvrad * (cosAlpha * cosAlpha) + merrad * (sinAlpha * sinAlpha));
  }

  double radiusRadians(final double phi, final double alpha) {
    final double a = this.semiMajorAxis;
    final double ecc = this.eccentricity;
    final double sinPhi = Math.sin(phi);
    final double denom = Math.sqrt(1. - ecc * (sinPhi * sinPhi));
    final double pvrad = a / denom;
    final double merrad = a * (1. - ecc) / (denom * (denom * denom));
    final double cosAlpha = Math.cos(alpha);
    final double sinAlpha = Math.sin(alpha);
    return pvrad * merrad / (pvrad * (cosAlpha * cosAlpha) + merrad * (sinAlpha * sinAlpha));
  }

  public double slopeDistance(final double lon1, final double lat1, final double h1,
    final double xsi, final double eta, final double lon2, final double lat2, final double h2,
    final double x0, final double y0, final double z0) {
    final double lambda1 = Math.toRadians(lon1);
    final double phi1 = Math.toRadians(lat1);
    final double lambda2 = Math.toRadians(lon2);
    final double phi2 = Math.toRadians(lat2);

    return slopeDistanceRadians(lambda1, phi1, h1, lambda2, phi2, h2, x0, y0, z0);
  }

  public double slopeDistanceRadians(final double lambda1, final double phi1, final double h1,
    final double lambda2, final double phi2, final double h2, final double x0, final double y0,
    final double z0) {
    final double[] p1 = toCartesian(lambda1, phi1, h1, x0, y0, z0);
    final double[] p2 = toCartesian(lambda2, phi2, h2, x0, y0, z0);

    final double deltaX = p1[0] - p2[0];
    final double deltaY = p1[1] - p2[1];
    final double deltaZ = p1[2] - p2[2];
    final double ssq = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    final double slopeDistance = Math.sqrt(ssq);

    return slopeDistance;
  }

  public double spatialDirection(final double lon1, final double lat1, final double h1, double xsi,
    double eta, final double lon2, final double lat2, final double h2, final double x0,
    final double y0, final double z0, final double d12) {

    final double a = this.semiMajorAxis;
    final double b = this.semiMinorAxis;

    final double lambda1 = Math.toRadians(lon1);
    final double phi1 = Math.toRadians(lat1);
    final double lambda2 = Math.toRadians(lon2);
    final double phi2 = Math.toRadians(lat2);
    eta = Math.toRadians(eta);
    xsi = Math.toRadians(xsi);

    final double phim = (phi1 + phi2) / 2;
    final double esq = (a * a - b * b) / (a * a);

    final double sinPhi1 = Math.sin(phi1);
    final double sinPhi2 = Math.sin(phi2);
    final double d__1 = Math.sqrt(1. - esq * (sinPhi1 * sinPhi1));
    final double d__4 = Math.sqrt(1 - esq * (sinPhi2 * sinPhi2));
    final double mm = (a * (1 - esq) / (d__1 * (d__1 * d__1))
      + a * (1 - esq) / (d__4 * (d__4 * d__4))) / 2.;
    final double nm = (a / Math.sqrt(1 - esq * (sinPhi1 * sinPhi1))
      + a / Math.sqrt(1 - esq * (sinPhi2 * sinPhi2))) / 2.;

    final double s12 = distanceMetresRadians(lambda1, phi1, lambda2, phi2);
    final double a12 = azimuthRadians(lambda1, phi1, lambda2, phi2);

    // final double slopeDistance = this.slopeDistanceRadians(lambda1, phi1, h1, lambda2, phi2, h2,
    // x0,
    // y0, z0);

    // final double dh = 0;
    final double c1 = 0;// (-xsi * Math.sin(a12) + eta * Math.cos(a12)) * dh / Math.sqrt(ssq - dh *
                        // dh);

    final double cosPhi2 = Math.cos(phi2);
    final double c2 = h2 / mm * esq * Math.sin(a12) * Math.cos(a12) * (cosPhi2 * cosPhi2);

    final double cosPhim = Math.cos(phim);
    final double c3 = -esq * (s12 * s12) * (cosPhim * cosPhim) * Math.sin(a12 * 2.)
      / (nm * nm * 12.);

    return d12 - c1 - c2 - c3;
  }

  public double spatialDistance(final double lon1, final double lat1, final double h1,
    final double heightOfInstrument, final double heightOfTarget, final double lon2,
    final double lat2, final double h2, final double spatialDistance) {
    final double lambda1 = Math.toRadians(lon1);
    final double phi1 = Math.toRadians(lat1);
    final double lambda2 = Math.toRadians(lon2);
    final double phi2 = Math.toRadians(lat2);
    return spatialDistanceRadians(lambda1, phi1, h1, heightOfInstrument, heightOfTarget, lambda2,
      phi2, h2, spatialDistance);
  }

  public double spatialDistanceRadians(final double lambda1, final double phi1, double h1,
    final double heightOfInstrument, final double heightOfTarget, final double lambda2,
    final double phi2, double h2, final double spatialDistance) {

    final double a12 = azimuthRadians(lambda1, phi1, lambda2, phi2);
    final double a21 = azimuthRadians(lambda2, phi2, lambda1, phi1);
    final double r1 = radius(phi1, a12);
    final double r2 = radius(phi2, a21);

    h1 += heightOfInstrument;
    h2 += heightOfTarget;
    final double deltaH = h2 - h1;
    final double deltaHSq = deltaH * deltaH;
    if (spatialDistance * spatialDistance - deltaHSq >= 0) {
      final double twor = r1 + r2;
      final double lo = Math
        .sqrt((spatialDistance * spatialDistance - deltaHSq) / ((h1 / r1 + 1) * (h2 / r2 + 1)));
      return twor * Math.asin(lo / twor);
    } else {
      return spatialDistance;
    }
  }

  private double[] toCartesian(final double lambda, final double phi, final double h,
    final double x0, final double y0, final double z0) {
    final double a = this.semiMajorAxis;
    final double b = this.semiMinorAxis;

    final double e2 = (a * a - b * b) / (a * a);
    final double sp = Math.sin(phi);
    final double cp = Math.cos(phi);

    final double n = a / Math.sqrt(1 - e2 * (sp * sp));
    final double x = x0 + (n + h) * cp * Math.cos(lambda);
    final double y = y0 + (n + h) * cp * Math.sin(lambda);
    final double z = z0 + (n * (1 - e2) + h) * sp;
    return new double[] {
      x, y, z
    };
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
