package com.revolsys.geometry.cs.projection;

import com.revolsys.geometry.cs.Ellipsoid;
import com.revolsys.geometry.cs.NormalizedParameterNames;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;

/**
 * An implementation of the Transverse Mercator projection. See section 1.3.5 of
 * <a href="http://www.epsg.org/guides/G7-2.html">OGP Surveying and Positioning
 * Guidance Note number 7, part 2.</a> Krüger and published in Finland as Recommendations for Public Administration (JHS) 154.
 *
 *
 * @author Paul Austin
 */
public class TransverseMercatorThomas extends AbstractCoordinatesProjection {
  private final double ko;

  private final double xo;

  private final double e;

  private final double λo;

  private final double a;

  private final double b;

  private final String name;

  private final double a0;

  private final double a2;

  private final double a4;

  private final double a6;

  private final double a8;

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
    this.λo = Math.toRadians(longitudeOrigin);
    this.ko = ko;

    this.a = ellipsoid.getSemiMajorAxis();
    this.b = ellipsoid.getSemiMinorAxis();
    final double e2 = (this.a * this.a - this.b * this.b) / (this.a * this.a);
    this.e = Math.sqrt(e2);

    final double e4 = e2 * e2;
    final double e6 = e4 * e2;
    final double e8 = e6 * e2;
    this.a0 = 1 - e2 / 4 - e4 * 3 / 64 - e6 * 5 / 256 - e8 * 175 / 16384;
    this.a2 = (e2 + e4 / 4. + e6 * 15. / 128. - e8 * 455. / 4096.) * .375;
    this.a4 = (e4 + e6 * 3. / 4. - e8 * 77. / 128.) * .05859375;
    this.a6 = (e6 - e8 * 41. / 32.) * .011393229166666666;
    this.a8 = e8 * -315. / 131072.;
  }

  /**
   * Project the projected coordinates in metres to lon/lat cordinates in degrees.
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param targetCoordinates The ordinates to write the converted ordinates to.
   */
  @Override
  public void inverse(double x, double y, final double[] targetCoordinates,
    final int targetOffset) {
    x = (x - this.xo) / this.ko;
    y /= this.ko;
    final double a = this.a;
    final double b = this.b;
    final double e = this.e;

    double phi1 = y / a;
    double dphi;
    do {
      dphi = (a * (this.a0 * phi1 - this.a2 * Math.sin(phi1 * 2) + this.a4 * Math.sin(phi1 * 4)
        - this.a6 * Math.sin(phi1 * 6) + this.a8 * Math.sin(phi1 * 8)) - y)
        / (a * (this.a0 - this.a2 * 2 * Math.cos(phi1 * 2) + this.a4 * 4. * Math.cos(phi1 * 4)
          - this.a6 * 6 * Math.cos(phi1 * 6) + this.a8 * 8 * Math.cos(phi1 * 8)));
      phi1 -= dphi;

    } while (Math.abs(dphi) >= 1e-15);

    final double t = Math.tan(phi1);
    final double tp2 = t * t;
    final double tp4 = tp2 * tp2;
    final double tp6 = tp2 * tp4;

    final double sp = Math.sin(phi1);
    final double sp2 = sp * sp;
    final double cp = Math.cos(phi1);

    final double eta = Math.sqrt((a * a - b * b) / (b * b) * (cp * cp));
    final double etap2 = eta * eta;
    final double etap4 = etap2 * etap2;
    final double etap6 = etap2 * etap4;
    final double etap8 = etap4 * etap4;
    final double dn = a / Math.sqrt(1. - e * e * (sp * sp));
    final double d__2 = 1. - e * e * sp2;
    final double dm = a * (1 - e * e) / Math.sqrt(d__2 * (d__2 * d__2));
    final double xbydn = x / dn;

    final double φ = phi1 + t * (-(x * x) / (dm * 2 * dn)
      + xbydn * (xbydn * xbydn) * x / (dm * 24)
        * (tp2 * 3 + 5 + etap2 - etap4 * 4. - etap2 * 9 * tp2)
      - xbydn * (xbydn * xbydn * xbydn * xbydn) * x / (dm * 720)
        * (tp2 * 90 + 61 + etap2 * 46 + tp4 * 45 - tp2 * 252 * etap2 - etap4 * 3 + etap6 * 100
          - tp2 * 66 * etap4 - tp4 * 90 * etap2 + etap8 * 88 + tp4 * 225 * etap4 + tp2 * 84. * etap6
          - tp2 * 192. * etap8)
      + xbydn * xbydn * xbydn * (xbydn * xbydn * xbydn * xbydn) * x / (dm * 40320)
        * (tp2 * 3633 + 1385 + tp4 * 4095 + tp6 * 1574));

    final double λ = this.λo + (xbydn - xbydn * (xbydn * xbydn) / 6 * (tp2 * 2 + 1. + etap2)
      + xbydn * (xbydn * xbydn * xbydn * xbydn) / 120
        * (etap2 * 6 + 5 + tp2 * 28 - etap4 * 3 + tp2 * 8 * etap2 + tp4 * 24 - etap6 * 4
          + tp2 * 4 * etap4 + tp2 * 24 * etap6)
      - xbydn * xbydn * xbydn * (xbydn * xbydn * xbydn * xbydn) / 5040
        * (tp2 * 662 + 61 + tp4 * 1320 + tp6 * 720))
      / cp;

    final double lon = Math.toDegrees(λ);
    final double lat = Math.toDegrees(φ);

    targetCoordinates[targetOffset] = lon;
    targetCoordinates[targetOffset + 1] = lat;
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
    final double e = this.e;

    final double eta = Math.sqrt((a * a - b * b) / (b * b) * (cp * cp));
    final double sphi = a * (this.a0 * phi - this.a2 * Math.sin(phi * 2)
      + this.a4 * Math.sin(phi * 4) - this.a6 * Math.sin(phi * 6) + this.a8 * Math.sin(phi * 8));

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
