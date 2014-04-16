package com.revolsys.jts.geom;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.jts.LineSegment;

public interface BoundingBox {

  /**
   * Computes the coordinate of the centre of this envelope (as long as it is non-null
   *
   * @return the centre coordinate of this envelope
   * <code>null</code> if the envelope is null
   */
  Coordinates centre();

  BoundingBox clipToCoordinateSystem();

  /**
   * Tests if the <code>BoundingBox other</code>
   * lies wholely inside this <code>BoundingBox</code> (inclusive of the boundary).
   * <p>
   * Note that this is <b>not</b> the same definition as the SFS <tt>contains</tt>,
   * which would exclude the envelope boundary.
   *
   *@param  other the <code>BoundingBox</code> to check
   *@return true if <code>other</code> is contained in this <code>BoundingBox</code>
   *
   *@see #covers(BoundingBox)
   */
  boolean contains(BoundingBox other);

  /**
   * Tests if the given point lies in or on the envelope.
   * <p>
   * Note that this is <b>not</b> the same definition as the SFS <tt>contains</tt>,
   * which would exclude the envelope boundary.
   *
   *@param  p  the point which this <code>BoundingBox</code> is
   *      being checked for containing
   *@return    <code>true</code> if the point lies in the interior or
   *      on the boundary of this <code>BoundingBox</code>.
   *      
   *@see #covers(Coordinates)
   */
  boolean contains(Coordinates p);

  /**
   * Tests if the given point lies in or on the envelope.
   * <p>
   * Note that this is <b>not</b> the same definition as the SFS <tt>contains</tt>,
   * which would exclude the envelope boundary.
   *
   *@param  x  the x-coordinate of the point which this <code>BoundingBox</code> is
   *      being checked for containing
   *@param  y  the y-coordinate of the point which this <code>BoundingBox</code> is
   *      being checked for containing
   *@return    <code>true</code> if <code>(x, y)</code> lies in the interior or
   *      on the boundary of this <code>BoundingBox</code>.
   *      
   *@see #covers(double, double)
   */
  boolean contains(double x, double y);

  boolean contains(Geometry geometry);

  boolean contains(Point geometry);

  BoundingBox convert(GeometryFactory geometryFactory);

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
   *@param  p  the point which this <code>BoundingBox</code> is
   *      being checked for containing
   *@return    <code>true</code> if the point lies in the interior or
   *      on the boundary of this <code>BoundingBox</code>.
   */
  boolean covers(Coordinates p);

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

  /**
   * Computes the distance between this and another
   * <code>BoundingBox</code>.
   * The distance between overlapping BoundingBoxs is 0.  Otherwise, the
   * distance is the Euclidean distance between the closest points.
   */
  double distance(BoundingBox env);

  double distance(Geometry point);

  BoundingBox expand(Coordinates point);

  BoundingBox expand(double maxDistance);

  BoundingBox expand(final double deltaX, final double deltaY);

  BoundingBox expandPercent(double d);

  BoundingBox expandPercent(final double factorX, final double factorY);

  BoundingBox expandToInclude(BoundingBox boundingBox);

  BoundingBox expandToInclude(DataObject record);

  BoundingBox expandToInclude(Geometry geometry);

  /**
   * Gets the area of this envelope.
   * 
   * @return the area of the envelope
   * @return 0.0 if the envelope is null
   */
  double getArea();

  double getAspectRatio();

  Coordinates getCentre();

  Point getCentrePoint();

  double getCentreX();

  double getCentreY();

  CoordinateSystem getCoordinateSystem();

  Coordinates getCornerPoint(int i);

  CoordinatesList getCornerPoints();

  LineSegment getEastLine();

  GeometryFactory getGeometryFactory();

  /**
   *  Returns the difference between the maximum and minimum y values.
   *
   *@return    max y - min y, or 0 if this is a null <code>BoundingBox</code>
   */
  double getHeight();

  Measure<Length> getHeightLength();

  String getId();

  <Q extends Quantity> Measurable<Q> getMaximumX();

  <Q extends Quantity> Measurable<Q> getMaximumY();

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

  double getMaxZ();

  <Q extends Quantity> Measurable<Q> getMinimumX();

  <Q extends Quantity> Measurable<Q> getMinimumY();

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

  double getMinZ();

  LineSegment getNorthLine();

  LineSegment getSouthLine();

  int getSrid();

  Point getTopLeftPoint();

  LineSegment getWestLine();

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
   *  Check if the point <code>p</code>
   *  overlaps (lies inside) the region of this <code>BoundingBox</code>.
   *
   *@param  p  the <code>Coordinate</code> to be tested
   *@return        <code>true</code> if the point overlaps this <code>BoundingBox</code>
   */
  boolean intersects(Coordinates p);

  /**
   *  Check if the point <code>(x, y)</code>
   *  overlaps (lies inside) the region of this <code>BoundingBox</code>.
   *
   *@param  x  the x-ordinate of the point
   *@param  y  the y-ordinate of the point
   *@return        <code>true</code> if the point overlaps this <code>BoundingBox</code>
   */
  boolean intersects(double x, double y);

  boolean isEmpty();

  /**
   *  Returns <code>true</code> if this <code>BoundingBox</code> is a "null"
   *  envelope.
   *
   *@return    <code>true</code> if this <code>BoundingBox</code> is uninitialized
   *      or is the envelope of the empty geometry.
   */
  boolean isNull();

  /**
   * Gets the maximum extent of this envelope across both dimensions.
   * 
   * @return the maximum extent of this envelope
   */
  double maxExtent();

  /**
   * Gets the minimum extent of this envelope across both dimensions.
   * 
   * @return the minimum extent of this envelope
   */
  double minExtent();

  BoundingBox move(double deltaX, double deltaY);

  Geometry toGeometry();

  Polygon toPolygon();

  Polygon toPolygon(GeometryFactory geometryFactory);

  Polygon toPolygon(GeometryFactory factory, int numPoints);

  Polygon toPolygon(GeometryFactory geometryFactory, int numX, int numY);

  Polygon toPolygon(int numSegments);

  Polygon toPolygon(int numX, int numY);

}
