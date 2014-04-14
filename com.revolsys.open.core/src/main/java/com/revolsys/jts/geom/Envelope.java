/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.jts.geom;

import java.io.Serializable;

import com.revolsys.gis.model.coordinates.CoordinatesUtil;

/**
 *  Defines a rectangular region of the 2D coordinate plane.
 *  It is often used to represent the bounding box of a {@link Geometry},
 *  e.g. the minimum and maximum x and y values of the {@link Coordinates}s.
 *  <p>
 *  Note that Envelopes support infinite or half-infinite regions, by using the values of
 *  <code>Double.POSITIVE_INFINITY</code> and <code>Double.NEGATIVE_INFINITY</code>.
 *  <p>
 *  When Envelope objects are created or initialized,
 *  the supplies extent values are automatically sorted into the correct order.
 *
 *@version 1.7
 */
public class Envelope implements Serializable {
  private static final long serialVersionUID = 5873921885273102420L;

  /**
   * Test the point q to see whether it intersects the Envelope defined by p1-p2
   * @param p1 one extremal point of the envelope
   * @param p2 another extremal point of the envelope
   * @param q the point to test for intersection
   * @return <code>true</code> if q intersects the envelope p1-p2
   */
  public static boolean intersects(final Coordinates p1, final Coordinates p2,
    final Coordinates q) {
    // OptimizeIt shows that Math#min and Math#max here are a bottleneck.
    // Replace with direct comparisons. [Jon Aquino]
    if (((q.getX() >= (p1.getX() < p2.getX() ? p1.getX() : p2.getX())) && (q.getX() <= (p1.getX() > p2.getX() ? p1.getX()
      : p2.getX())))
      && ((q.getY() >= (p1.getY() < p2.getY() ? p1.getY() : p2.getY())) && (q.getY() <= (p1.getY() > p2.getY() ? p1.getY()
        : p2.getY())))) {
      return true;
    }
    return false;
  }

  /**
   * Tests whether the envelope defined by p1-p2
   * and the envelope defined by q1-q2
   * intersect.
   * 
   * @param p1 one extremal point of the envelope P
   * @param p2 another extremal point of the envelope P
   * @param q1 one extremal point of the envelope Q
   * @param q2 another extremal point of the envelope Q
   * @return <code>true</code> if Q intersects P
   */
  public static boolean intersects(final Coordinates p1, final Coordinates p2,
    final Coordinates q1, final Coordinates q2) {
    double minq = Math.min(q1.getX(), q2.getX());
    double maxq = Math.max(q1.getX(), q2.getX());
    double minp = Math.min(p1.getX(), p2.getX());
    double maxp = Math.max(p1.getX(), p2.getX());

    if (minp > maxq) {
      return false;
    }
    if (maxp < minq) {
      return false;
    }

    minq = Math.min(q1.getY(), q2.getY());
    maxq = Math.max(q1.getY(), q2.getY());
    minp = Math.min(p1.getY(), p2.getY());
    maxp = Math.max(p1.getY(), p2.getY());

    if (minp > maxq) {
      return false;
    }
    if (maxp < minq) {
      return false;
    }
    return true;
  }

  /**
   *  the minimum x-coordinate
   */
  private double minx;

  /**
   *  the maximum x-coordinate
   */
  private double maxx;

  /**
   *  the minimum y-coordinate
   */
  private double miny;

  /**
   *  the maximum y-coordinate
   */
  private double maxy;

  /**
   *  Creates a null <code>Envelope</code>.
   */
  public Envelope() {
    init();
  }

  /**
   *  Creates an <code>Envelope</code> for a region defined by a single Coordinate.
   *
   *@param  p  the Coordinate
   */
  public Envelope(final Coordinates p) {
    init(p.getX(), p.getX(), p.getY(), p.getY());
  }

  /**
   *  Creates an <code>Envelope</code> for a region defined by two Coordinates.
   *
   *@param  p1  the first Coordinate
   *@param  p2  the second Coordinate
   */
  public Envelope(final Coordinates p1, final Coordinates p2) {
    init(p1.getX(), p2.getX(), p1.getY(), p2.getY());
  }

  public Envelope(final double x, final double y) {
    this(x, x, y, y);
  }

  /**
   *  Creates an <code>Envelope</code> for a region defined by maximum and minimum values.
   *
   *@param  x1  the first x-value
   *@param  x2  the second x-value
   *@param  y1  the first y-value
   *@param  y2  the second y-value
   */
  public Envelope(final double x1, final double x2, final double y1,
    final double y2) {
    init(x1, x2, y1, y2);
  }

  /**
   *  Create an <code>Envelope</code> from an existing Envelope.
   *
   *@param  env  the Envelope to initialize from
   */
  public Envelope(final Envelope env) {
    init(env);
  }

  /**
   * Computes the coordinate of the centre of this envelope (as long as it is non-null
   *
   * @return the centre coordinate of this envelope
   * <code>null</code> if the envelope is null
   */
  public Coordinates centre() {
    if (isNull()) {
      return null;
    }
    return new Coordinate((getMinX() + getMaxX()) / 2.0,
      (getMinY() + getMaxY()) / 2.0, Coordinates.NULL_ORDINATE);
  }

  /**
   * Tests if the given point lies in or on the envelope.
   * <p>
   * Note that this is <b>not</b> the same definition as the SFS <tt>contains</tt>,
   * which would exclude the envelope boundary.
   *
   *@param  p  the point which this <code>Envelope</code> is
   *      being checked for containing
   *@return    <code>true</code> if the point lies in the interior or
   *      on the boundary of this <code>Envelope</code>.
   *      
   *@see #covers(Coordinates)
   */
  public boolean contains(final Coordinates p) {
    return covers(p);
  }

  /**
   * Tests if the given point lies in or on the envelope.
   * <p>
   * Note that this is <b>not</b> the same definition as the SFS <tt>contains</tt>,
   * which would exclude the envelope boundary.
   *
   *@param  x  the x-coordinate of the point which this <code>Envelope</code> is
   *      being checked for containing
   *@param  y  the y-coordinate of the point which this <code>Envelope</code> is
   *      being checked for containing
   *@return    <code>true</code> if <code>(x, y)</code> lies in the interior or
   *      on the boundary of this <code>Envelope</code>.
   *      
   *@see #covers(double, double)
   */
  public boolean contains(final double x, final double y) {
    return covers(x, y);
  }

  /**
   * Tests if the <code>Envelope other</code>
   * lies wholely inside this <code>Envelope</code> (inclusive of the boundary).
   * <p>
   * Note that this is <b>not</b> the same definition as the SFS <tt>contains</tt>,
   * which would exclude the envelope boundary.
   *
   *@param  other the <code>Envelope</code> to check
   *@return true if <code>other</code> is contained in this <code>Envelope</code>
   *
   *@see #covers(Envelope)
   */
  public boolean contains(final Envelope other) {
    return covers(other);
  }

  /**
   * Tests if the given point lies in or on the envelope.
   *
   *@param  p  the point which this <code>Envelope</code> is
   *      being checked for containing
   *@return    <code>true</code> if the point lies in the interior or
   *      on the boundary of this <code>Envelope</code>.
   */
  public boolean covers(final Coordinates p) {
    return covers(p.getX(), p.getY());
  }

  /**
   * Tests if the given point lies in or on the envelope.
   *
   *@param  x  the x-coordinate of the point which this <code>Envelope</code> is
   *      being checked for containing
   *@param  y  the y-coordinate of the point which this <code>Envelope</code> is
   *      being checked for containing
   *@return    <code>true</code> if <code>(x, y)</code> lies in the interior or
   *      on the boundary of this <code>Envelope</code>.
   */
  public boolean covers(final double x, final double y) {
    if (isNull()) {
      return false;
    }
    return x >= minx && x <= maxx && y >= miny && y <= maxy;
  }

  /**
   * Tests if the <code>Envelope other</code>
   * lies wholely inside this <code>Envelope</code> (inclusive of the boundary).
   *
   *@param  other the <code>Envelope</code> to check
   *@return true if this <code>Envelope</code> covers the <code>other</code> 
   */
  public boolean covers(final Envelope other) {
    if (isNull() || other.isNull()) {
      return false;
    }
    return other.getMinX() >= minx && other.getMaxX() <= maxx
      && other.getMinY() >= miny && other.getMaxY() <= maxy;
  }

  /**
   * Computes the distance between this and another
   * <code>Envelope</code>.
   * The distance between overlapping Envelopes is 0.  Otherwise, the
   * distance is the Euclidean distance between the closest points.
   */
  public double distance(final Envelope env) {
    if (intersects(env)) {
      return 0;
    }

    double dx = 0.0;
    if (maxx < env.minx) {
      dx = env.minx - maxx;
    } else if (minx > env.maxx) {
      dx = minx - env.maxx;
    }

    double dy = 0.0;
    if (maxy < env.miny) {
      dy = env.miny - maxy;
    } else if (miny > env.maxy) {
      dy = miny - env.maxy;
    }

    // if either is zero, the envelopes overlap either vertically or
    // horizontally
    if (dx == 0.0) {
      return dy;
    }
    if (dy == 0.0) {
      return dx;
    }
    return Math.sqrt(dx * dx + dy * dy);
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof Envelope)) {
      return false;
    }
    final Envelope otherEnvelope = (Envelope)other;
    if (isNull()) {
      return otherEnvelope.isNull();
    }
    return maxx == otherEnvelope.getMaxX() && maxy == otherEnvelope.getMaxY()
      && minx == otherEnvelope.getMinX() && miny == otherEnvelope.getMinY();
  }

  /**
   * Expands this envelope by a given distance in all directions.
   * Both positive and negative distances are supported.
   *
   * @param distance the distance to expand the envelope
   */
  public void expandBy(final double distance) {
    expandBy(distance, distance);
  }

  /**
   * Expands this envelope by a given distance in all directions.
   * Both positive and negative distances are supported.
   *
   * @param deltaX the distance to expand the envelope along the the X axis
   * @param deltaY the distance to expand the envelope along the the Y axis
   */
  public void expandBy(final double deltaX, final double deltaY) {
    if (isNull()) {
      return;
    }

    minx -= deltaX;
    maxx += deltaX;
    miny -= deltaY;
    maxy += deltaY;

    // check for envelope disappearing
    if (minx > maxx || miny > maxy) {
      setToNull();
    }
  }

  /**
   *  Enlarges this <code>Envelope</code> so that it contains
   *  the given {@link Coordinates}. 
   *  Has no effect if the point is already on or within the envelope.
   *
   *@param  p  the Coordinates to expand to include
   */
  public void expandToInclude(final Coordinates p) {
    expandToInclude(p.getX(), p.getY());
  }

  /**
   *  Enlarges this <code>Envelope</code> so that it contains
   *  the given point. 
   *  Has no effect if the point is already on or within the envelope.
   *
   *@param  x  the value to lower the minimum x to or to raise the maximum x to
   *@param  y  the value to lower the minimum y to or to raise the maximum y to
   */
  public void expandToInclude(final double x, final double y) {
    if (isNull()) {
      minx = x;
      maxx = x;
      miny = y;
      maxy = y;
    } else {
      if (x < minx) {
        minx = x;
      }
      if (x > maxx) {
        maxx = x;
      }
      if (y < miny) {
        miny = y;
      }
      if (y > maxy) {
        maxy = y;
      }
    }
  }

  /**
   *  Enlarges this <code>Envelope</code> so that it contains
   *  the <code>other</code> Envelope. 
   *  Has no effect if <code>other</code> is wholly on or
   *  within the envelope.
   *
   *@param  other  the <code>Envelope</code> to expand to include
   */
  public void expandToInclude(final Envelope other) {
    if (other.isNull()) {
      return;
    }
    if (isNull()) {
      minx = other.getMinX();
      maxx = other.getMaxX();
      miny = other.getMinY();
      maxy = other.getMaxY();
    } else {
      if (other.minx < minx) {
        minx = other.minx;
      }
      if (other.maxx > maxx) {
        maxx = other.maxx;
      }
      if (other.miny < miny) {
        miny = other.miny;
      }
      if (other.maxy > maxy) {
        maxy = other.maxy;
      }
    }
  }

  /**
   * Gets the area of this envelope.
   * 
   * @return the area of the envelope
   * @return 0.0 if the envelope is null
   */
  public double getArea() {
    return getWidth() * getHeight();
  }

  /**
   *  Returns the difference between the maximum and minimum y values.
   *
   *@return    max y - min y, or 0 if this is a null <code>Envelope</code>
   */
  public double getHeight() {
    if (isNull()) {
      return 0;
    }
    return maxy - miny;
  }

  /**
   *  Returns the <code>Envelope</code>s maximum x-value. min x > max x
   *  indicates that this is a null <code>Envelope</code>.
   *
   *@return    the maximum x-coordinate
   */
  public double getMaxX() {
    return maxx;
  }

  /**
   *  Returns the <code>Envelope</code>s maximum y-value. min y > max y
   *  indicates that this is a null <code>Envelope</code>.
   *
   *@return    the maximum y-coordinate
   */
  public double getMaxY() {
    return maxy;
  }

  /**
   *  Returns the <code>Envelope</code>s minimum x-value. min x > max x
   *  indicates that this is a null <code>Envelope</code>.
   *
   *@return    the minimum x-coordinate
   */
  public double getMinX() {
    return minx;
  }

  /**
   *  Returns the <code>Envelope</code>s minimum y-value. min y > max y
   *  indicates that this is a null <code>Envelope</code>.
   *
   *@return    the minimum y-coordinate
   */
  public double getMinY() {
    return miny;
  }

  /**
   *  Returns the difference between the maximum and minimum x values.
   *
   *@return    max x - min x, or 0 if this is a null <code>Envelope</code>
   */
  public double getWidth() {
    if (isNull()) {
      return 0;
    }
    return maxx - minx;
  }

  @Override
  public int hashCode() {
    // Algorithm from Effective Java by Joshua Bloch [Jon Aquino]
    int result = 17;
    result = 37 * result + CoordinatesUtil.hashCode(minx);
    result = 37 * result + CoordinatesUtil.hashCode(maxx);
    result = 37 * result + CoordinatesUtil.hashCode(miny);
    result = 37 * result + CoordinatesUtil.hashCode(maxy);
    return result;
  }

  /**
   *  Initialize to a null <code>Envelope</code>.
   */
  public void init() {
    setToNull();
  }

  /**
   *  Initialize an <code>Envelope</code> to a region defined by a single Coordinate.
   *
   *@param  p  the coordinate
   */
  public void init(final Coordinates p) {
    init(p.getX(), p.getX(), p.getY(), p.getY());
  }

  /**
   *  Initialize an <code>Envelope</code> to a region defined by two Coordinates.
   *
   *@param  p1  the first Coordinate
   *@param  p2  the second Coordinate
   */
  public void init(final Coordinates p1, final Coordinates p2) {
    init(p1.getX(), p2.getX(), p1.getY(), p2.getY());
  }

  /**
   *  Initialize an <code>Envelope</code> for a region defined by maximum and minimum values.
   *
   *@param  x1  the first x-value
   *@param  x2  the second x-value
   *@param  y1  the first y-value
   *@param  y2  the second y-value
   */
  public void init(final double x1, final double x2, final double y1,
    final double y2) {
    if (x1 < x2) {
      minx = x1;
      maxx = x2;
    } else {
      minx = x2;
      maxx = x1;
    }
    if (y1 < y2) {
      miny = y1;
      maxy = y2;
    } else {
      miny = y2;
      maxy = y1;
    }
  }

  /**
   *  Initialize an <code>Envelope</code> from an existing Envelope.
   *
   *@param  env  the Envelope to initialize from
   */
  public void init(final Envelope env) {
    this.minx = env.minx;
    this.maxx = env.maxx;
    this.miny = env.miny;
    this.maxy = env.maxy;
  }

  /**
   * Computes the intersection of two {@link Envelope}s.
   *
   * @param env the envelope to intersect with
   * @return a new Envelope representing the intersection of the envelopes (this will be
   * the null envelope if either argument is null, or they do not intersect
   */
  public Envelope intersection(final Envelope env) {
    if (isNull() || env.isNull() || !intersects(env)) {
      return new Envelope();
    }

    final double intMinX = minx > env.minx ? minx : env.minx;
    final double intMinY = miny > env.miny ? miny : env.miny;
    final double intMaxX = maxx < env.maxx ? maxx : env.maxx;
    final double intMaxY = maxy < env.maxy ? maxy : env.maxy;
    return new Envelope(intMinX, intMaxX, intMinY, intMaxY);
  }

  /**
   *  Check if the point <code>p</code>
   *  overlaps (lies inside) the region of this <code>Envelope</code>.
   *
   *@param  p  the <code>Coordinate</code> to be tested
   *@return        <code>true</code> if the point overlaps this <code>Envelope</code>
   */
  public boolean intersects(final Coordinates p) {
    return intersects(p.getX(), p.getY());
  }

  /**
   *  Check if the point <code>(x, y)</code>
   *  overlaps (lies inside) the region of this <code>Envelope</code>.
   *
   *@param  x  the x-ordinate of the point
   *@param  y  the y-ordinate of the point
   *@return        <code>true</code> if the point overlaps this <code>Envelope</code>
   */
  public boolean intersects(final double x, final double y) {
    if (isNull()) {
      return false;
    }
    return !(x > maxx || x < minx || y > maxy || y < miny);
  }

  /**
   *  Check if the region defined by <code>other</code>
   *  overlaps (intersects) the region of this <code>Envelope</code>.
   *
   *@param  other  the <code>Envelope</code> which this <code>Envelope</code> is
   *          being checked for overlapping
   *@return        <code>true</code> if the <code>Envelope</code>s overlap
   */
  public boolean intersects(final Envelope other) {
    if (isNull() || other.isNull()) {
      return false;
    }
    return !(other.minx > maxx || other.maxx < minx || other.miny > maxy || other.maxy < miny);
  }

  /**
   *  Returns <code>true</code> if this <code>Envelope</code> is a "null"
   *  envelope.
   *
   *@return    <code>true</code> if this <code>Envelope</code> is uninitialized
   *      or is the envelope of the empty geometry.
   */
  public boolean isNull() {
    return maxx < minx;
  }

  /**
   * Gets the maximum extent of this envelope across both dimensions.
   * 
   * @return the maximum extent of this envelope
   */
  public double maxExtent() {
    if (isNull()) {
      return 0.0;
    }
    final double w = getWidth();
    final double h = getHeight();
    if (w > h) {
      return w;
    }
    return h;
  }

  /**
   * Gets the minimum extent of this envelope across both dimensions.
   * 
   * @return the minimum extent of this envelope
   */
  public double minExtent() {
    if (isNull()) {
      return 0.0;
    }
    final double w = getWidth();
    final double h = getHeight();
    if (w < h) {
      return w;
    }
    return h;
  }

  /**
   * @deprecated Use #intersects instead.
   */
  @Deprecated
  public boolean overlaps(final Coordinates p) {
    return intersects(p);
  }

  /**
   * @deprecated Use #intersects instead.
   */
  @Deprecated
  public boolean overlaps(final double x, final double y) {
    return intersects(x, y);
  }

  /**
   * @deprecated Use #intersects instead. In the future, #overlaps may be
   * changed to be a true overlap check; that is, whether the intersection is
   * two-dimensional.
   */
  @Deprecated
  public boolean overlaps(final Envelope other) {
    return intersects(other);
  }

  /**
   *  Makes this <code>Envelope</code> a "null" envelope, that is, the envelope
   *  of the empty geometry.
   */
  public void setToNull() {
    minx = 0;
    maxx = -1;
    miny = 0;
    maxy = -1;
  }

  @Override
  public String toString() {
    return "Env[" + minx + " : " + maxx + ", " + miny + " : " + maxy + "]";
  }

  /**
   * Translates this envelope by given amounts in the X and Y direction.
   *
   * @param transX the amount to translate along the X axis
   * @param transY the amount to translate along the Y axis
   */
  public void translate(final double transX, final double transY) {
    if (isNull()) {
      return;
    }
    init(getMinX() + transX, getMaxX() + transX, getMinY() + transY, getMaxY()
      + transY);
  }
}
