package com.revolsys.gis.cs.projection;

import com.revolsys.gis.cs.Datum;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.Spheroid;
import com.revolsys.gis.model.coordinates.Coordinates;

/**
 * An implementation of the Transverse Mercator projection. See section 1.3.5 of
 * <a href="http://www.epsg.org/guides/G7-2.html">OGP Surveying and Positioning
 * Guidance Note number 7, part 2</a>.
 * 
 * @author Paul Austin
 */
public class TransverseMercator implements CoordinatesProjection {

  /** The length in metres of the semi-major axis of the ellipsoid. */
  private final double a;

  /** The coordinate system providing the parameters for the projection. */
  private final ProjectedCoordinateSystem coordinateSystem;

  /** The eccentricity ^ 4 of the ellipsoid. */
  private final double ePow4;

  /** The eccentricity ^ 6 of the ellipsoid. */
  private final double ePow6;

  /** The eccentricity prime squared of the ellipsoid. */
  private final double ePrimeSq;

  /** The eccentricity ^ 2 of the ellipsoid. */
  private final double eSq;

  /** Scale Factor. */
  private final double k0;

  /** Latitude of origin in radians. */
  private final double lambda0;

  /** The value of m at the latitude of origin. */
  private final double m0;

  /** False Easting. */
  private final double x0;

  /** False Northing. */
  private final double y0;

  /**
   * Construct a new TransverseMercator projection.
   * 
   * @param coordinateSystem The coordinate system.
   */
  public TransverseMercator(final ProjectedCoordinateSystem coordinateSystem) {
    this.coordinateSystem = coordinateSystem;
    final GeographicCoordinateSystem geographicCS = coordinateSystem.getGeographicCoordinateSystem();
    final Datum datum = geographicCS.getDatum();
    final double latitudeOfNaturalOrigin = coordinateSystem.getDoubleParameter("latitude_of_natural_origin");
    final double centralMeridian = coordinateSystem.getDoubleParameter("longitude_of_natural_origin");
    final double scaleFactor = coordinateSystem.getDoubleParameter("scale_factor_at_natural_origin");

    final Spheroid spheroid = datum.getSpheroid();
    this.x0 = coordinateSystem.getDoubleParameter("false_easting");
    this.y0 = coordinateSystem.getDoubleParameter("false_northing");
    this.lambda0 = Math.toRadians(centralMeridian);
    this.a = spheroid.getSemiMajorAxis();
    this.k0 = scaleFactor;
    final double phi0 = Math.toRadians(latitudeOfNaturalOrigin);
    eSq = spheroid.getEccentricitySquared();
    ePow4 = eSq * eSq;
    ePow6 = ePow4 * eSq;
    this.m0 = m(phi0);
    this.ePrimeSq = eSq / (1 - eSq);

  }

  /**
   * Project the projected coordinates in metres to lon/lat ordinates in
   * degrees.
   * 
   * <pre>
   * ϕ = ϕ1 – (ν1 * tanϕ1 / ρ1 ) * [
   *   D &circ; 2/2 –
   *   (5 + 3 * T1 + 10 * C1 – 4 * C1 &circ; 2 – 9 * e' &circ; 2) * D &circ; 4 / 24 +
   *   (61 + 90 * T1 + 298 * C1 + 45 * T1 &circ; 2 – 252 * e' &circ; 2 – 3 * C1 &circ; 2) * D &circ; 6 / 720
   * ]
   * λ = λO + [
   *   D –
   *   (1 + 2 * T1 + C1) * D &circ; 3 / 6 +
   *   (5 – 2 * C1 + 28 * T1 –
   *   3 * C1 &circ; 2 + 8 * e' &circ; 2 + 24 * T1 &circ; 2) * D &circ; 5 / 120
   * ] / cosϕ1
   * 
   * ν1 = a /(1 – e &circ; 2 * sinϕ1 &circ; 2) &circ; 0.5
   * ρ1 = a * (1 – e &circ; 2) / (1 – e &circ; 2 * sinϕ1 &circ; 2) &circ; 1.5
   * 
   * ϕ1 = μ1 +
   *   (3 * e1 / 2 – 27 * e1 &circ; 3 /32 + .....) * sin(2 * μ1) +
   *   (21 * e1 &circ; 2 / 16 – 55 * e1 &circ; 4 / 32 + ....) * sin(4 * μ1) +
   *   (151 * e1 &circ; 3 / 96 + .....) * sin(6 * μ1) +
   *   (1097 * e1 &circ; 4 / 512 – ....) * sin(8 * μ1) +
   *   ......
   *   
   * e1 = [1 – (1 – e &circ; 2) &circ; 0.5] / [1 + (1 – e &circ; 2) &circ; 0.5]
   * μ1 = M1 / [a * (1 – e &circ; 2 / 4 – 3 * e &circ; 4 / 64 – 5 * e &circ; 6 / 256 – ....)]
   * M1 = MO + (y – y0) / k0
   * T1 = tanϕ1 &circ; 2
   * C1 = e' &circ; 2 * cosϕ1 &circ; 2
   * e' &circ; 2 = e &circ; 2 / (1 – e &circ; 2)
   * D = (x – x0) / (ν1 * kO)
   * </pre>
   * 
   * @param from The ordinates to convert.
   * @param to The ordinates to write the converted ordinates to.
   */
  @Override
  public void inverse(final Coordinates from, final Coordinates to) {
    final double x = from.getX();
    final double y = from.getY();
    final double m = m0 + (y - y0) / k0;
    final double sqrt1MinusESq = Math.sqrt(1 - eSq);
    final double e1 = (1 - sqrt1MinusESq) / (1 + sqrt1MinusESq);
    final double mu = m
      / (a * (1 - eSq / 4 - 3 * ePow4 / 64 - 5 * ePow6 / 256));
    final double e1Pow2 = e1 * e1;
    final double e1Pow3 = e1Pow2 * e1;
    final double e1Pow4 = e1Pow2 * e1Pow2;
    final double phi11 = mu + (3 * e1 / 2 - 27 * e1Pow3 / 32)
      * Math.sin(2 * mu) + (21 * e1Pow2 / 16 - 55 * e1Pow4 / 32)
      * Math.sin(4 * mu) + (151 * e1Pow3 / 96) * Math.sin(6 * mu)
      + (1097 * e1Pow4 / 512) * Math.sin(8 * mu);

    final double phi1 = phi11;
    final double cosPhi1 = Math.cos(phi1);
    final double sinPhi = Math.sin(phi1);
    final double tanPhi1 = Math.tan(phi1);

    final double oneMinusESqSinPhi1Sq = 1 - eSq * sinPhi * sinPhi;
    final double nu1 = a / Math.sqrt(oneMinusESqSinPhi1Sq);
    final double rho1 = a * (1 - eSq)
      / (oneMinusESqSinPhi1Sq * Math.sqrt(oneMinusESqSinPhi1Sq));
    final double c1 = ePrimeSq * cosPhi1 * cosPhi1;
    final double d = (x - x0) / (nu1 * k0);
    final double d2 = d * d;
    final double d3 = d2 * d;
    final double d4 = d2 * d2;
    final double d5 = d4 * d;
    final double d6 = d4 * d2;
    final double t1 = tanPhi1 * tanPhi1;

    final double c1Sq = c1 * c1;
    final double t1Sq = t1 * t1;
    final double phi = phi1
      - (nu1 * tanPhi1 / rho1)
      * (d2 / 2 - (5 + 3 * t1 + 10 * c1 - 4 * c1Sq - 9 * ePrimeSq) * d4 / 24 + (61
        + 90 * t1 + 298 * c1 + 45 * t1Sq - 252 * ePrimeSq - 3 * c1Sq)
        * d6 / 720);

    final double lambda = lambda0
      + (d - (1 + 2 * t1 + c1) * d3 / 6 + (5 - 2 * c1 + 28 * t1 - 3 * c1Sq + 8
        * ePrimeSq + 24 * t1Sq)
        * d5 / 120) / cosPhi1;

    to.setValue(0, lambda);
    to.setValue(1, phi);
    for (int i = 2; i < from.getNumAxis() && i < to.getNumAxis(); i++) {
      final double ordinate = from.getValue(i);
      to.setValue(i, ordinate);
    }
  }

  /**
   * Calculate the value of m for the given value of phi using the following
   * forumla.
   * 
   * <pre>
   * m = a [
   *   (1 – e2/4 – 3e4/64 – 5e6/256 –....)ϕ –
   *   (3e2/8 + 3e4/32 + 45e6/1024+....)sin2ϕ + 
   *   (15e4/256 + 45e6/1024 +.....)sin4ϕ – 
   *   (35e6/3072 + ....)sin6ϕ + .....
   * ]
   * </pre>
   * 
   * @param phi The phi value in radians.
   * @return The value of m.
   */
  private double m(final double phi) {
    return a
      * ((1 - eSq / 4 - 3 * ePow4 / 64 - 5 * ePow6 / 256) * phi
        - (3 * eSq / 8 + 3 * ePow4 / 32 + 45 * ePow6 / 1024)
        * Math.sin(2 * phi) + (15 * ePow4 / 256 + 45 * ePow6 / 1024)
        * Math.sin(4 * phi) - (35 * ePow6 / 3072) * Math.sin(6 * phi));
  }

  /**
   * Project the lon/lat ordinates in degrees to projected coordinates in
   * metres.
   * 
   * <pre>
   * x = x0 + kO * ν * [
   *   A + (1 – T + C) * A &circ; 3 / 6 +
   *   (5 – 18 * T + T &circ; 2 + 72 *C – 58 *e' &circ; 2 ) * A &circ; 5 / 120
   * ]
   * y = y0 + kO * { M – MO + ν * tanϕ * [
   *   A &circ; 2 / 2 +
   *   (5 – T + 9 * C + 4 * C &circ; 2) * A &circ; 4 / 24 +
   *   (61 – 58 * T + T &circ; 2 + 600 * C – 330 * e' &circ; 2 ) * A &circ; 6 / 720
   * ]}
   * 
   * T = tanϕ * 2
   * C = e &circ; 2 * cosϕ &circ; 2 / (1 – e &circ; 2)
   * A = (λ – λO) * cosϕ
   * ν = a / (1 – e &circ; 2 * sinϕ &circ; 2) &circ; 0.5
   * </pre>
   * 
   * @param from The ordinates to convert.
   * @param to The ordinates to write the converted ordinates to.
   */
  @Override
  public void project(final Coordinates from, final Coordinates to) {
    final double lambda = from.getValue(0);
    final double phi = from.getValue(1);
    final double cosPhi = Math.cos(phi);
    final double sinPhi = Math.sin(phi);
    final double tanPhi = Math.tan(phi);

    final double nu = a / Math.sqrt(1 - eSq * sinPhi * sinPhi);
    final double t = tanPhi * tanPhi;
    final double tSq = t * t;
    final double c = ePrimeSq * cosPhi * cosPhi;
    final double cSq = c * c;
    final double a1 = (lambda - lambda0) * cosPhi;
    final double a1Pow2 = a1 * a1;
    final double a1Pow3 = a1Pow2 * a1;
    final double a1Pow4 = a1Pow2 * a1Pow2;
    final double a1Pow5 = a1Pow4 * a1;
    final double a1Pow6 = a1Pow4 * a1Pow2;
    final double x = x0
      + k0
      * nu
      * (a1 + (1 - t + c) * a1Pow3 / 6 + (5 - 18 * t + tSq + 72 * c - 58 * ePrimeSq)
        * a1Pow5 / 120);

    final double m = m(phi);
    final double y = y0
      + k0
      * (m - m0 + nu
        * tanPhi
        * (a1Pow2 / 2 + (5 - t + 9 * c + 4 * cSq) * a1Pow4 / 24 + (61 - 58 * t
          + tSq + 600 * c - 330 * ePrimeSq)
          * a1Pow6 / 720));
    to.setValue(0, x);
    to.setValue(1, y);
    for (int i = 2; i < from.getNumAxis() && i < to.getNumAxis(); i++) {
      final double ordinate = from.getValue(i);
      to.setValue(i, ordinate);
    }
  }

  /**
   * Return the string representation of the projection.
   * 
   * @return The string.
   */
  @Override
  public String toString() {
    return coordinateSystem.getName();
  }
}
