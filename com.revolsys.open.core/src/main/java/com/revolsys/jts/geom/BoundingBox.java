package com.revolsys.jts.geom;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import com.revolsys.data.record.Record;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;

public interface BoundingBox {

  BoundingBox EMPTY = new BoundingBoxDoubleGf();

  BoundingBox clipToCoordinateSystem();

  BoundingBox convert(GeometryFactory geometryFactory);

  BoundingBox convert(GeometryFactory geometryFactory, int axisCount);

  boolean coveredBy(double... bounds);

  /**
   * Tests if the <code>BoundingBox other</code>
   * lies wholely inside this <code>BoundingBox</code> (inclusive of the boundary).
   *
   *@param  other the <code>BoundingBox</code> to check
   *@return true if this <code>BoundingBox</code> covers the <code>other</code>
   */
  boolean covers(BoundingBox other);

  /**
   * Tests if the given point lies in or on the envelope.
   *
   *@param  x  the x-coordinate of the point which this <code>BoundingBox</code> is
   *      being checked for containing
   *@param  y  the y-coordinate of the point which this <code>BoundingBox</code> is
   *      being checked for containing
   *@return    <code>true</code> if <code>(x, y)</code> lies in the interior or
   *      on the boundary of this <code>BoundingBox</code>.
   */
  boolean covers(double x, double y);

  boolean covers(Geometry geometry);

  /**
   * Tests if the given point lies in or on the envelope.
   *
   *@param  p  the point which this <code>BoundingBox</code> is
   *      being checked for containing
   *@return    <code>true</code> if the point lies in the interior or
   *      on the boundary of this <code>BoundingBox</code>.
   */
  boolean covers(Point point);

  /**
   * Computes the distance between this and another
   * <code>BoundingBox</code>.
   * The distance between overlapping BoundingBoxs is 0.  Otherwise, the
   * distance is the Euclidean distance between the closest points.
   */
  double distance(BoundingBox env);

  double distance(Geometry point);

  double distance(Point point);

  BoundingBox expand(double maxDistance);

  BoundingBox expand(final double deltaX, final double deltaY);

  BoundingBox expand(Point point);

  BoundingBox expandPercent(double d);

  BoundingBox expandPercent(final double factorX, final double factorY);

  BoundingBox expandToInclude(BoundingBox boundingBox);

  BoundingBox expandToInclude(double... coordinates);

  BoundingBox expandToInclude(Geometry geometry);

  BoundingBox expandToInclude(Record record);

  /**
   * Gets the area of this envelope.
   *
   * @return the area of the envelope
   * @return 0.0 if the envelope is null
   */
  double getArea();

  double getAspectRatio();

  int getAxisCount();

  double[] getBounds();

  double[] getBounds(int axisCount);

  Point getCentre();

  double getCentreX();

  double getCentreY();

  CoordinateSystem getCoordinateSystem();

  /**
   * maxX,minY
   * minX,minY
   * minX,maxY
   * maxX,maxY
   */
  Point getCornerPoint(int i);

  LineString getCornerPoints();

  GeometryFactory getGeometryFactory();

  /**
   *  Returns the difference between the maximum and minimum y values.
   *
   *@return    max y - min y, or 0 if this is a null <code>BoundingBox</code>
   */
  double getHeight();

  Measure<Length> getHeightLength();

  double getMax(int i);

  <Q extends Quantity> Measurable<Q> getMaximum(int axisIndex);

  @SuppressWarnings("rawtypes")
  <Q extends Quantity> double getMaximum(int axisIndex, final Unit convertUnit);

  /**
   *  Returns the <code>BoundingBox</code>s maximum x-value. min x > max x
   *  indicates that this is a null <code>BoundingBox</code>.
   *
   *@return    the maximum x-coordinate
   */
  double getMaxX();

  /**
   *  Returns the <code>BoundingBox</code>s maximum y-value. min y > max y
   *  indicates that this is a null <code>BoundingBox</code>.
   *
   *@return    the maximum y-coordinate
   */
  double getMaxY();

  double getMin(int i);

  <Q extends Quantity> Measurable<Q> getMinimum(int axisIndex);

  @SuppressWarnings("rawtypes")
  <Q extends Quantity> double getMinimum(int axisIndex, final Unit convertUnit);

  /**
   *  Returns the <code>BoundingBox</code>s minimum x-value. min x > max x
   *  indicates that this is a null <code>BoundingBox</code>.
   *
   *@return    the minimum x-coordinate
   */
  double getMinX();

  /**
   *  Returns the <code>BoundingBox</code>s minimum y-value. min y > max y
   *  indicates that this is a null <code>BoundingBox</code>.
   *
   *@return    the minimum y-coordinate
   */
  double getMinY();

  Point getRandomPointWithin();

  int getSrid();

  Point getTopLeftPoint();

  /**
   *  Returns the difference between the maximum and minimum x values.
   *
   *@return    max x - min x, or 0 if this is a null <code>BoundingBox</code>
   */
  double getWidth();

  Measure<Length> getWidthLength();

  /**
   * Computes the intersection of two {@link BoundingBox}s.
   *
   * @param env the envelope to intersect with
   * @return a new BoundingBox representing the intersection of the envelopes (this will be
   * the null envelope if either argument is null, or they do not intersect
   */
  BoundingBox intersection(BoundingBox env);

  /**
   *  Check if the region defined by <code>other</code>
   *  overlaps (intersects) the region of this <code>BoundingBox</code>.
   *
   *@param  other  the <code>BoundingBox</code> which this <code>BoundingBox</code> is
   *          being checked for overlapping
   *@return        <code>true</code> if the <code>BoundingBox</code>s overlap
   */
  boolean intersects(BoundingBox other);

  /**
   *  Check if the point <code>(x, y)</code>
   *  overlaps (lies inside) the region of this <code>BoundingBox</code>.
   *
   *@param  x  the x-ordinate of the point
   *@param  y  the y-ordinate of the point
   *@return        <code>true</code> if the point overlaps this <code>BoundingBox</code>
   */
  boolean intersects(double x, double y);

  /**
   *  Check if the line <code>(x1, y1) -> (x2, y2)</code>
   *  intersects (covered by or crosses) the region of this <code>BoundingBox</code>.
   *
   *@return        <code>true</code> if the line overlaps this <code>BoundingBox</code>
   */
  boolean intersects(double x1, double y1, double x2, double y2);

  /**
   *  Check if the point <code>p</code>
   *  overlaps (lies inside) the region of this <code>BoundingBox</code>.
   *
   *@param  p  the <code>Coordinate</code> to be tested
   *@return        <code>true</code> if the point overlaps this <code>BoundingBox</code>
   */
  boolean intersects(Point p);

  boolean isEmpty();

  boolean isWithinDistance(BoundingBox boundingBox, double maxDistance);

  boolean isWithinDistance(Geometry geometry, double maxDistance);

  BoundingBox move(double deltaX, double deltaY);

  /**
   * Creates a {@link Geometry} with the same extent as the given envelope.
   * The Geometry returned is guaranteed to be valid.
   * To provide this behaviour, the following cases occur:
   * <p>
   * If the <code>BoundingBoxDoubleGf</code> is:
   * <ul>
   * <li>null : returns an empty {@link Point}
   * <li>a point : returns a non-empty {@link Point}
   * <li>a line : returns a two-point {@link LineString}
   * <li>a rectangle : returns a {@link Polygon}> whose points are (minx, miny),
   *  (minx, maxy), (maxx, maxy), (maxx, miny), (minx, miny).
   * </ul>
   *
   *@param  envelope the <code>BoundingBoxDoubleGf</code> to convert
   *@return an empty <code>Point</code> (for null <code>BoundingBoxDoubleGf</code>s),
   *  a <code>Point</code> (when min x = max x and min y = max y) or a
   *      <code>Polygon</code> (in all other cases)
   */

  Geometry toGeometry();

  Polygon toPolygon();

  Polygon toPolygon(GeometryFactory geometryFactory);

  Polygon toPolygon(GeometryFactory factory, int numPoints);

  Polygon toPolygon(GeometryFactory geometryFactory, int numX, int numY);

  Polygon toPolygon(int numSegments);

  Polygon toPolygon(int numX, int numY);

}
