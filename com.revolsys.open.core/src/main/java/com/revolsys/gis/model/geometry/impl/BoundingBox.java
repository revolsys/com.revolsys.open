package com.revolsys.gis.model.geometry.impl;

import java.io.Serializable;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.projection.CoordinatesListProjectionUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.Polygon;
import com.revolsys.util.MathUtil;
import com.revolsys.gis.model.geometry.GeometryFactory;

public class BoundingBox implements Serializable {

  /** The serialization version. */
  private static final long serialVersionUID = -810356856421113732L;

  /**
   * Point intersects the bounding box of the line.
   * 
   * @param lineStart
   * @param lineEnd
   * @param point
   * @return
   */
  public static boolean intersects(final Coordinates lineStart,
    final Coordinates lineEnd, final Coordinates point) {
    final double x1 = lineStart.getX();
    final double y1 = lineStart.getY();
    final double x2 = lineEnd.getX();
    final double y2 = lineEnd.getY();

    final double x = point.getX();
    final double y = point.getY();
    return intersects(x1, y1, x2, y2, x, y);
  }

  public static boolean intersects(final Coordinates line1Start,
    final Coordinates line1End, final Coordinates line2Start,
    final Coordinates line2End) {
    final double line1x1 = line1Start.getX();
    final double line1y1 = line1Start.getY();
    final double line1x2 = line1End.getX();
    final double line1y2 = line1End.getY();

    final double line2x1 = line2Start.getX();
    final double line2y1 = line2Start.getY();
    final double line2x2 = line2End.getX();
    final double line2y2 = line2End.getY();
    return intersects(line1x1, line1y1, line1x2, line1y2, line2x1, line2y1,
      line2x2, line2y2);
  }

  /**
   * Point intersects the bounding box of the line.
   * 
   * @param lineStart
   * @param lineEnd
   * @param point
   * @return
   */
  public static boolean intersects(final double p1X, final double p1Y,
    final double p2X, final double p2Y, final double qX, final double qY) {
    if (((qX >= (p1X < p2X ? p1X : p2X)) && (qX <= (p1X > p2X ? p1X : p2X)))
      && ((qY >= (p1Y < p2Y ? p1Y : p2Y)) && (qY <= (p1Y > p2Y ? p1Y : p2Y)))) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean intersects(final double p1X, final double p1Y,
    final double p2X, final double p2Y, final double q1X, final double q1Y,
    final double q2X, final double q2Y) {
    double minp = Math.min(p1X, p2X);
    double maxq = Math.max(q1X, q2X);
    if (minp > maxq) {
      return false;
    } else {
      double minq = Math.min(q1X, q2X);
      double maxp = Math.max(p1X, p2X);
      if (maxp < minq) {
        return false;
      } else {
        minp = Math.min(p1Y, p2Y);
        maxq = Math.max(q1Y, q2Y);
        if (minp > maxq) {
          return false;
        } else {
          minq = Math.min(q1Y, q2Y);
          maxp = Math.max(p1Y, p2Y);
          if (maxp < minq) {
            return false;
          } else {
            return true;
          }
        }
      }
    }
  }

  private GeometryFactory geometryFactory;

  private double maxX = Double.MIN_VALUE;

  private double maxY = Double.MIN_VALUE;

  private double maxZ = Double.MIN_VALUE;

  private double minX = Double.MAX_VALUE;

  private double minY = Double.MAX_VALUE;

  private double minZ = Double.MAX_VALUE;

  public BoundingBox() {
  }

  public BoundingBox(final BoundingBox boundingBox) {
    this(boundingBox.getGeometryFactory(), boundingBox);
  }

  public BoundingBox(final Coordinates point1, final Coordinates point2) {
    this(null, point1.getX(), point1.getY(), point1.getZ(), point2.getX(),
      point2.getY(), point2.getZ());
  }

  public BoundingBox(final Geometry geometry) {
    this.geometryFactory = geometry.getGeometryFactory();
    for (final CoordinatesList points : geometry.getCoordinatesLists()) {
      for (int i = 0; i < points.size(); i++) {
        final double x = points.getX(i);
        final double y = points.getY(i);
        final double z = points.getZ(i);
        add(x, y, z);
      }
    }
  }

  public BoundingBox(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public BoundingBox(final GeometryFactory geometryFactory,
    final BoundingBox boundingBox) {
    this.geometryFactory = geometryFactory;
    final double x1 = boundingBox.getMinX();
    final double y1 = boundingBox.getMinY();
    final double z1 = boundingBox.getMinZ();
    add(x1, y1, z1);

    final double x2 = boundingBox.getMaxX();
    final double y2 = boundingBox.getMaxY();
    final double z2 = boundingBox.getMaxZ();
    add(x2, y2, z2);
  }

  public BoundingBox(final GeometryFactory geometryFactory, final double x,
    final double y, final double z) {
    this.geometryFactory = geometryFactory;
    add(x, y, z);
  }

  public BoundingBox(final GeometryFactory geometryFactory, final double x1,
    final double y1, final double x2, final double y2) {
    this.geometryFactory = geometryFactory;
    add(x1, y1, Double.NaN);
    add(x2, y2, Double.NaN);
  }

  public BoundingBox(final GeometryFactory geometryFactory, final double x1,
    final double y1, final double z1, final double x2, final double y2,
    final double z2) {
    this.geometryFactory = geometryFactory;
    add(x1, y1, z1);
    add(x2, y2, z2);
  }

  protected void add(final double x, final double y, final double z) {
    if (!Double.isNaN(x)) {
      if (x < getMinX()) {
        setMinX(x);
      }
      if (x > getMaxX()) {
        setMaxX(x);
      }
    }
    if (!Double.isNaN(y)) {
      if (y < getMinY()) {
        setMinY(y);
      }
      if (y > getMaxY()) {
        setMaxY(y);
      }
    }
    if (!Double.isNaN(z)) {
      if (z < getMinZ()) {
        setMinZ(z);
      }
      if (z > getMaxZ()) {
        setMaxZ(z);
      }
    }
  }

  public Coordinates centre() {
    if (isNull()) {
      return null;
    } else {
      final double x = (getMinX() + getMaxX()) / 2.0;
      final double y = (getMinY() + getMaxY()) / 2.0;
      return geometryFactory.createPoint(x, y);
    }
  }

  public boolean contains(final BoundingBox other) {
    return covers(other);
  }

  public boolean contains(final Coordinates coordinates) {
    final double x = coordinates.getX();
    final double y = coordinates.getY();
    return contains(x, y);
  }

  /**
   * Tests if the given point lies in or on the envelope.
   * <p>
   * Note that this is <b>not</b> the same definition as the SFS
   * <tt>contains</tt>, which would exclude the envelope boundary.
   * 
   * @param x the x-coordinate of the point which this <code>BoundingBox</code>
   *          is being checked for containing
   * @param y the y-coordinate of the point which this <code>BoundingBox</code>
   *          is being checked for containing
   * @return <code>true</code> if <code>(x, y)</code> lies in the interior or on
   *         the boundary of this <code>BoundingBox</code>.
   * @see #covers(double, double)
   */
  public boolean contains(final double x, final double y) {
    return covers(x, y);
  }

  public BoundingBox convert(final GeometryFactory geometryFactory) {
    if (getGeometryFactory() == null) {
      return new BoundingBox(geometryFactory, this);
    } else {
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      if (coordinateSystem.equals(this.getCoordinateSystem())) {
        return new BoundingBox(geometryFactory, this);
      } else {
        final Polygon polygon = toPolygon(geometryFactory);
        return new BoundingBox(polygon.getBoundingBox());
      }
    }
  }

  public boolean covers(final BoundingBox other) {
    if (isNull() || other.isNull()) {
      return false;
    }
    return other.getMinX() >= getMinX() && other.getMaxX() <= getMaxX()
      && other.getMinY() >= getMinY() && other.getMaxY() <= getMaxY();
  }

  public boolean covers(final Coordinates p) {
    return covers(p.getX(), p.getY());
  }

  public boolean covers(final double x, final double y) {
    if (isNull()) {
      return false;
    }
    return x >= getMinX() && x <= getMaxX() && y >= getMinY() && y <= getMaxY();
  }

  public double distance(final BoundingBox env) {
    if (intersects(env)) {
      return 0;
    }

    double dx = 0.0;
    if (getMaxX() < env.getMinX()) {
      dx = env.getMinX() - getMaxX();
    } else if (getMinX() > env.getMaxX()) {
      dx = getMinX() - env.getMaxX();
    }

    double dy = 0.0;
    if (getMaxY() < env.getMinY()) {
      dy = env.getMinY() - getMaxY();
    } else if (getMinY() > env.getMaxY()) {
      dy = getMinY() - env.getMaxY();
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
    if (other instanceof BoundingBox) {
      final BoundingBox boundingBox = (BoundingBox)other;
      return geometryFactory.equals(boundingBox.getGeometryFactory())
        && getMaxX() == boundingBox.getMaxX()
        && getMaxY() == boundingBox.getMaxY()
        && getMinX() == boundingBox.getMinX()
        && getMinY() == boundingBox.getMinY();
    } else {
      return false;
    }
  }

  /**
   * Expands this envelope by a given distance in all directions. Both positive
   * and negative distances are supported.
   * 
   * @param distance the distance to expand the envelope
   */
  public void expandBy(final double distance) {
    expandBy(distance, distance);
  }

  public BoundingBox expandBy(final double... deltas) {
    if (isNull() || deltas.length == 0) {
      return this;
    } else {

      final double minX = getMinX() - deltas[0];
      final double maxX = getMaxX() + deltas[0];
      double minY = getMinY();
      double maxY = getMaxY();
      if (deltas.length > 1) {
        minY -= deltas[1];
        maxY += deltas[1];
      }
      double minZ = getMinY();
      double maxZ = getMaxY();
      if (deltas.length > 2) {
        minZ -= deltas[2];
        maxZ += deltas[2];
      }
      if (minX > getMaxX() || getMinY() > getMaxY()) {
        return new BoundingBox(getGeometryFactory());

      } else {
        return new BoundingBox(geometryFactory, minX, minY, minZ, maxX, maxY,
          maxZ);
      }
    }
  }

  public BoundingBox expandToInclude(final BoundingBox boundingBox) {
    final BoundingBox convertedBoundingBox = boundingBox.convert(getGeometryFactory());
    if (convertedBoundingBox.isNull()) {
      return this;
    } else if (isNull()) {
      return convertedBoundingBox;
    } else {
      final double minX = Math.min(getMinX(), convertedBoundingBox.getMinX());
      final double minY = Math.min(getMinY(), convertedBoundingBox.getMinY());
      final double minZ = Math.min(getMinZ(), convertedBoundingBox.getMinZ());
      final double maxX = Math.max(getMaxX(), convertedBoundingBox.getMaxX());
      final double maxY = Math.max(getMaxY(), convertedBoundingBox.getMaxY());
      final double maxZ = Math.max(getMaxZ(), convertedBoundingBox.getMaxZ());

      return new BoundingBox(geometryFactory, minX, minY, minZ, maxX, maxY,
        maxZ);
    }
  }

  public BoundingBox expandToInclude(final Coordinates p) {
    return expandToInclude(p.getX(), p.getY(), p.getZ());
  }

  public BoundingBox expandToInclude(final double x, final double y,
    final double z) {
    if (isNull()) {
      return new BoundingBox(getGeometryFactory(), x, y, Double.NaN);
    } else {
      final double minX = Math.min(getMinX(), x);
      final double minY = Math.min(getMinY(), y);
      final double minZ = Math.min(getMinZ(), z);
      final double maxX = Math.max(getMaxX(), x);
      final double maxY = Math.max(getMaxY(), y);
      final double maxZ = Math.max(getMaxZ(), z);

      return new BoundingBox(geometryFactory, minX, minY, minZ, maxX, maxY,
        maxZ);
    }
  }

  public BoundingBox expandToInclude(final Geometry geometry) {
    final BoundingBox box = geometry.getBoundingBox();
    return expandToInclude(box);
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
   * Get the aspect ratio x:y.
   * 
   * @return The aspect ratio.
   */
  public double getAspectRatio() {
    final double width = getWidth();
    final double height = getHeight();
    final double aspectRatio = width / height;
    return aspectRatio;
  }

  public double getCentreX() {
    return getMinX() + (getWidth() / 2);
  }

  public double getCentreY() {
    return getMinY() + (getHeight() / 2);
  }

  /**
   * Get the geometry factory.
   * 
   * @return The geometry factory.
   */
  public CoordinateSystem getCoordinateSystem() {
    return geometryFactory.getCoordinateSystem();
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  /**
   * Returns the difference between the maximum and minimum y values.
   * 
   * @return max y - min y, or 0 if this is a null <code>BoundingBox</code>
   */
  public double getHeight() {
    if (isNull()) {
      return 0;
    }
    return getMaxY() - getMinY();
  }

  public Measurable<Length> getHeightLength() {
    final double height = getHeight();
    return Measure.valueOf(height, getCoordinateSystem().getLengthUnit());
  }

  public String getId() {
    final String string = MathUtil.toString(getMinX()) + "_"
      + MathUtil.toString(getMinY()) + "_" + MathUtil.toString(getMaxX()) + "_"
      + MathUtil.toString(getMaxY());
    if (geometryFactory == null) {
      return string;
    } else {
      return geometryFactory.getSrid() + "-" + string;
    }
  }

  public <Q extends Quantity> Measurable<Q> getMaximumX() {
    final Unit<Q> unit = getCoordinateSystem().getUnit();
    return Measure.valueOf(getMaxX(), unit);
  }

  public <Q extends Quantity> Measurable<Q> getMaximumY() {
    final Unit<Q> unit = getCoordinateSystem().getUnit();
    return Measure.valueOf(getMaxY(), unit);
  }

  public double getMaxX() {
    return maxX;
  }

  public double getMaxY() {
    return maxY;
  }

  public double getMaxZ() {
    if (Double.isNaN(maxZ)) {
      return 0;
    } else {
      return maxZ;
    }
  }

  public <Q extends Quantity> Measurable<Q> getMinimumX() {
    final Unit<Q> unit = getCoordinateSystem().getUnit();
    return Measure.valueOf(getMinX(), unit);
  }

  public <Q extends Quantity> Measurable<Q> getMinimumY() {
    final Unit<Q> unit = getCoordinateSystem().getUnit();
    return Measure.valueOf(getMinY(), unit);
  }

  public double getMinX() {
    return minX;
  }

  public double getMinY() {
    return minY;
  }

  public double getMinZ() {
    if (Double.isNaN(minZ)) {
      return 0;
    } else {
      return minZ;
    }
  }

  /**
   * Returns the difference between the maximum and minimum x values.
   * 
   * @return max x - min x, or 0 if this is a null <code>BoundingBox</code>
   */
  public double getWidth() {
    if (isNull()) {
      return 0;
    }
    return getMaxX() - getMinX();
  }

  public Measurable<Length> getWidthLength() {
    final double width = getWidth();
    return Measure.valueOf(width, getCoordinateSystem().getLengthUnit());
  }

  public BoundingBox intersection(final BoundingBox boundingBox) {
    final BoundingBox convertedBoundingBox = boundingBox.convert(geometryFactory);
    if (isNull() || convertedBoundingBox.isNull()
      || !intersects(convertedBoundingBox)) {
      return new BoundingBox(geometryFactory);
    } else {

      final double intMinX = getMinX() > convertedBoundingBox.getMinX() ? getMinX()
        : convertedBoundingBox.getMinX();
      final double intMinY = getMinY() > convertedBoundingBox.getMinY() ? getMinY()
        : convertedBoundingBox.getMinY();
      final double intMaxX = getMaxX() < convertedBoundingBox.getMaxX() ? getMaxX()
        : convertedBoundingBox.getMaxX();
      final double intMaxY = getMaxY() < convertedBoundingBox.getMaxY() ? getMaxY()
        : convertedBoundingBox.getMaxY();
      return new BoundingBox(geometryFactory, intMinX, intMaxX, intMinY,
        intMaxY);
    }
  }

  public boolean intersects(final BoundingBox boundingBox) {
    final BoundingBox convertedBoundingBox = boundingBox.convert(geometryFactory);

    if (isNull() || convertedBoundingBox.isNull()) {
      return false;
    }
    return !(convertedBoundingBox.getMinX() > getMaxX()
      || convertedBoundingBox.getMaxX() < getMinX()
      || convertedBoundingBox.getMinY() > getMaxY() || convertedBoundingBox.getMaxY() < getMinY());
  }

  public boolean intersects(final Coordinates point) {
    return intersects(point.getX(), point.getY());
  }

  public boolean intersects(final double x, final double y) {
    if (isNull()) {
      return false;
    }
    return !(x > getMaxX() || x < getMinX() || y > getMaxY() || y < getMinY());
  }

  /**
   * Returns <code>true</code> if this <code>BoundingBox</code> is a "null"
   * envelope.
   * 
   * @return <code>true</code> if this <code>BoundingBox</code> is uninitialized
   *         or is the envelope of the empty geometry.
   */
  public boolean isNull() {
    return getMaxX() < getMinX();
  }

  private double makeXyPrecise(final double value) {
    final CoordinatesPrecisionModel precisionModel = getGeometryFactory();
    if (precisionModel == null) {
      return value;
    } else {
      return precisionModel.makeXyPrecise(value);
    }
  }

  private double makeZPrecise(final double value) {
    final CoordinatesPrecisionModel precisionModel = getGeometryFactory();
    if (precisionModel == null) {
      return value;
    } else {
      return precisionModel.makeZPrecise(value);
    }
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

  public BoundingBox move(final double... deltas) {
    if (isNull() || deltas.length == 0) {
      return this;
    } else {
      final double minX = getMinX() + deltas[0];
      final double maxX = getMaxX() + deltas[0];
      double minY = getMinY();
      double maxY = getMaxY();
      if (deltas.length > 1) {
        minY += deltas[1];
        maxY += deltas[1];
      }
      double minZ = getMinY();
      double maxZ = getMaxY();
      if (deltas.length > 2) {
        minZ += deltas[2];
        maxZ += deltas[2];
      }
      return new BoundingBox(geometryFactory, minX, minY, minZ, maxX, maxY,
        maxZ);
    }
  }

  public boolean overlaps(final BoundingBox other) {
    return intersects(other);
  }

  public boolean overlaps(final Coordinates p) {
    return intersects(p.getX(), p.getY());
  }

  public boolean overlaps(final double x, final double y) {
    return intersects(x, y);
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  protected void setMaxX(final double maxX) {
    if (!Double.isNaN(maxX)) {
      this.maxX = makeXyPrecise(maxX);
    }
  }

  protected void setMaxY(final double maxY) {
    if (!Double.isNaN(maxY)) {
      this.maxY = makeXyPrecise(maxY);
    }
  }

  protected void setMaxZ(final double maxZ) {
    if (!Double.isNaN(maxZ)) {
      this.maxZ = makeZPrecise(maxZ);
    }
  }

  protected void setMinX(final double minX) {
    if (!Double.isNaN(minX)) {
      this.minX = makeXyPrecise(minX);
    }
  }

  protected void setMinY(final double minY) {
    if (!Double.isNaN(minY)) {
      this.minY = makeXyPrecise(minY);
    }
  }

  protected void setMinZ(final double minZ) {
    if (!Double.isNaN(minZ)) {
      this.minZ = makeZPrecise(minZ);
    }
  }

  public Geometry toGeometry() {
    final double width = getWidth();
    final double height = getHeight();
    if (width == 0 && height == 0) {
      return geometryFactory.createPoint(getMinX(), getMinY());
    } else if (width == 0 || height == 0) {
      return geometryFactory.createLineString(new DoubleCoordinatesList(2,
        getMinX(), getMinY(), getMaxX(), getMaxY()));
    } else {
      return geometryFactory.createPolygon(new DoubleCoordinatesList(2,
        getMinX(), getMinY(), getMinX(), getMaxY(), getMaxX(), getMaxY(),
        getMaxX(), getMinY(), getMinX(), getMinY()));
    }
  }

  public Polygon toPolygon() {
    return toPolygon(100, 100);

  }

  public Polygon toPolygon(final GeometryFactory factory) {
    return toPolygon(factory, 100, 100);
  }

  public Polygon toPolygon(final GeometryFactory factory, final int numSegments) {
    return toPolygon(factory, numSegments, numSegments);
  }

  public Polygon toPolygon(final GeometryFactory geometryFactory,
    final int numX, final int numY) {
    final double xStep = getWidth() / numX;
    final double yStep = getHeight() / numY;
    final int numCoordinates = 2 * (numX + numY) + 1;
    CoordinatesList coordinates = new DoubleCoordinatesList(numCoordinates, 2);
    int i = 0;

    for (int j = 0; j < numX; j++) {
      coordinates.setX(i, getMaxX() - j * xStep);
      coordinates.setY(i, getMinY());
      i++;
    }
    for (int j = 0; j < numY; j++) {
      coordinates.setX(i, getMinX());
      coordinates.setY(i, getMinY() + j * yStep);
      i++;
    }
    for (int j = 0; j < numX; j++) {
      coordinates.setX(i, getMinX() + j * xStep);
      coordinates.setY(i, getMaxY());
      i++;
    }
    for (int j = 0; j < numY; j++) {
      coordinates.setX(i, getMaxX());
      coordinates.setY(i, getMinY() + (numY - j) * yStep);
      i++;
    }
    coordinates.setX(coordinates.size() - 1, getMaxX());
    coordinates.setY(coordinates.size() - 1, getMinY());
    if (geometryFactory != this.geometryFactory) {
      coordinates = CoordinatesListProjectionUtil.perform(coordinates,
        this.geometryFactory.getCoordinateSystem(),
        geometryFactory.getCoordinateSystem());
    }

    final Polygon polygon = geometryFactory.createPolygon(coordinates);
    return polygon;
  }

  public Polygon toPolygon(final int numSegments) {
    return toPolygon(numSegments, numSegments);
  }

  public Polygon toPolygon(final int numX, final int numY) {
    return toPolygon(geometryFactory, numX, numY);
  }

  @Override
  public String toString() {
    if (geometryFactory == null) {
      return "BBOX(" + getMinX() + "," + getMinY() + " " + getMaxX() + ","
        + getMaxY() + ")";
    } else {
      return "SRID=" + geometryFactory.getSrid() + ";BBOX(" + getMinX() + ","
        + getMinY() + " " + getMaxX() + "," + getMaxY() + ")";
    }
  }

}
