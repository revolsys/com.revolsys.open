package com.revolsys.geometry.cs.projection;

import com.revolsys.geometry.cs.Datum;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.ProjectionParameterNames;
import com.revolsys.geometry.cs.Spheroid;
import com.revolsys.math.Angle;

/**
 * <h1>Albers Equal Area</h1>
 * <p>
 * (EPSG dataset coordinate operation method code 9822)
 * </p>
 * <p>
 * To derive the projected coordinates of a point, geodetic latitude (φ) is
 * converted to authalic latitude (ß). The formulas to convert geodetic latitude
 * and longitude (φ, λ) to Easting (E) and Northing (N) are:
 * </p>
 *
 * <pre>
 * Easting (E) = EF + (ρ sin θ)
 * Northing(N) = NF +ρO –(ρ cosθ)
 * </pre>
 * <p>
 * where
 * </p>
 *
 * <pre>
 * θ =n (λ–λO)
 * ρ =[a (C–nα)0.5]/n ρO =[a (C–nαO)0.5]/n
 * </pre>
 * <p>
 * and
 * </p>
 *
 * <pre>
 * C = m12 + (n α1)
 * n =(m12 –m22)/(α2 –α1)
 * m1 = cos φ1 / (1 – e2sin2φ1)0.5
 * m2 = cos φ2 / (1 – e2sin2φ2)0.5
 * α = (1 – e2) {[sinφ / (1 – e2sin2φ)] – [1/(2e)] ln [(1 – esinφ) / (1 + esinφ)]}
 * αO = (1 – e2) {[sinφO / (1 – e2sin2φO)] – [1/(2e)] ln [(1 – e sinφO) / (1 + e sinφO)]}
 * α1 = (1 – e2) {[sinφ1 / (1 – e2sin2φ1)] – [1/(2e)] ln [(1 – e sinφ1) / (1 + e sinφ1)]}
 * α2 = (1 – e2) {[sinφ2 / (1 – e2sin2φ2)] – [1/(2e)] ln [(1 – e sinφ2) / (1 + e sinφ2)]}
 * </pre>
 * <p>
 * The reverse formulas to derive the geodetic latitude and longitude of a point
 * from its Easting and Northing values are: φ = ß' + (e2/3 + 31e4/180 +
 * 517e6/5040) sin 2ß'] + [(23e4/360 + 251e6/3780) sin 4ß'] + [(761e6/45360) sin
 * 6ß']
 * </p>
 *
 * <pre>
 * λ = λO + (θ / n)
 * </pre>
 * <p>
 * where
 * </p>
 *
 * <pre>
 * ß'= asin(α'/{1–[(1–e2)/(2 e)] ln[(1–e)/(1+e)] α'= [C–(ρ2 n2 /a2)]/n
 * ρ= {(E–EF)2 +[ρO –(N–NF)]2 }0.5
 * θ= atan[(E–EF)/[ρO –(N–NF)]
 * </pre>
 * <p>
 * and C, n and ρO are as in the forward equations.
 * </p>
 *
 * @author paustin
 */
public class AlbersConicEqualArea extends AbstractCoordinatesProjection {

  /** Constant c = sq(m(phi1) + n * q(phi1) */
  private final double c;

  private final double e;

  /** sq(e). */
  private final double ee;

  /** Central Meridian. */
  private final double lambda0;

  /** Constant n = ( sq(m(phi1)) - sq(m(phi2) ) /( q(phi2) - q(phi1) ) */
  private final double n;

  /** Lattitude of Projection. */
  private final double phi0;

  /** First standard parallel. */
  private final double phi1;

  /** Second standard parallel. */
  private final double phi2;

  /** Constant rho0 = semiMajorAxis * sqrt( C - n * q(phi0) ) / n. */
  private final double rho0;

  private final double semiMajorAxis;

  /** The spheriod. */
  private final Spheroid spheroid;

  /** The false Easting. */
  private final double x0;

  /** The false Northing. */
  private final double y0;

  public AlbersConicEqualArea(final ProjectedCoordinateSystem cs) {
    final GeographicCoordinateSystem geographicCS = cs.getGeographicCoordinateSystem();
    final Datum datum = geographicCS.getDatum();
    final double centralMeridian = cs
      .getDoubleParameter(ProjectionParameterNames.LONGITUDE_OF_CENTER);
    final double firstStandardParallel = cs
      .getDoubleParameter(ProjectionParameterNames.STANDARD_PARALLEL_1);
    final double secondStandardParallel = cs
      .getDoubleParameter(ProjectionParameterNames.STANDARD_PARALLEL_2);
    final double latitudeOfProjection = cs
      .getDoubleParameter(ProjectionParameterNames.LATITUDE_OF_CENTER);
    this.spheroid = datum.getSpheroid();
    this.x0 = cs.getDoubleParameter(ProjectionParameterNames.FALSE_EASTING);
    this.y0 = cs.getDoubleParameter(ProjectionParameterNames.FALSE_NORTHING);
    this.lambda0 = Math.toRadians(centralMeridian);
    this.phi0 = Math.toRadians(latitudeOfProjection);
    this.phi1 = Math.toRadians(firstStandardParallel);
    this.phi2 = Math.toRadians(secondStandardParallel);

    this.semiMajorAxis = this.spheroid.getSemiMajorAxis();

    this.e = this.spheroid.getEccentricity();
    this.ee = this.spheroid.getEccentricitySquared();

    final double m1 = m(this.phi1);
    final double m2 = m(this.phi2);
    final double q0 = q(this.phi0);
    final double q1 = q(this.phi1);
    final double q2 = q(this.phi2);

    this.n = (m1 * m1 - m2 * m2) / (q2 - q1);
    this.c = m1 * m1 + this.n * q1;
    this.rho0 = this.semiMajorAxis * Math.sqrt(this.c - this.n * q0) / this.n;
  }

  /**
   * n = sin(phi1) + sin (phi2)
   * <p>
   * phi = sin-1( ( C - (rho ^ 2) * (n ^ 2) ) / 2 * n )
   * <p>
   * lambda =
   */
  @Override
  public void inverse(final double x, final double y, final double[] targetCoordinates,
    final int targetOffset) {
    final double dX = x - this.x0;
    final double dY = y - this.y0;
    final double theta = Math.atan(dX / (this.rho0 - dY));
    final double rho = Math.sqrt(dX * dX + Math.pow(this.rho0 - dY, 2.0));
    final double q = (this.c
      - rho * rho * this.n * this.n / (this.semiMajorAxis * this.semiMajorAxis)) / this.n;
    final double lambda = this.lambda0 + theta / this.n;
    double li = Math.asin(q / 2.0);
    if (!Double.isNaN(li)) {
      double delta = 10e010;
      final double maxIter = 1000;
      int i = 0;
      do {
        final double sinLi = Math.sin(li);

        final double j1 = Math.pow(1.0 - this.ee * Math.pow(sinLi, 2.0), 2.0)
          / (2.0 * Math.cos(li));
        final double k1 = q / (1.0 - this.ee);
        final double k2 = sinLi / (1.0 - this.ee * Math.pow(sinLi, 2.0));
        final double k3 = 1.0 / (2.0 * this.e)
          * Math.log((1.0 - this.e * sinLi) / (1.0 + this.e * sinLi));
        final double lip1 = li + j1 * (k1 - k2 + k3);
        delta = Math.abs(lip1 - li);
        li = lip1;
        i++;
      } while (!Double.isNaN(li) && delta > 1.0e-011 && i < maxIter);
    }
    double phi;
    if (Double.isNaN(li)) {
      phi = Angle.PI_OVER_2;
    } else {
      phi = li;
    }
    targetCoordinates[targetOffset] = lambda;
    targetCoordinates[targetOffset + 1] = phi;
  }

  /**
   * <pre>
   * cos(phi) / sqrt(1 - sq(e) * sq(sin(phi)))
   * </pre>
   *
   * @param phi The lattitude in radians.
   * @return
   */
  private double m(final double phi) {
    final double sinPhi = Math.sin(phi);
    final double m = Math.cos(phi) / Math.sqrt(1.0 - this.ee * sinPhi * sinPhi);
    return m;
  }

  /**
   * <pre>
   * a = semiMajorAxis
   * lambda = lon;
   * phi = lat;
   *
   * x = rho * sin(theta)
   * y = rho0 - rho * sin(theta)
   *
   * rho = a * sqrt(C - n * q) / n
   *
   * </pre>
   */
  @Override
  public void project(final double lambda, final double phi, final double[] targetCoordinates,
    final int targetOffset) {
    final double q = q(phi);
    final double lminusl0 = lambda - this.lambda0;
    final double n = this.n;
    final double theta = n * lminusl0;
    final double sqrtCminsNQOverN = Math.sqrt(this.c - n * q) / n;
    final double rho = this.semiMajorAxis * sqrtCminsNQOverN;

    final double x = this.x0 + rho * Math.sin(theta);
    final double y = this.y0 + this.rho0 - rho * Math.cos(theta);

    targetCoordinates[targetOffset] = x;
    targetCoordinates[targetOffset + 1] = y;
  }

  /**
   * <pre>
   * (1 - sq(e)) *
   * (
   *   sin(phi) / (1 - sq(e) * sq(sin(phi))) ) -
   *   (1 / (2 * e)) *
   *   ln( ( 1 - e * sin(phi) ) / ( 1 + e sin(phi)))
   * )
   *
   * </pre>
   *
   * @param phi The lattitude in radians
   * @return
   */
  private double q(final double phi) {
    final double sinPhi = Math.sin(phi);
    final double eSinPhi = this.e * sinPhi;
    final double q = (1.0 - this.ee) * (sinPhi / (1.0 - this.ee * sinPhi * sinPhi)
      - 1.0 / (2.0 * this.e) * Math.log((1.0 - eSinPhi) / (1.0 + eSinPhi)));
    return q;
  }
}
