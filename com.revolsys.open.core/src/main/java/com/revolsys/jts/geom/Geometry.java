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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.data.model.types.DataTypeProxy;
import com.revolsys.io.Reader;
import com.revolsys.jts.geom.segment.Segment;
import com.revolsys.jts.geom.vertex.Vertex;
import com.revolsys.jts.operation.buffer.Buffer;
import com.revolsys.jts.operation.linemerge.LineMerger;
import com.revolsys.jts.operation.union.UnaryUnionOp;
import com.revolsys.jts.operation.valid.IsValidOp;

/**
 * A representation of a planar, linear vector geometry.
 * <P>
 *
 *  <H3>Binary Predicates</H3>
 * Because it is not clear at this time
 * what semantics for spatial
 * analysis methods involving <code>GeometryCollection</code>s would be useful,
 * <code>GeometryCollection</code>s are not supported as arguments to binary
 * predicates or the <code>relate</code>
 * method.
 *
 * <H3>Overlay Methods</H3>
 *
 * The overlay methods 
 * return the most specific class possible to represent the result. If the
 * result is homogeneous, a <code>Point</code>, <code>LineString</code>, or
 * <code>Polygon</code> will be returned if the result contains a single
 * element; otherwise, a <code>MultiPoint</code>, <code>MultiLineString</code>,
 * or <code>MultiPolygon</code> will be returned. If the result is
 * heterogeneous a <code>GeometryCollection</code> will be returned. <P>
 *
 * Because it is not clear at this time what semantics for set-theoretic
 * methods involving <code>GeometryCollection</code>s would be useful,
 * <code>GeometryCollections</code>
 * are not supported as arguments to the set-theoretic methods.
 *
 *  <H4>Representation of Computed Geometries </H4>
 *
 *  The SFS states that the result
 *  of a set-theoretic method is the "point-set" result of the usual
 *  set-theoretic definition of the operation (SFS 3.2.21.1). However, there are
 *  sometimes many ways of representing a point set as a <code>Geometry</code>.
 *  <P>
 *
 *  The SFS does not specify an unambiguous representation of a given point set
 *  returned from a spatial analysis method. One goal of JTS is to make this
 *  specification precise and unambiguous. JTS uses a canonical form for
 *  <code>Geometry</code>s returned from overlay methods. The canonical
 *  form is a <code>Geometry</code> which is simple and noded:
 *  <UL>
 *    <LI> Simple means that the Geometry returned will be simple according to
 *    the JTS definition of <code>isSimple</code>.
 *    <LI> Noded applies only to overlays involving <code>LineString</code>s. It
 *    means that all intersection points on <code>LineString</code>s will be
 *    present as endpoints of <code>LineString</code>s in the result.
 *  </UL>
 *  This definition implies that non-simple geometries which are arguments to
 *  spatial analysis methods must be subjected to a line-dissolve process to
 *  ensure that the results are simple.
 *
 *  <H4> Constructed Point And The Precision Model </H4>
 *
 *  The results computed by the set-theoretic methods may
 *  contain constructed points which are not present in the input <code>Geometry</code>
 *  s. These new points arise from intersections between line segments in the
 *  edges of the input <code>Geometry</code>s. In the general case it is not
 *  possible to represent constructed points exactly. This is due to the geometryFactory
 *  that the coordinates of an intersection point may contain twice as many bits
 *  of precision as the coordinates of the input line segments. In order to
 *  represent these constructed points explicitly, JTS must truncate them to fit
 *  the <code>PrecisionModel</code>. <P>
 *
 *  Unfortunately, truncating coordinates moves them slightly. Line segments
 *  which would not be coincident in the exact result may become coincident in
 *  the truncated representation. This in turn leads to "topology collapses" --
 *  situations where a computed element has a lower dimension than it would in
 *  the exact result. <P>
 *
 *  When JTS detects topology collapses during the computation of spatial
 *  analysis methods, it will throw an exception. If possible the exception will
 *  report the location of the collapse. <P>
 *
 * <h3>Geometry Equality</h3>
 * 
 * There are two ways of comparing geometries for equality: 
 * <b>structural equality</b> and <b>topological equality</b>.
 * 
 * <h4>Structural Equality</h4>
 *
 * Structural Equality is provided by the 
 * {@link #equals(2,Geometry)} method.  
 * This implements a comparison based on exact, structural pointwise
 * equality. 
 * The {@link #equals(Object)} is a synonym for this method, 
 * to provide structural equality semantics for
 * use in Java collections.
 * It is important to note that structural pointwise equality
 * is easily affected by things like
 * ring order and component order.  In many situations
 * it will be desirable to normalize geometries before
 * comparing them (using the {@link #norm()} 
 * or {@link #normalize()} methods).
 * {@link #equalsNorm(Geometry)} is provided
 * as a convenience method to compute equality over
 * normalized geometries, but it is expensive to use.
 * Finally, {@link #equalsExact(Geometry, double)}
 * allows using a tolerance value for point comparison.
 * 
 * 
 * <h4>Topological Equality</h4>
 * 
 * Topological Equality is provided by the 
 * {@link #equalsTopo(Geometry)} method. 
 * It implements the SFS definition of point-set equality
 * defined in terms of the DE-9IM matrix.
 * To support the SFS naming convention, the method
 * {@link #equals(Geometry)} is also provided as a synonym.  
 * However, due to the potential for confusion with {@link #equals(Object)}
 * its use is discouraged.
 * <p>
 * Since {@link #equals(Object)} and {@link #hashCode()} are overridden, 
 * Geometries can be used effectively in Java collections.
 *
 *@version 1.7
 */
public interface Geometry extends Cloneable, Comparable<Object>, Serializable,
  DataTypeProxy {

  List<String> sortedGeometryTypes = Collections.unmodifiableList(Arrays.asList(
    "Point", "MultiPoint", "LineString", "LinearRing", "MultiLineString",
    "Polygon", "MultiPolygon", "GeometryCollection"));

  /**
   * Computes a buffer area around this geometry having the given width. The
   * buffer of a Geometry is the Minkowski sum or difference of the geometry
   * with a disc of radius <code>abs(distance)</code>.
   * <p> 
   * Mathematically-exact buffer area boundaries can contain circular arcs. 
   * To represent these arcs using linear geometry they must be approximated with line segments.
   * The buffer geometry is constructed using 8 segments per quadrant to approximate 
   * the circular arcs.
   * The end cap style is <code>CAP_ROUND</code>.
   * <p>
   * The buffer operation always returns a polygonal result. The negative or
   * zero-distance buffer of lines and points is always an empty {@link Polygon}.
   * This is also the result for the buffers of degenerate (zero-area) polygons.
   * 
   * @param distance
   *          the width of the buffer (may be positive, negative or 0)
   * @return a polygonal geometry representing the buffer region (which may be
   *         empty)
   * 
   * @throws TopologyException
   *           if a robustness error occurs
   * 
   * @see #buffer(double, int)
   * @see #buffer(double, int, int)
   */
  Geometry buffer(final double distance);

  /**
   * Computes a buffer area around this geometry having the given width and with
   * a specified accuracy of approximation for circular arcs.
   * <p>
   * Mathematically-exact buffer area boundaries can contain circular arcs. 
   * To represent these arcs
   * using linear geometry they must be approximated with line segments. The
   * <code>quadrantSegments</code> argument allows controlling the accuracy of
   * the approximation by specifying the number of line segments used to
   * represent a quadrant of a circle
   * <p>
   * The buffer operation always returns a polygonal result. The negative or
   * zero-distance buffer of lines and points is always an empty {@link Polygon}.
   * This is also the result for the buffers of degenerate (zero-area) polygons.
   * 
   * @param distance
   *          the width of the buffer (may be positive, negative or 0)
   * @param quadrantSegments
   *          the number of line segments used to represent a quadrant of a
   *          circle
   * @return a polygonal geometry representing the buffer region (which may be
   *         empty)
   * 
   * @throws TopologyException
   *           if a robustness error occurs
   * 
   * @see #buffer(double)
   * @see #buffer(double, int, int)
   */
  Geometry buffer(final double distance, final int quadrantSegments);

  /**
   * Computes a buffer area around this geometry having the given
   * width and with a specified accuracy of approximation for circular arcs,
   * and using a specified end cap style.
   * <p>
   * Mathematically-exact buffer area boundaries can contain circular arcs.
   * To represent these arcs using linear geometry they must be approximated with line segments.
   * The <code>quadrantSegments</code> argument allows controlling the
   * accuracy of the approximation
   * by specifying the number of line segments used to represent a quadrant of a circle
   * <p>
   * The end cap style specifies the buffer geometry that will be
   * created at the ends of linestrings.  The styles provided are:
   * <ul>
   * <li><code>Buffer.CAP_ROUND</code> - (default) a semi-circle
   * <li><code>Buffer.CAP_BUTT</code> - a straight line perpendicular to the end segment
   * <li><code>Buffer.CAP_SQUARE</code> - a half-square
   * </ul>
   * <p>
   * The buffer operation always returns a polygonal result. The negative or
   * zero-distance buffer of lines and points is always an empty {@link Polygon}.
   * This is also the result for the buffers of degenerate (zero-area) polygons.
   *
   *@param  distance  the width of the buffer (may be positive, negative or 0)
   *@param quadrantSegments the number of line segments used to represent a quadrant of a circle
   *@param endCapStyle the end cap style to use
   *@return a polygonal geometry representing the buffer region (which may be empty)
   *
   * @throws TopologyException if a robustness error occurs
   *
   * @see #buffer(double)
   * @see #buffer(double, int)
   * @see Buffer
   */
  Geometry buffer(final double distance, final int quadrantSegments,
    final int endCapStyle);

  /**
   * Creates and returns a full copy of this {@link Geometry} object
   * (including all coordinates contained by it).
   * Subclasses are responsible for overriding this method and copying
   * their internal data.  Overrides should call this method first.
   *
   * @return a clone of this instance
   */
  Geometry clone();

  /**
  *  Returns whether this <code>Geometry</code> is greater than, equal to,
  *  or less than another <code>Geometry</code>. <P>
  *
  *  If their classes are different, they are compared using the following
  *  ordering:
  *  <UL>
  *    <LI> Point (lowest)
  *    <LI> MultiPoint
  *    <LI> LineString
  *    <LI> LinearRing
  *    <LI> MultiLineString
  *    <LI> Polygon
  *    <LI> MultiPolygon
  *    <LI> GeometryCollection (highest)
  *  </UL>
  *  If the two <code>Geometry</code>s have the same class, their first
  *  elements are compared. If those are the same, the second elements are
  *  compared, etc.
  *
  *@param  other  a <code>Geometry</code> with which to compare this <code>Geometry</code>
  *@return    a positive number, 0, or a negative number, depending on whether
  *      this object is greater than, equal to, or less than <code>o</code>, as
  *      defined in "Normal Form For Geometry" in the JTS Technical
  *      Specifications
  */
  @Override
  int compareTo(final Object other);

  /**
   *  Returns whether this <code>Geometry</code> is greater than, equal to,
   *  or less than another <code>Geometry</code>,
   * using the given {@link CoordinateSequenceComparator}.
   * <P>
   *
   *  If their classes are different, they are compared using the following
   *  ordering:
   *  <UL>
   *    <LI> Point (lowest)
   *    <LI> MultiPoint
   *    <LI> LineString
   *    <LI> LinearRing
   *    <LI> MultiLineString
   *    <LI> Polygon
   *    <LI> MultiPolygon
   *    <LI> GeometryCollection (highest)
   *  </UL>
   *  If the two <code>Geometry</code>s have the same class, their first
   *  elements are compared. If those are the same, the second elements are
   *  compared, etc.
   *
   *@param  o  a <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@param comp a <code>CoordinateSequenceComparator</code>
   *
   *@return    a positive number, 0, or a negative number, depending on whether
   *      this object is greater than, equal to, or less than <code>o</code>, as
   *      defined in "Normal Form For Geometry" in the JTS Technical
   *      Specifications
   */
  int compareTo(final Object o, final CoordinateSequenceComparator comp);

  int compareToSameClass(Geometry geometry);

  int compareToSameClass(Geometry geometry, CoordinateSequenceComparator comp);

  /**
   * Tests whether this geometry contains the
   * argument geometry.
   * <p>
   * The <code>contains</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>Every point of the other geometry is a point of this geometry,
   * and the interiors of the two geometries have at least one point in common.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches 
   * the pattern
   * <code>[T*****FF*]</code>
   * <li><code>g.within(this) = true</code>
   * <br>(<code>contains</code> is the converse of {@link #within} )
   * </ul>
   * An implication of the definition is that "Geometries do not
   * contain their boundary".  In other words, if a geometry A is a subset of
   * the points in the boundary of a geometry B, <code>B.contains(A) = false</code>.
   * (As a concrete example, take A to be a LineString which lies in the boundary of a Polygon B.)
   * For a predicate with similar behaviour but avoiding 
   * this subtle limitation, see {@link #covers}.
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if this <code>Geometry</code> contains <code>g</code>
   *
   * @see Geometry#within
   * @see Geometry#covers
   */
  boolean contains(final Geometry geometry);

  /**
   * Convert the geometry to the requried geometry factory. Projecting to the required
   * coordinate system and applying the precision model. If the geometry factory is
   * the same as this geometries factory then the geometry will be returned.
   * 
   * @author Paul Austin <paul.austin@revolsys.com>
   * @param geometryFactory The geometry factory to convert the geometry to.
   * @return The converted geometry
   */
  <V extends Geometry> V convert(GeometryFactory geometryFactory);

  /**
   *  Computes the smallest convex <code>Polygon</code> that contains all the
   *  points in the <code>Geometry</code>. This obviously applies only to <code>Geometry</code>
   *  s which contain 3 or more points; the results for degenerate cases are
   *  specified as follows:
   *  <TABLE>
   *    <TR>
   *      <TH>    Number of <code>Point</code>s in argument <code>Geometry</code>   </TH>
   *      <TH>    <code>Geometry</code> class of result     </TH>
   *    </TR>
   *    <TR>
   *      <TD>        0      </TD>
   *      <TD>        empty <code>GeometryCollection</code>      </TD>
   *    </TR>
   *    <TR>  <TD>      1     </TD>
   *      <TD>     <code>Point</code>     </TD>
   *    </TR>
   *    <TR>
   *      <TD>      2     </TD>
   *      <TD>     <code>LineString</code>     </TD>
   *    </TR>
   *    <TR>
   *      <TD>       3 or more     </TD>
   *      <TD>      <code>Polygon</code>     </TD>
   *    </TR>
   *  </TABLE>
   *
   *@return    the minimum-area convex polygon containing this <code>Geometry</code>'
   *      s points
   */
  Geometry convexHull();

  /**
   * Create a copy of the geometry io the requried geometry factory. Projecting to the required
   * coordinate system and applying the precision model.
   * 
   * @author Paul Austin <paul.austin@revolsys.com>
   * @param geometryFactory The geometry factory to convert the geometry to.
   * @return The converted geometry
   */
  <V extends Geometry> V copy(GeometryFactory geometryFactory);

  /**
   * Tests whether this geometry is covered by the
   * argument geometry.
   * <p>
   * The <code>coveredBy</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>Every point of this geometry is a point of the other geometry.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * at least one of the following patterns:
   *  <ul>
   *   <li><code>[T*F**F***]</code>
   *   <li><code>[*TF**F***]</code>
   *   <li><code>[**FT*F***]</code>
   *   <li><code>[**F*TF***]</code>
   *  </ul>
   * <li><code>g.covers(this) = true</code>
   * <br>(<code>coveredBy</code> is the converse of {@link #covers})
   * </ul>
   * If either geometry is empty, the value of this predicate is <code>false</code>.
   * <p>
   * This predicate is similar to {@link #within},
   * but is more inclusive (i.e. returns <code>true</code> for more cases).
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if this <code>Geometry</code> is covered by <code>g</code>
   *
   * @see Geometry#within
   * @see Geometry#covers
   */
  boolean coveredBy(final Geometry geometry);

  /**
   * Tests whether this geometry covers the
   * argument geometry.
   * <p>
   * The <code>covers</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>Every point of the other geometry is a point of this geometry.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * at least one of the following patterns:
   *  <ul> 
   *   <li><code>[T*****FF*]</code>
   *   <li><code>[*T****FF*]</code>
   *   <li><code>[***T**FF*]</code>
   *   <li><code>[****T*FF*]</code>
   *  </ul>
   * <li><code>g.coveredBy(this) = true</code>
   * <br>(<code>covers</code> is the converse of {@link #coveredBy})
   * </ul>
   * If either geometry is empty, the value of this predicate is <code>false</code>.
   * <p>
   * This predicate is similar to {@link #contains},
   * but is more inclusive (i.e. returns <code>true</code> for more cases).
   * In particular, unlike <code>contains</code> it does not distinguish between
   * points in the boundary and in the interior of geometries.
   * For most situations, <code>covers</code> should be used in preference to <code>contains</code>.
   * As an added benefit, <code>covers</code> is more amenable to optimization,
   * and hence should be more performant.
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if this <code>Geometry</code> covers <code>g</code>
   *
   * @see Geometry#contains
   * @see Geometry#coveredBy
   */
  boolean covers(final Geometry geometry);

  /**
   * Tests whether this geometry crosses the
   * argument geometry.
   * <p>
   * The <code>crosses</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The geometries have some but not all interior points in common.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * one of the following patterns:
   *   <ul>
   *    <li><code>[T*T******]</code> (for P/L, P/A, and L/A situations)
   *    <li><code>[T*****T**]</code> (for L/P, A/P, and A/L situations)
   *    <li><code>[0********]</code> (for L/L situations)
   *   </ul>
   * </ul>
   * For any other combination of dimensions this predicate returns <code>false</code>.
   * <p>
   * The SFS defined this predicate only for P/L, P/A, L/L, and L/A situations.
   * In order to make the relation symmetric,
   * JTS extends the definition to apply to L/P, A/P and A/L situations as well.
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if the two <code>Geometry</code>s cross.
   */
  boolean crosses(final Geometry geometry);

  /**
   * Computes a <code>Geometry</code> representing the closure of the point-set
   * of the points contained in this <code>Geometry</code> that are not contained in 
   * the <code>other</code> Geometry. 
   * <p>
   * If the result is empty, it is an atomic geometry
   * with the dimension of the left-hand input.
   * <p>
   * Non-empty {@link GeometryCollection} arguments are not supported.
   *
   *@param  other  the <code>Geometry</code> with which to compute the
   *      difference
   *@return a Geometry representing the point-set difference of this <code>Geometry</code> with
   *      <code>other</code>
   * @throws TopologyException if a robustness error occurs
   * @throws IllegalArgumentException if either input is a non-empty GeometryCollection
   */
  Geometry difference(final Geometry geometry);

  /**
   * Tests whether this geometry is disjoint from the argument geometry.
   * <p>
   * The <code>disjoint</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The two geometries have no point in common
   * <li>The DE-9IM Intersection Matrix for the two geometries matches 
   * <code>[FF*FF****]</code>
   * <li><code>! g.intersects(this) = true</code>
   * <br>(<code>disjoint</code> is the inverse of <code>intersects</code>)
   * </ul>
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if the two <code>Geometry</code>s are
   *      disjoint
   *
   * @see Geometry#intersects
   */
  boolean disjoint(final Geometry geometry);

  /**
   *  Returns the minimum distance between this <code>Geometry</code>
   *  and another <code>Geometry</code>.
   *
   * @param  g the <code>Geometry</code> from which to compute the distance
   * @return the distance between the geometries
   * @return 0 if either input geometry is empty
   * @throws IllegalArgumentException if g is null
   */
  double distance(final Geometry geometry);

  boolean equal(final Point a, final Point b, final double tolerance);

  /**
  * Tests whether this geometry is 
  * topologically equal to the argument geometry.
   * <p>
   * This method is included for backward compatibility reasons.
   * It has been superseded by the {@link #equalsTopo(Geometry)} method,
   * which has been named to clearly denote its functionality.
   * <p>
   * This method should NOT be confused with the method 
   * {@link #equals(Object)}, which implements 
   * an exact equality comparison.
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return true if the two <code>Geometry</code>s are topologically equal
   *
   *@see #equalsTopo(Geometry)
   */
  boolean equals(final Geometry geometry);

  boolean equals(int axisCount, Geometry geometry);

  /**
   * Tests whether this geometry is structurally and numerically equal
   * to a given <code>Object</code>.
   * If the argument <code>Object</code> is not a <code>Geometry</code>, 
   * the result is <code>false</code>.
   * Otherwise, the result is computed using
   * {@link #equals(2,Geometry)}.
   * <p>
   * This method is provided to fulfill the Java contract
   * for value-based object equality. 
   * In conjunction with {@link #hashCode()} 
   * it provides semantics which are most useful 
   * for using
   * <code>Geometry</code>s as keys and values in Java collections.
   * <p>
   * Note that to produce the expected result the input geometries
   * should be in normal form.  It is the caller's 
   * responsibility to perform this where required
   * (using {@link Geometry#norm()
   * or {@link #normalize()} as appropriate).
   * 
   * @param other the Object to compare
   * @return true if this geometry is exactly equal to the argument 
   * 
   * @see #equals(2,Geometry)
   * @see #hashCode()
   * @see #norm()
   * @see #normalize()
   */
  @Override
  boolean equals(final Object other);

  boolean equalsExact(final Geometry other);

  /**
   * Returns true if the two <code>Geometry</code>s are exactly equal,
   * up to a specified distance tolerance.
   * Two Geometries are exactly equal within a distance tolerance
   * if and only if:
   * <ul>
   * <li>they have the same structure
   * <li>they have the same values for their vertices,
   * within the given tolerance distance, in exactly the same order.
   * </ul>
   * This method does <i>not</i>
   * test the values of the <code>GeometryFactory</code>, the <code>SRID</code>, 
   * or the <code>userData</code> fields.
   * <p>
   * To properly test equality between different geometries,
   * it is usually necessary to {@link #normalize()} them first.
   *
   * @param other the <code>Geometry</code> with which to compare this <code>Geometry</code>
   * @param tolerance distance at or below which two <code>Coordinate</code>s
   *   are considered equal
   * @return <code>true</code> if this and the other <code>Geometry</code>
   *   have identical structure and point values, up to the distance tolerance.
   *   
   * @see #equals(2,Geometry)
   * @see #normalize()
   * @see #norm()
   */
  boolean equalsExact(Geometry other, double tolerance);

  /**
   * Tests whether two geometries are exactly equal
   * in their normalized forms.
   * This is a convenience method which creates normalized
   * versions of both geometries before computing
   * {@link #equals(2,Geometry)}.
   * <p>
   * This method is relatively expensive to compute.  
   * For maximum performance, the client 
   * should instead perform normalization on the individual geometries
   * at an appropriate point during processing.
   * 
   * @param g a Geometry
   * @return true if the input geometries are exactly equal in their normalized form
   */
  boolean equalsNorm(final Geometry geometry);

  /**
   * Tests whether this geometry is topologically equal to the argument geometry
   * as defined by the SFS <code>equals</code> predicate.
   * <p>
   * The SFS <code>equals</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The two geometries have at least one point in common,
   * and no point of either geometry lies in the exterior of the other geometry.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * the pattern <code>T*F**FFF*</code> 
   * <pre>
   * T*F
   * **F
   * FF*
   * </pre>
   * </ul>
   * <b>Note</b> that this method computes <b>topologically equality</b>. 
   * For structural equality, see {@link #equals(2,Geometry)}.
   *
   *@param g the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return <code>true</code> if the two <code>Geometry</code>s are topologically equal
   *
   *@see #equals(2,Geometry) 
   */
  boolean equalsTopo(final Geometry geometry);

  Iterable<Geometry> geometries();

  /**
   *  Returns the area of this <code>Geometry</code>.
   *  Areal Geometries have a non-zero area.
   *  They override this function to compute the area.
   *  Others return 0.0
   *
   *@return the area of the Geometry
   */
  double getArea();

  int getAxisCount();

  /**
   * Returns the boundary, or an empty geometry of appropriate dimension
   * if this <code>Geometry</code>  is empty.
   * (In the case of zero-dimensional geometries, '
   * an empty GeometryCollection is returned.)
   * For a discussion of this function, see the OpenGIS Simple
   * Features Specification. As stated in SFS Section 2.1.13.1, "the boundary
   * of a Geometry is a set of Geometries of the next lower dimension."
   *
   *@return    the closure of the combinatorial boundary of this <code>Geometry</code>
   */
  Geometry getBoundary();

  /**
   *  Returns the dimension of this <code>Geometry</code>s inherent boundary.
   *
   *@return    the dimension of the boundary of the class implementing this
   *      interface, whether or not this object is the empty geometry. Returns
   *      <code>Dimension.FALSE</code> if the boundary is the empty geometry.
   */
  int getBoundaryDimension();

  /**
   * Gets an {@link Envelope} containing 
   * the minimum and maximum x and y values in this <code>Geometry</code>.
   * If the geometry is empty, an empty <code>Envelope</code> 
   * is returned.
   * <p>
   * The returned object is a copy of the one maintained internally,
   * to avoid aliasing issues.  
   * For best performance, clients which access this
   * envelope frequently should cache the return value.
   *
   *@return the envelope of this <code>Geometry</code>.
   *@return an empty BoundingBox if this Geometry is empty
   */
  BoundingBox getBoundingBox();

  /**
   * Computes the centroid of this <code>Geometry</code>.
   * The centroid
   * is equal to the centroid of the set of component Geometries of highest
   * dimension (since the lower-dimension geometries contribute zero
   * "weight" to the centroid).
   * <p>
   * The centroid of an empty geometry is <code>POINT EMPTY</code>.
   *
   * @return a {@link Point} which is the centroid of this Geometry
   */
  Point getCentroid();

  int getClassSortIndex();

  /**
   * 
   * @author Paul Austin <paul.austin@revolsys.com>
   * @return
   */
  CoordinateSystem getCoordinateSystem();

  /**
   * Returns the dimension of this geometry.
   * The dimension of a geometry is is the topological 
   * dimension of its embedding in the 2-D Euclidean plane.
   * In the JTS spatial model, dimension values are in the set {0,1,2}.
   * <p>
   * Note that this is a different concept to the dimension of 
   * the vertex {@link Coordinates}s.  
   * The geometry dimension can never be greater than the coordinate dimension.
   * For example, a 0-dimensional geometry (e.g. a Point) 
   * may have a coordinate dimension of 3 (X,Y,Z). 
   *
   *@return the topological dimension of this geometry.
   */
  int getDimension();

  /**
   *  Gets a Geometry representing the envelope (bounding box) of 
   *  this <code>Geometry</code>. 
   *  <p>
   *  If this <code>Geometry</code> is:
   *  <ul>
   *  <li>empty, returns an empty <code>Point</code>. 
   *  <li>a point, returns a <code>Point</code>.
   *  <li>a line parallel to an axis, a two-vertex <code>LineString</code> 
   *  <li>otherwise, returns a
   *  <code>Polygon</code> whose vertices are (minx miny, maxx miny, 
   *  maxx maxy, minx maxy, minx miny).
   *  </ul>
   *
   *@return a Geometry representing the envelope of this Geometry
   *      
   * @see GeometryFactory#toLineString(Envelope) 
   */
  Geometry getEnvelope();

  /**
   * @author Paul Austin <paul.austin@revolsys.com>
   */
  <V extends Geometry> List<V> getGeometries();

  <V extends Geometry> List<V> getGeometries(Class<V> geometryClass);

  /**
   * Returns an element {@link Geometry} from a {@link GeometryCollection}
   * (or <code>this</code>, if the geometry is not a collection).
   *
   * @param partIndex the index of the geometry element
   * @return the n'th geometry contained in this geometry
   */
  <V extends Geometry> V getGeometry(final int partIndex);

  /**
   * Differs from {@link #getGeometries(Class)} in that it will return matching {@link Polygon#rings()}
   * 
   * @param geometryClass
   * @return
   */

  <V extends Geometry> List<V> getGeometryComponents(
    final Class<V> geometryClass);

  /**
   * Returns the number of {@link Geometry}s in a {@link GeometryCollection}
   * (or 1, if the geometry is not a collection).
   *
   * @return the number of geometries contained in this geometry
   */
  int getGeometryCount();

  /**
   * Gets the geometryFactory which contains the context in which this geometry was created.
   *
   * @return the geometryFactory for this geometry
  * @author Paul Austin <paul.austin@revolsys.com>
   */
  GeometryFactory getGeometryFactory();

  /**
   * Returns the name of this Geometry's actual class.
   *
   *@return the name of this <code>Geometry</code>s actual class
   */
  String getGeometryType();

  /**
   * Computes an interior point of this <code>Geometry</code>.
   * An interior point is guaranteed to lie in the interior of the Geometry,
   * if it possible to calculate such a point exactly. Otherwise,
   * the point may lie on the boundary of the geometry.
   * <p>
   * The interior point of an empty geometry is <code>POINT EMPTY</code>.
   *
   * @return a {@link Point} which is in the interior of this Geometry
   */
  Point getInteriorPoint();

  /**
   *  Returns the length of this <code>Geometry</code>.
   *  Linear geometries return their length.
   *  Areal geometries return their perimeter.
   *  They override this function to compute the area.
   *  Others return 0.0
   *
   *@return the length of the Geometry
   */
  double getLength();

  /**
   *  Returns a vertex of this <code>Geometry</code>
   *  (usually, but not necessarily, the first one).
   *  The returned coordinate should not be assumed
   *  to be an actual Point object used in
   *  the internal representation.
   *
   *@return    a {@link Coordinates} which is a vertex of this <code>Geometry</code>.
   *@return null if this Geometry is empty
   */
  Point getPoint();

  /**
   * <p>Get the {@link Segment} at the specified vertexId (see {@link Segment#getSegmentId()}).</p>
   * 
   * @author Paul Austin <paul.austin@revolsys.com>
   * @param vertexId The id of the vertex.
   * @return The vertex or null if it does not exist.
   */
  Segment getSegment(final int... segmentId);

  /**
   *  Returns the ID of the Spatial Reference System used by the <code>Geometry</code>.
   *  <P>
   *
   *  JTS supports Spatial Reference System information in the simple way
   *  defined in the SFS. A Spatial Reference System ID (SRID) is present in
   *  each <code>Geometry</code> object. <code>Geometry</code> provides basic
   *  accessor operations for this field, but no others. The SRID is represented
   *  as an integer.
   *
   *@return    the ID of the coordinate space in which the <code>Geometry</code>
   *      is defined.
   *
   */
  int getSrid();

  /**
   * Gets the user data object for this geometry, if any.
   *
   * @return the user data object, or <code>null</code> if none set
   */
  Object getUserData();

  /**
   * <p>Get the {@link Vertex} at the specified vertexId (see {@link Vertex#getVertexId()}).</p>
   * 
   * @author Paul Austin <paul.austin@revolsys.com>
   * @param vertexId The id of the vertex.
   * @return The vertex or null if it does not exist.
   */
  Vertex getVertex(final int... vertexId);

  /**
   *  Returns the count of this <code>Geometry</code>s vertices. The <code>Geometry</code>
   *  s contained by composite <code>Geometry</code>s must be
   *  Geometry's; that is, they must implement <code>getNumPoints</code>
   *
   *@return    the number of vertices in this <code>Geometry</code>
   */
  int getVertexCount();

  /**
   * Gets a hash code for the Geometry.
   * 
   * @return an integer value suitable for use as a hashcode
   */
  @Override
  int hashCode();

  /**
   * Computes a <code>Geometry</code> representing the point-set which is
   * common to both this <code>Geometry</code> and the <code>other</code> Geometry.
   * <p>
   * The intersection of two geometries of different dimension produces a result
   * geometry of dimension less than or equal to the minimum dimension of the input
   * geometries. 
   * The result geometry may be a heterogenous {@link GeometryCollection}.
   * If the result is empty, it is an atomic geometry
   * with the dimension of the lowest input dimension.
   * <p>
   * Intersection of {@link GeometryCollection}s is supported
   * only for homogeneous collection types. 
   * <p>
   * Non-empty heterogeneous {@link GeometryCollection} arguments are not supported.
   *
   * @param  other the <code>Geometry</code> with which to compute the intersection
   * @return a Geometry representing the point-set common to the two <code>Geometry</code>s
   * @throws TopologyException if a robustness error occurs
   * @throws IllegalArgumentException if the argument is a non-empty heterogeneous <code>GeometryCollection</code>
   */
  Geometry intersection(final Geometry other);

  boolean intersects(BoundingBox boundingBox);

  /**
   * Tests whether this geometry intersects the argument geometry.
   * <p>
   * The <code>intersects</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The two geometries have at least one point in common
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * at least one of the patterns
   *  <ul>
   *   <li><code>[T********]</code>
   *   <li><code>[*T*******]</code>
   *   <li><code>[***T*****]</code>
   *   <li><code>[****T****]</code>
   *  </ul>
   * <li><code>! g.disjoint(this) = true</code>
   * <br>(<code>intersects</code> is the inverse of <code>disjoint</code>)
   * </ul>
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if the two <code>Geometry</code>s intersect
   *
   * @see Geometry#disjoint
   */
  boolean intersects(final Geometry geometry);

  /**
   * Tests whether the set of points covered by this <code>Geometry</code> is
   * empty.
   *
   *@return <code>true</code> if this <code>Geometry</code> does not cover any points
   */
  boolean isEmpty();

  boolean isRectangle();

  /**
   * Tests whether this {@link Geometry} is simple.
   * The SFS definition of simplicity
   * follows the general rule that a Geometry is simple if it has no points of
   * self-tangency, self-intersection or other anomalous points.
   * <p>
   * Simplicity is defined for each {@link Geometry} subclass as follows:
   * <ul>
   * <li>Valid polygonal geometries are simple, since their rings
   * must not self-intersect.  <code>isSimple</code>
   * tests for this condition and reports <code>false</code> if it is not met.
   * (This is a looser test than checking for validity).
   * <li>Linear rings have the same semantics.
   * <li>Linear geometries are simple iff they do not self-intersect at points
   * other than boundary points.
   * <li>Zero-dimensional geometries (points) are simple iff they have no
   * repeated points.
   * <li>Empty <code>Geometry</code>s are always simple.
   * <ul>
   *
   * @return <code>true</code> if this <code>Geometry</code> is simple
   * @see #isValid
   */
  boolean isSimple();

  /**
   * Tests whether this <code>Geometry</code>
   * is topologically valid, according to the OGC SFS specification.
   * <p>
   * For validity rules see the Javadoc for the specific Geometry subclass.
   *
   *@return <code>true</code> if this <code>Geometry</code> is valid
   *
   * @see IsValidOp
   */
  boolean isValid();

  /**
   * Tests whether the distance from this <code>Geometry</code>
   * to another is less than or equal to a specified value.
   *
   * @param geom the Geometry to check the distance to
   * @param distance the distance value to compare
   * @return <code>true</code> if the geometries are less than <code>distance</code> apart.
   */
  boolean isWithinDistance(final Geometry geom, final double distance);

  Geometry move(final double... deltas);

  /**
   *  Converts this <code>Geometry</code> to <b>normal form</b> (or <b>
   *  canonical form</b> ). Normal form is a unique representation for <code>Geometry</code>
   *  s. It can be used to test whether two <code>Geometry</code>s are equal
   *  in a way that is independent of the ordering of the coordinates within
   *  them. Normal form equality is a stronger condition than topological
   *  equality, but weaker than pointwise equality. The definitions for normal
   *  form use the standard lexicographical ordering for coordinates. "Sorted in
   *  order of coordinates" means the obvious extension of this ordering to
   *  sequences of coordinates.
    * 
   * @return a normalized copy of this geometry.
   * @see #normalize()
   */
  Geometry normalize();

  /**
   * Tests whether this geometry overlaps the
   * specified geometry.
   * <p>
   * The <code>overlaps</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The geometries have at least one point each not shared by the other
   * (or equivalently neither covers the other),
   * they have the same dimension,
   * and the intersection of the interiors of the two geometries has
   * the same dimension as the geometries themselves.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   *   <code>[T*T***T**]</code> (for two points or two surfaces)
   *   or <code>[1*T***T**]</code> (for two curves)
   * </ul>
   * If the geometries are of different dimension this predicate returns <code>false</code>.
   * This predicate is symmetric.
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if the two <code>Geometry</code>s overlap.
   */
  boolean overlaps(final Geometry geometry);

  /**
   *  Returns the DE-9IM {@link IntersectionMatrix} for the two <code>Geometry</code>s.
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        an {@link IntersectionMatrix} describing the intersections of the interiors,
   *      boundaries and exteriors of the two <code>Geometry</code>s
   */
  IntersectionMatrix relate(final Geometry geometry);

  /**
   * Tests whether the elements in the DE-9IM
   * {@link IntersectionMatrix} for the two <code>Geometry</code>s match the elements in <code>intersectionPattern</code>.
   * The pattern is a 9-character string, with symbols drawn from the following set:
   *  <UL>
   *    <LI> 0 (dimension 0)
   *    <LI> 1 (dimension 1)
   *    <LI> 2 (dimension 2)
   *    <LI> T ( matches 0, 1 or 2)
   *    <LI> F ( matches FALSE)
   *    <LI> * ( matches any value)
   *  </UL>
   *  For more information on the DE-9IM, see the <i>OpenGIS Simple Features
   *  Specification</i>.
   *
   *@param  g                the <code>Geometry</code> with which to compare
   *      this <code>Geometry</code>
   *@param  intersectionPattern  the pattern against which to check the
   *      intersection matrix for the two <code>Geometry</code>s
   *@return                      <code>true</code> if the DE-9IM intersection
   *      matrix for the two <code>Geometry</code>s match <code>intersectionPattern</code>
   * @see IntersectionMatrix
   */
  boolean relate(final Geometry g, final String intersectionPattern);

  /**
   * Computes a new geometry which has all component coordinate sequences
   * in reverse order (opposite orientation) to this one.
   * 
   * @return a reversed geometry
   */
  Geometry reverse();

  Reader<Segment> segments();

  /**
   * A simple scheme for applications to add their own custom data to a Geometry.
   * An example use might be to add an object representing a Point Reference System.
   * <p>
   * Note that user data objects are not present in geometries created by
   * construction methods.
   *
   * @param userData an object, the semantics for which are defined by the
   * application using this Geometry
   */
  void setUserData(final Object userData);

  /**
   * Computes a <coe>Geometry </code> representing the closure of the point-set 
   * which is the union of the points in this <code>Geometry</code> which are not 
   * contained in the <code>other</code> Geometry,
   * with the points in the <code>other</code> Geometry not contained in this
   * <code>Geometry</code>. 
   * If the result is empty, it is an atomic geometry
   * with the dimension of the highest input dimension.
   * <p>
   * Non-empty {@link GeometryCollection} arguments are not supported.
   *
   *@param  other the <code>Geometry</code> with which to compute the symmetric
   *      difference
   *@return a Geometry representing the point-set symmetric difference of this <code>Geometry</code>
   *      with <code>other</code>
   * @throws TopologyException if a robustness error occurs
   * @throws IllegalArgumentException if either input is a non-empty GeometryCollection
   */
  Geometry symDifference(final Geometry other);

  /**
   * Tests whether this geometry touches the
   * argument geometry.
   * <p>
   * The <code>touches</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The geometries have at least one point in common, 
   * but their interiors do not intersect.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * at least one of the following patterns
   *  <ul>
   *   <li><code>[FT*******]</code>
   *   <li><code>[F**T*****]</code>
   *   <li><code>[F***T****]</code>
   *  </ul>
   * </ul>
   * If both geometries have dimension 0, the predicate returns <code>false</code>,
   * since points have only interiors.
   * This predicate is symmetric.
   * 
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if the two <code>Geometry</code>s touch;
   *      Returns <code>false</code> if both <code>Geometry</code>s are points
   */
  boolean touches(final Geometry geometry);

  /**
   *  <p>Returns the Extended Well-known Text representation of this <code>Geometry</code>.
   *  For a definition of the Well-known Text format, see the OpenGIS Simple
   *  Features Specification.</p>
   *
   *@return    the Well-known Text representation of this <code>Geometry</code>
   *@author Paul Austin <paul.austin@revolsys.com>
   */
  String toWkt();

  /**
   * Computes the union of all the elements of this geometry. 
   * <p>
   * This method supports
   * {@link GeometryCollection}s 
   * (which the other overlay operations currently do not).
   * <p>
   * The result obeys the following contract:
   * <ul>
   * <li>Unioning a set of {@link LineString}s has the effect of fully noding
   * and dissolving the linework.
   * <li>Unioning a set of {@link Polygon}s always 
   * returns a {@link Polygonal} geometry (unlike {@link #union(Geometry)},
   * which may return geometries of lower dimension if a topology collapse occurred).
   * </ul>
   * 
   * @return the union geometry
     * @throws TopologyException if a robustness error occurs
   * 
   * @see UnaryUnionOp
   */
  Geometry union();

  /**
   * Computes a <code>Geometry</code> representing the point-set 
   * which is contained in both this
   * <code>Geometry</code> and the <code>other</code> Geometry.
   * <p>
   * The union of two geometries of different dimension produces a result
   * geometry of dimension equal to the maximum dimension of the input
   * geometries. 
   * The result geometry may be a heterogenous
   * {@link GeometryCollection}.
   * If the result is empty, it is an atomic geometry
   * with the dimension of the highest input dimension.
   * <p>
   * Unioning {@link LineString}s has the effect of
   * <b>noding</b> and <b>dissolving</b> the input linework. In this context
   * "noding" means that there will be a node or endpoint in the result for
   * every endpoint or line segment crossing in the input. "Dissolving" means
   * that any duplicate (i.e. coincident) line segments or portions of line
   * segments will be reduced to a single line segment in the result. 
   * If <b>merged</b> linework is required, the {@link LineMerger}
   * class can be used.
   * <p>
   * Non-empty {@link GeometryCollection} arguments are not supported.
   * 
   * @param other
   *          the <code>Geometry</code> with which to compute the union
   * @return a point-set combining the points of this <code>Geometry</code> and the
   *         points of <code>other</code>
   * @throws TopologyException
   *           if a robustness error occurs
   * @throws IllegalArgumentException
   *           if either input is a non-empty GeometryCollection
   * @see LineMerger
   */
  Geometry union(final Geometry other);

  /**
   * <p>Get an {@link Iterable} that iterates over the {@link Vertex} of the geometry. For memory
   * efficiency the {@link Vertex} returned is the same instance for each call to next
   * on the iterator. If the vertex is required to track the previous vertex then the
   * {@link Vertex#clone()} method must be called to get a copy of the vertex.</p>
   * 
   * <p>The {@link Iterable#iterator()} method always returns the same {@link Iterator} instance.
   * Therefore that method should not be called more than once.</p>
   * 
   * @author Paul Austin <paul.austin@revolsys.com>
   * @return The iterator over the vertices of the geometry.
   */
  Reader<Vertex> vertices();

  /**
   * Tests whether this geometry is within the
   * specified geometry.
   * <p>
   * The <code>within</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>Every point of this geometry is a point of the other geometry,
   * and the interiors of the two geometries have at least one point in common.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches 
   * <code>[T*F**F***]</code>
   * <li><code>g.contains(this) = true</code>
   * <br>(<code>within</code> is the converse of {@link #contains})
   * </ul>
   * An implication of the definition is that
   * "The boundary of a Geometry is not within the Geometry".
   * In other words, if a geometry A is a subset of
   * the points in the boundary of a geomtry B, <code>A.within(B) = false</code>
   * (As a concrete example, take A to be a LineString which lies in the boundary of a Polygon B.)
   * For a predicate with similar behaviour but avoiding 
   * this subtle limitation, see {@link #coveredBy}.
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if this <code>Geometry</code> is within
   *      <code>g</code>
   *
   * @see Geometry#contains
   * @see Geometry#coveredBy
   */
  boolean within(final Geometry geometry);

}
