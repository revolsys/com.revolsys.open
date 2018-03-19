package com.revolsys.geometry.cs.projection;

import com.revolsys.geometry.cs.Ellipsoid;
import com.revolsys.geometry.cs.NormalizedParameterNames;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;

/**
 * An implementation of the Transverse Mercator projection. See section 1.3.5 of
 * <a href="http://www.epsg.org/guides/G7-2.html">OGP Surveying and Positioning
 * Guidance Note number 7, part 2</a>. Snyder in US Geological Survey Professional Paper #1395
 *
 * @author Paul Austin
 */
public class TransverseMercatorUsgs extends TransverseMercator {

  /** The eccentricity ^ 4 of the ellipsoid. */
  private final double ePow4;

  /** The eccentricity ^ 6 of the ellipsoid. */
  private final double ePow6;

  /** The eccentricity prime squared of the ellipsoid. */
  private final double ePrimeSq;

  /** The eccentricity ^ 2 of the ellipsoid. */
  private final double eSq;

  private final double sqrt1MinusESq;

  /** The value of m at the latitude of origin. */
  private final double mo;

  private final double e1Time2Div2MinusE1Pow3Times27Div32;

  private final double e1Pow2Times21Div16MinusE1Pow4Times55Div32;

  private final double e1Pow3Times151Div96;

  private final double e1Pow4Times1097Div512;

  private final double aTimes1MinusEsqDiv4MinesEPow4Times3Div64MinusEPow6Times5Div256;

  private final double ePrimeSqTimes9;

  private final double ePrimeSqTimes8;

  /**
   * Construct a new TransverseMercator projection.
   *
   * @param coordinateSystem The coordinate system.
   */
  public TransverseMercatorUsgs(final ProjectedCoordinateSystem coordinateSystem) {
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

  public TransverseMercatorUsgs(final String name, final Ellipsoid ellipsoid,
    final double longitudeOrigin, final double latitudeOrigin, final double ko, final double xo,
    final double yo) {
    super(name, ellipsoid, longitudeOrigin, latitudeOrigin, ko, xo, yo);
    final double φ0 = Math.toRadians(latitudeOrigin);
    this.eSq = ellipsoid.getEccentricitySquared();
    this.sqrt1MinusESq = Math.sqrt(1 - this.eSq);

    this.ePow4 = this.eSq * this.eSq;
    this.ePow6 = this.ePow4 * this.eSq;
    this.mo = m(φ0);
    this.ePrimeSq = this.eSq / (1 - this.eSq);
    this.ePrimeSqTimes9 = 9 * this.ePrimeSq;
    this.ePrimeSqTimes8 = 8 * this.ePrimeSq;

    final double e1 = (1 - this.sqrt1MinusESq) / (1 + this.sqrt1MinusESq);
    final double e1Pow2 = e1 * e1;
    final double e1Pow3 = e1Pow2 * e1;
    final double e1Pow4 = e1Pow2 * e1Pow2;
    this.e1Time2Div2MinusE1Pow3Times27Div32 = e1 * 3 / 2 - e1Pow3 * 27 / 32;
    this.e1Pow2Times21Div16MinusE1Pow4Times55Div32 = e1Pow2 * 21 / 16 - e1Pow4 * 55 / 32;
    this.e1Pow3Times151Div96 = 151 * e1Pow3 / 96;
    this.e1Pow4Times1097Div512 = 1097 * e1Pow4 / 512;
    this.aTimes1MinusEsqDiv4MinesEPow4Times3Div64MinusEPow6Times5Div256 = this.a
      * (1 - this.eSq / 4 - this.ePow4 * 3 / 64 - this.ePow6 * 5 / 256);

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
   * M1 = MO + (y – yo) / ko
   * T1 = tanϕ1 &circ; 2
   * C1 = e' &circ; 2 * cosϕ1 &circ; 2
   * e' &circ; 2 = e &circ; 2 / (1 – e &circ; 2)
   * D = (x – xo) / (ν1 * kO)
   * </pre>
   * @param from The ordinates to convert.
   * @param to The ordinates to write the converted ordinates to.
   */
  @Override
  public void inverse(final double x, final double y, final double[] targetCoordinates,
    final int targetOffset) {
    final double eSq = this.eSq;
    final double a = this.a;
    final double ko = this.ko;
    final double ePrimeSq = this.ePrimeSq;

    final double m = this.mo + (y - this.yo) / ko;
    final double mu = m / this.aTimes1MinusEsqDiv4MinesEPow4Times3Div64MinusEPow6Times5Div256;
    final double φ1 = mu + this.e1Time2Div2MinusE1Pow3Times27Div32 * Math.sin(2 * mu)
      + this.e1Pow2Times21Div16MinusE1Pow4Times55Div32 * Math.sin(4 * mu)
      + this.e1Pow3Times151Div96 * Math.sin(6 * mu) + this.e1Pow4Times1097Div512 * Math.sin(8 * mu);
    final double cosφ1 = Math.cos(φ1);
    final double sinφ = Math.sin(φ1);
    final double tanφ1 = Math.tan(φ1);

    final double oneMinusESqSinφ1Sq = 1 - eSq * sinφ * sinφ;
    final double nu1 = a / Math.sqrt(oneMinusESqSinφ1Sq);
    final double rho1 = a * (1 - eSq) / (oneMinusESqSinφ1Sq * Math.sqrt(oneMinusESqSinφ1Sq));
    final double c1 = ePrimeSq * cosφ1 * cosφ1;
    final double d = (x - this.xo) / (nu1 * ko);
    final double d2 = d * d;
    final double d3 = d2 * d;
    final double d4 = d2 * d2;
    final double d5 = d4 * d;
    final double d6 = d4 * d2;
    final double t1 = tanφ1 * tanφ1;

    final double c1Sq = c1 * c1;
    final double t1Sq = t1 * t1;
    final double φ = φ1 - nu1 * tanφ1 / rho1
      * (d2 / 2 - (5 + 3 * t1 + 10 * c1 - 4 * c1Sq - this.ePrimeSqTimes9) * d4 / 24
        + (61 + 90 * t1 + 298 * c1 + 45 * t1Sq - 252 * ePrimeSq - 3 * c1Sq) * d6 / 720);

    final double λ = this.λo + (d - (1 + 2 * t1 + c1) * d3 / 6
      + (5 - 2 * c1 + 28 * t1 - 3 * c1Sq + this.ePrimeSqTimes8 + 24 * t1Sq) * d5 / 120) / cosφ1;

    targetCoordinates[targetOffset] = Math.toDegrees(λ);
    targetCoordinates[targetOffset + 1] = Math.toDegrees(φ);
  }

  /**
   * Calculate the value of m for the given value of φ using the following
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
   * @param φ The φ value in radians.
   * @return The value of m.
   */
  private double m(final double φ) {
    return this.a * ((1 - this.eSq / 4 - 3 * this.ePow4 / 64 - 5 * this.ePow6 / 256) * φ
      - (3 * this.eSq / 8 + 3 * this.ePow4 / 32 + 45 * this.ePow6 / 1024) * Math.sin(2 * φ)
      + (15 * this.ePow4 / 256 + 45 * this.ePow6 / 1024) * Math.sin(4 * φ)
      - 35 * this.ePow6 / 3072 * Math.sin(6 * φ));
  }

  /**
   * Project the lon/lat ordinates in degrees to projected coordinates in
   * metres.
   *
   * <pre>
   * x = xo + kO * ν * [
   *   A + (1 – T + C) * A &circ; 3 / 6 +
   *   (5 – 18 * T + T &circ; 2 + 72 *C – 58 *e' &circ; 2 ) * A &circ; 5 / 120
   * ]
   * y = yo + kO * { M – MO + ν * tanϕ * [
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
   * @param from The ordinates to convert.
   * @param to The ordinates to write the converted ordinates to.
   */
  @Override
  public void project(final double lon, final double lat, final double[] targetCoordinates,
    final int targetOffset) {
    final double λ = Math.toRadians(lon);
    final double φ = Math.toRadians(lat);

    final double cosφ = Math.cos(φ);
    final double sinφ = Math.sin(φ);
    final double tanφ = Math.tan(φ);

    final double nu = this.a / Math.sqrt(1 - this.eSq * sinφ * sinφ);
    final double tanφPow2 = tanφ * tanφ;
    final double tanφPow4 = tanφPow2 * tanφPow2;
    final double c = this.ePrimeSq * cosφ * cosφ;
    final double cPow2 = c * c;
    final double a1 = (λ - this.λo) * cosφ;
    final double a1Pow2 = a1 * a1;
    final double a1Pow3 = a1Pow2 * a1;
    final double a1Pow4 = a1Pow2 * a1Pow2;
    final double a1Pow5 = a1Pow4 * a1;
    final double a1Pow6 = a1Pow4 * a1Pow2;
    final double x = this.xo + this.ko * nu * (a1 + (1 - tanφPow2 + c) * a1Pow3 / 6
      + (5 - 18 * tanφPow2 + tanφPow4 + 72 * c - 58 * this.ePrimeSq) * a1Pow5 / 120);

    final double m = m(φ);
    final double y = this.yo + this.ko
      * (m - this.mo + nu * tanφ * (a1Pow2 / 2 + (5 - tanφPow2 + 9 * c + 4 * cPow2) * a1Pow4 / 24
        + (61 - 58 * tanφPow2 + tanφPow4 + 600 * c - 330 * this.ePrimeSq) * a1Pow6 / 720));
    targetCoordinates[targetOffset] = x;
    targetCoordinates[targetOffset + 1] = y;
  }
}
