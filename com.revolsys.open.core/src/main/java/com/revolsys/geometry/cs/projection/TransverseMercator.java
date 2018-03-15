package com.revolsys.geometry.cs.projection;

import com.revolsys.geometry.cs.Ellipsoid;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.NormalizedParameterNames;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.datum.GeodeticDatum;
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
public class TransverseMercator extends AbstractCoordinatesProjection {

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

  /** The coordinate system providing the parameters for the projection. */
  private final ProjectedCoordinateSystem coordinateSystem;

  /** Scale Factor. */
  private final double ko;

  /** False Easting. */
  private final double xo;

  /** False Northing. */
  private final double yo;

  private final double mo;

  private final double n;

  private final double B;

  private final double h1;

  private final double h2;

  private final double h3;

  private final double h4;

  private final double h1Prime;

  private final double h2Prime;

  private final double h3Prime;

  private final double h4Prime;

  private final double e;

  private final double λo;

  /**
   * Construct a new TransverseMercator projection.
   *
   * @param coordinateSystem The coordinate system.
   */
  public TransverseMercator(final ProjectedCoordinateSystem coordinateSystem) {
    this.coordinateSystem = coordinateSystem;
    final GeographicCoordinateSystem geographicCS = coordinateSystem
      .getGeographicCoordinateSystem();
    final GeodeticDatum geodeticDatum = geographicCS.getDatum();
    this.xo = coordinateSystem.getDoubleParameter(NormalizedParameterNames.FALSE_EASTING);
    this.yo = coordinateSystem.getDoubleParameter(NormalizedParameterNames.FALSE_NORTHING);
    this.λo = Math
      .toRadians(coordinateSystem.getDoubleParameter(NormalizedParameterNames.CENTRAL_MERIDIAN));
    final double φo = Math
      .toRadians(coordinateSystem.getDoubleParameter(NormalizedParameterNames.LATITUDE_OF_ORIGIN));
    this.ko = coordinateSystem.getDoubleParameter(NormalizedParameterNames.SCALE_FACTOR);

    final Ellipsoid ellipsoid = geodeticDatum.getEllipsoid();
    final double a = ellipsoid.getSemiMajorAxis();
    final double f = ellipsoid.getFlattening();
    this.e = ellipsoid.getEccentricity();
    this.mo = mo(φo);
    this.n = f / (2 - f);
    final double n2 = this.n * this.n;
    final double n3 = this.n * n2;
    final double n4 = n2 * n2;
    this.B = a / (1 + this.n) * (1 + n2 / 4 + n4 / 64);
    this.h1 = this.n / 2 - 2 / 3 * n2 + 5 / 16 * n3 + 41 / 180 * n4;
    this.h2 = 13 / 48 * n2 - 3 / 5 * n3 + 557 / 1440 * n4;
    this.h3 = 61 / 240 * n3 - 103 / 140 * n4;
    this.h4 = 49561 / 161280 * n4;

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
    final double λ = Math.toRadians(lon);
    final double φ = Math.toRadians(lat);

    final double Q = FastMath.asinh(Math.tan(φ)) - this.e * FastMath.atanh(this.e * Math.sin(φ));
    final double β = Math.atan(Math.sinh(Q));
    final double η0 = FastMath.atanh(Math.cos(β) * Math.sin(λ - this.λo));
    final double ξ0 = Math.asin(Math.sin(β) * Math.cosh(η0));
    final double ξ1 = this.h1 * Math.sin(2 * ξ0) * Math.cosh(2 * η0);
    final double η1 = this.h1 * Math.cos(2 * ξ0) * Math.sinh(2 * η0);
    final double ξ2 = this.h2 * Math.sin(4 * ξ0) * Math.cosh(4 * η0);
    final double η2 = this.h2 * Math.cos(4 * ξ0) * Math.sinh(4 * η0);
    final double ξ3 = this.h3 * Math.sin(6 * ξ0) * Math.cosh(6 * η0);
    final double η3 = this.h3 * Math.cos(6 * ξ0) * Math.sinh(6 * η0);
    final double ξ4 = this.h4 * Math.sin(8 * ξ0) * Math.cosh(8 * η0);
    final double η4 = this.h4 * Math.cos(8 * ξ0) * Math.sinh(8 * η0);
    final double ξ = ξ0 + ξ1 + ξ2 + ξ3 + ξ4;
    final double η = η0 + η1 + η2 + η3 + η4;

    final double x = this.xo + this.ko * this.B * η;
    final double y = this.yo + this.ko * (this.B * ξ - this.mo);
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
    return this.coordinateSystem.getCoordinateSystemName();
  }
}
