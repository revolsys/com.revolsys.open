package com.revolsys.gis.model.geometry.operation.overlay;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.geometry.Geometry;
import com.vividsolutions.jts.precision.CommonBits;

/**
 * Removes common most-significant mantissa bits from one or more
 * {@link Geometry}s.
 * <p>
 * The CommonBitsRemover "scavenges" precision which is "wasted" by a large
 * displacement of the geometry from the origin. For example, if a small
 * geometry is displaced from the origin by a large distance, the displacement
 * increases the significant figures in the coordinates, but does not affect the
 * <i>relative</i> topology of the geometry. Thus the geometry can be translated
 * back to the origin without affecting its topology. In order to compute the
 * translation without affecting the full precision of the coordinate values,
 * the translation is performed at the bit level by removing the common leading
 * mantissa bits.
 * <p>
 * If the geometry envelope already contains the origin, the translation
 * procedure cannot be applied. In this case, the common bits value is computed
 * as zero.
 * <p>
 * If the geometry crosses the Y axis but not the X axis (and <i>mutatis
 * mutandum</i>), the common bits for Y are zero, but the common bits for X are
 * non-zero.
 * 
 * @version 1.7
 */
public class CommonBitsRemover {
  private Coordinates commonCoord;

  private final CommonBits commonBitsX = new CommonBits();

  private final CommonBits commonBitsY = new CommonBits();

  public CommonBitsRemover() {
  }

  /**
   * Add a geometry to the set of geometries whose common bits are being
   * computed. After this method has executed the common coordinate reflects the
   * common bits of all added geometries.
   * 
   * @param geom a Geometry to test for common bits
   */
  public void add(final Geometry geom) {
    for (final CoordinatesList points : geom.getCoordinatesLists()) {
      for (int i = 0; i < points.size(); i++) {
        final double x = points.getX(i);
        commonBitsX.add(x);
        final double y = points.getY(i);
        commonBitsY.add(y);
      }
    }
    commonCoord = new DoubleCoordinates(commonBitsX.getCommon(),
      commonBitsY.getCommon());
  }

  /**
   * Adds the common coordinate bits back into a Geometry. The coordinates of
   * the Geometry are changed.
   * 
   * @param geom the Geometry to which to add the common coordinate bits
   */
  public Geometry addCommonBits(final Geometry geom) {
    final double deltaX = commonBitsX.getCommon();
    final double deltaY = commonBitsY.getCommon();
    return translate(geom, deltaX, deltaY);
  }

  public Coordinates getCommonCoordinate() {
    return commonCoord;
  }

  /**
   * Removes the common coordinate bits from a Geometry. The coordinates of the
   * Geometry are changed.
   * 
   * @param geom the Geometry from which to remove the common coordinate bits
   * @return the shifted Geometry
   */
  public Geometry removeCommonBits(final Geometry geom) {
    final double deltaX = commonBitsX.getCommon();
    final double deltaY = commonBitsY.getCommon();
    return translate(geom, -deltaX, -deltaY);
  }

  protected Geometry translate(final Geometry geometry, final double deltaX,
    final double deltaY) {
    if (deltaX != 0.0 && deltaY != 0.0) {
      for (final CoordinatesList points : geometry.getCoordinatesLists()) {
        for (int i = 0; i < points.size(); i++) {
          final double x = points.getX(i);
          points.setX(i, x + deltaX);
          final double y = points.getY(i);
          points.setY(i, y + deltaY);
        }
      }
    }
    return geometry;
  }

}
