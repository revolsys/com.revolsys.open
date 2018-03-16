package com.revolsys.geometry.cs.projection;

import com.revolsys.geometry.cs.Ellipsoid;
import com.revolsys.geometry.cs.NormalizedParameterNames;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.math.Angle;
import com.revolsys.math.FastMath;

/**
 * An implementation of the Transverse Mercator projection. See section 1.3.5 of
 * <a href="http://www.epsg.org/guides/G7-2.html">OGP Surveying and Positioning
 * Guidance Note number 7, part 2.</a> Krüger and published in Finland as Recommendations for Public Administration (JHS) 154.
 *
 *
 * @author Paul Austin
 */
public class TransverseMercatorThomas extends AbstractCoordinatesProjection {

  /**
   * Then the meridional arc distance from equator to the projection origin (MO) is computed from
   *
   *
   * Note: if the projection grid origin is very close to but not exactly at the pole (within 2" or 50m), the tangent function in the equation above for QO is unstable and may fail. MO may instead be calculated (as in the USGS formula below) from:
   *
   * MO = a[(1 – e2/4 – 3e4/64 – 5e6/256 –....)φO – (3e2/8 + 3e4/32 + 45e6/1024+....)sin2φO
   *      + (15e4/256 + 45e6/1024 +.....)sin4φO – (35e6/3072 + ....)sin6φO + .....]
   * @param phi
   * @return
   */
  private static double mo(final double phi) {
    final double B = 0;
    final double e = 0;
    final double h1 = 0;
    final double h2 = 0;
    final double h3 = 0;
    final double h4 = 0;
    if (phi == 0) {
      return 0;
    } else if (phi == Angle.PI_OVER_2) {
      return B * Angle.PI_OVER_2;
    } else if (phi == -Angle.PI_OVER_2) {
      return B * -Angle.PI_OVER_2;
    } else {
      final double QO = FastMath.asinh(Math.tan(phi)) - e * FastMath.atanh(e * Math.sin(phi));
      final double βO = Math.atan(Math.sinh(QO));
      final double ξO0 = Math.asin(Math.sin(βO));
      // Note: The previous two steps are taken from the generic calculation flow given below for
      // latitude φ, but here for φO may be simplified to ξO0 = βO = atan(sinh QO).
      final double ξO1 = h1 * Math.sin(2 * ξO0);
      final double ξO2 = h2 * Math.sin(4 * ξO0);
      final double ξO3 = h3 * Math.sin(6 * ξO0);
      final double ξO4 = h4 * Math.sin(8 * ξO0);
      final double ξO = ξO0 + ξO1 + ξO2 + ξO3 + ξO4;
      return B * ξO;
    }
  }

  /** Scale Factor. */
  private final double ko;

  /** False Easting. */
  private final double xo;

  /** False Northing. */
  private final double yo;

  private final double mo;

  private final double n;

  private final double B;

  private final double h1Prime;

  private final double h2Prime;

  private final double h3Prime;

  private final double h4Prime;

  private final double e;

  private final double λo;

  private final double a;

  private final double b;

  private final String name;

  public TransverseMercatorThomas(final ProjectedCoordinateSystem coordinateSystem) {
    this(//
      coordinateSystem.getCoordinateSystemName(), //
      coordinateSystem.getEllipsoid(), //
      coordinateSystem.getDoubleParameter(NormalizedParameterNames.CENTRAL_MERIDIAN), //
      coordinateSystem.getDoubleParameter(NormalizedParameterNames.LATITUDE_OF_ORIGIN), //
      coordinateSystem.getDoubleParameter(NormalizedParameterNames.SCALE_FACTOR), //
      coordinateSystem.getDoubleParameter(NormalizedParameterNames.FALSE_EASTING), //
      coordinateSystem.getDoubleParameter(NormalizedParameterNames.FALSE_NORTHING) //
    );
  }

  public TransverseMercatorThomas(final String name, final Ellipsoid ellipsoid,
    final double longitudeOrigin, final double latitudeOrigin, final double ko, final double xo,
    final double yo) {
    this.name = name;
    this.xo = xo;
    this.yo = yo;
    this.λo = Math.toRadians(longitudeOrigin);
    final double φo = Math.toRadians(latitudeOrigin);
    this.ko = ko;

    this.a = ellipsoid.getSemiMajorAxis();
    this.b = ellipsoid.getSemiMinorAxis();
    final double f = ellipsoid.getFlattening();
    this.e = ellipsoid.getEccentricity();
    this.mo = mo(φo);
    this.n = f / (2 - f);
    final double n2 = this.n * this.n;
    final double n3 = this.n * n2;
    final double n4 = n2 * n2;
    this.B = this.a / (1 + this.n) * (1 + n2 / 4 + n4 / 64);

    this.h1Prime = this.n / 2 - 2 / 3 * n2 + 37 / 96 * n3 - 1 / 360 * n4;
    this.h2Prime = 1 / 48 * n2 + 1 / 15 * n3 - 437 / 1440 * n4;
    this.h3Prime = 17 / 480 * n3 - 37 / 840 * n4;
    this.h4Prime = 4397 / 161280 * n4;
  }

  /**
   * Project the projected coordinates in metres to lon/lat cordinates in degrees.
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param targetCoordinates The ordinates to write the converted ordinates to.
   */
  @Override
  public void inverse(final double x, final double y, final double[] targetCoordinates,
    final int targetOffset) {
    final double ηPrime = (x - this.xo) / (this.B * this.ko);
    final double ξPrime = (y - this.yo + this.ko * this.mo) / (this.B * this.ko);

    final double ξ1Prime = this.h1Prime * Math.sin(2 * ξPrime) * Math.cosh(2 * ηPrime);
    final double η1Prime = this.h1Prime * Math.cos(2 * ξPrime) * Math.sinh(2 * ηPrime);
    final double ξ2Prime = this.h2Prime * Math.sin(4 * ξPrime) * Math.cosh(4 * ηPrime);
    final double η2Prime = this.h2Prime * Math.cos(4 * ξPrime) * Math.sinh(4 * ηPrime);
    final double ξ3Prime = this.h3Prime * Math.sin(6 * ξPrime) * Math.cosh(6 * ηPrime);
    final double η3Prime = this.h3Prime * Math.cos(6 * ξPrime) * Math.sinh(6 * ηPrime);
    final double ξ4Prime = this.h4Prime * Math.sin(8 * ξPrime) * Math.cosh(8 * ηPrime);
    final double η4Prime = this.h4Prime * Math.cos(8 * ξPrime) * Math.sinh(8 * ηPrime);
    final double ξ0Prime = ξPrime - (ξ1Prime + ξ2Prime + ξ3Prime + ξ4Prime);
    final double η0Prime = ηPrime - (η1Prime + η2Prime + η3Prime + η4Prime);
    final double βPrime = Math.asin(Math.sin(ξ0Prime) / Math.cosh(η0Prime));
    final double QPrime = FastMath.asinh(Math.tan(βPrime));
    double QPrimePrime = QPrime + this.e * FastMath.atanh(this.e * Math.tanh(QPrime));
    final double lastQPrimePrime = QPrimePrime;
    int i = 0;
    do {
      QPrimePrime = QPrime + this.e * FastMath.atanh(this.e * Math.tanh(QPrimePrime));
    } while (Math.abs(lastQPrimePrime - QPrimePrime) < 1.0e-011 && ++i < 100);

    final double λ = this.λo + Math.asin(Math.tanh(η0Prime) / Math.cos(βPrime));
    final double φ = Math.atan(Math.sinh(QPrimePrime));

    targetCoordinates[targetOffset] = Math.toDegrees(λ);
    targetCoordinates[targetOffset + 1] = Math.toDegrees(φ);
  }

  /**
   * Project the lon/lat ordinates in degrees to projected coordinates in metres.
   *
   * @param from The ordinates to convert.
   * @param to The ordinates to write the converted ordinates to.
   */
  @Override
  public void project(final double lon, final double lat, final double[] targetCoordinates,
    final int targetOffset) {
    final double deltaLambda = Math.toRadians(lon) - this.λo;
    final double phi = Math.toRadians(lat);

    final double sing = 2e-9;
    final double sp = Math.sin(phi);
    final double cp = Math.cos(phi);
    final double t = Math.tan(phi);
    final double a = this.a;
    final double b = this.b;
    final double e = Math.sqrt((a * a - b * b) / (a * a));
    final double eta = Math.sqrt((a * a - b * b) / (b * b) * (cp * cp));

    final double e2 = (a * a - b * b) / (a * a);
    final double e4 = e2 * e2;
    final double e6 = e4 * e2;
    final double e8 = e6 * e2;
    final double a0 = 1 - e2 / 4 - e4 * 3 / 64 - e6 * 5 / 256 - e8 * 175 / 16384;
    final double a2 = (e2 + e4 / 4. + e6 * 15. / 128. - e8 * 455. / 4096.) * .375;
    final double a4 = (e4 + e6 * 3. / 4. - e8 * 77. / 128.) * .05859375;
    final double a6 = (e6 - e8 * 41. / 32.) * .011393229166666666;
    final double a8 = e8 * -315. / 131072.;
    final double sphi = a * (a0 * phi - a2 * Math.sin(phi * 2) + a4 * Math.sin(phi * 4)
      - a6 * Math.sin(phi * 6) + a8 * Math.sin(phi * 8));

    final double dn = a / Math.sqrt(1. - e * e * (sp * sp));
    double x = 0;
    double y = sphi;
    if (Math.abs(deltaLambda) >= sing) {
      final double deltaLambdaSq = deltaLambda * deltaLambda;
      final double cpSq = cp * cp;
      final double tSq = t * t;
      final double etaSq = eta * eta;
      x = dn * (deltaLambda * cp
        + deltaLambda * deltaLambdaSq * (cp * cpSq) / 6. * (1. - tSq + eta * eta)
        + deltaLambda * (deltaLambdaSq * deltaLambdaSq) * (cp * (cpSq * cpSq)) / 120.
          * (5. - tSq * 18. + tSq * tSq + eta * eta * 14. - tSq * 58. * etaSq + etaSq * etaSq * 13.
            + etaSq * (etaSq * etaSq) * 4. - etaSq * etaSq * 64. * tSq
            - etaSq * (etaSq * etaSq) * 24. * tSq)
        + deltaLambdaSq * deltaLambda * (deltaLambdaSq * deltaLambdaSq) / 5040.
          * (cpSq * cp * (cpSq * cpSq))
          * (61. - tSq * 479. + tSq * tSq * 179. - tSq * (tSq * tSq)));

      y = sphi + dn * (deltaLambdaSq / 2. * sp * cp
        + deltaLambdaSq * deltaLambdaSq / 24. * sp * (cp * cpSq)
          * (5. - tSq + etaSq * 9. + etaSq * etaSq * 4.)
        + deltaLambdaSq * (deltaLambdaSq * deltaLambdaSq) / 720. * sp * (cp * (cpSq * cpSq))
          * (61. - tSq * 58. + tSq * tSq + etaSq * 270. - tSq * 330. * etaSq + etaSq * etaSq * 445.
            + etaSq * (etaSq * etaSq) * 324. - etaSq * etaSq * 680. * tSq
            + eta * etaSq * (eta * etaSq) * 88. - etaSq * (etaSq * etaSq) * 600. * tSq
            - etaSq * etaSq * (etaSq * etaSq) * 192. * tSq)
        + deltaLambdaSq * deltaLambdaSq * (deltaLambdaSq * deltaLambdaSq) / 40320. * sp
          * (cp * cpSq * (cpSq * cpSq))
          * (1385. - tSq * 3111. + tSq * tSq * 543. - tSq * (tSq * tSq)));
    }

    x = this.xo + this.ko * x;
    y = this.ko * y;
    targetCoordinates[targetOffset] = x;
    targetCoordinates[targetOffset + 1] = y;
  }

  /**
   * Return the string representation of the projection.
   *
   * @return The string.
   */
  @Override
  public String toString() {
    return this.name;
  }
}
