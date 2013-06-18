package com.revolsys.gis.cs;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.cs.projection.CoordinatesListProjectionUtil;
import com.revolsys.gis.cs.projection.GeometryOperation;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * A BoundingBox is an {@link Envelope} with a {@link CoordinateSystem}.
 * 
 * @author Paul Austin
 */
public class BoundingBox extends Envelope implements Cloneable {

  /** The serialization version. */
  private static final long serialVersionUID = -810356856421113732L;

  public static BoundingBox getBoundingBox(final DataObject object) {
    if (object == null) {
      return new BoundingBox();
    } else {
      final Geometry geometry = object.getGeometryValue();
      return getBoundingBox(geometry);
    }
  }

  public static BoundingBox getBoundingBox(final Geometry geometry) {
    if (geometry == null) {
      return new BoundingBox();
    } else {
      final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
      final Envelope envelope = geometry.getEnvelopeInternal();
      return new BoundingBox(geometryFactory, envelope);
    }
  }

  public static BoundingBox getBoundingBox(
    final GeometryFactory geometryFactory, final DataObject object) {
    final BoundingBox boundingBox = getBoundingBox(object);
    return boundingBox.convert(geometryFactory);
  }

  public static BoundingBox getBoundingBox(
    final GeometryFactory geometryFactory, final Geometry geometry) {
    final BoundingBox boundingBox = getBoundingBox(geometry);
    return boundingBox.convert(geometryFactory);
  }

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

  public static BoundingBox parse(final String bbox) {
    final String[] args = bbox.split(",");
    if (args.length == 4) {
      final double x1 = Double.valueOf(args[0]);
      final double y1 = Double.valueOf(args[1]);
      final double x2 = Double.valueOf(args[2]);
      final double y2 = Double.valueOf(args[3]);
      return new BoundingBox(GeometryFactory.getFactory(4326), x1, y1, x2, y2);
    } else {
      throw new IllegalArgumentException(
        "BBOX must have match <minX>,<minY>,<maxX>,<maxY> not " + bbox);
    }
  }

  private double maxZ;

  private double minZ;

  private GeometryFactory geometryFactory = GeometryFactory.getFactory();

  public BoundingBox() {
    this(4326);
  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param boundingBox The bounding box to clone.
   */
  public BoundingBox(final BoundingBox boundingBox) {
    super(boundingBox);
    this.geometryFactory = boundingBox.getGeometryFactory();
    this.minZ = boundingBox.getMinZ();
    this.maxZ = boundingBox.getMaxZ();
  }

  public BoundingBox(final Coordinates point) {
    this(GeometryFactory.getFactory(), point.getX(), point.getY());
  }

  public BoundingBox(final Coordinates point1, final Coordinates point2) {
    this(GeometryFactory.getFactory(), point1.getX(), point1.getY(),
      point2.getX(), point2.getY());
  }

  public BoundingBox(final double x, final double y) {
    this(GeometryFactory.getFactory(0), x, y);
  }

  public BoundingBox(final Envelope envelope) {
    super(envelope);
  }

  public BoundingBox(final Geometry geometry) {
    this(GeometryFactory.getFactory(geometry), geometry.getEnvelopeInternal());
  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param geometryFactory The geometry factory.
   */
  public BoundingBox(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param geometryFactory The geometry factory to convert the bounding box to.
   * @param boundingBox The bounding box.
   */
  public BoundingBox(final GeometryFactory geometryFactory,
    final BoundingBox boundingBox) {
    this.geometryFactory = geometryFactory;
    this.minZ = boundingBox.getMinZ();
    this.maxZ = boundingBox.getMaxZ();
    if (geometryFactory == null) {
      throw new IllegalArgumentException(
        "A bounding box must have a geometry factory");
    } else if (boundingBox.geometryFactory == null) {
      expandToInclude((Envelope)boundingBox);
    } else if (boundingBox.geometryFactory.equals(geometryFactory)) {
      this.geometryFactory = boundingBox.geometryFactory;
      expandToInclude((Envelope)boundingBox);
    } else {
      final Polygon polygon = boundingBox.toPolygon();
      final GeometryOperation operation = ProjectionFactory.getGeometryOperation(
        boundingBox.geometryFactory, geometryFactory);
      if (operation != null) {
        final Polygon projectedPolygon = operation.perform(polygon);
        final Envelope envelope = projectedPolygon.getEnvelopeInternal();
        expandToInclude(envelope);
      } else {
        expandToInclude((Envelope)boundingBox);
      }
    }
  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param geometryFactory The geometry factory.
   * @param coordinate The coordinate.
   */
  public BoundingBox(final GeometryFactory geometryFactory,
    final Coordinate coordinate) {
    super(coordinate);
    this.geometryFactory = geometryFactory;
    this.minZ = coordinate.z;
    this.maxZ = coordinate.z;
  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param geometryFactory The geometry factory.
   * @param coordinate1 The first coordinate.
   * @param coordinate2 The second coordinate.
   */
  public BoundingBox(final GeometryFactory geometryFactory,
    final Coordinate coordinate1, final Coordinate coordinate2) {
    super(coordinate1, coordinate2);
    this.geometryFactory = geometryFactory;
    this.minZ = Math.min(coordinate1.z, coordinate2.z);
    this.maxZ = Math.max(coordinate1.z, coordinate2.z);
  }

  public BoundingBox(final GeometryFactory geometryFactory,
    final Coordinates point) {
    this(geometryFactory, point.getX(), point.getY());
  }

  public BoundingBox(final GeometryFactory geometryFactory,
    final Coordinates point1, final Coordinates point2) {
    this(geometryFactory, point1.getX(), point1.getY(), point2.getX(),
      point2.getY());
  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param geometryFactory The geometry factory.
   * @param x The x value.
   * @param y The y value.
   */
  public BoundingBox(final GeometryFactory geometryFactory, final double x,
    final double y) {
    this(geometryFactory, x, y, x, y);
  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param geometryFactory The geometry factory.
   * @param x1 The first x coordinate.
   * @param y1 The first y coordinate.
   * @param x2 The second x coordinate.
   * @param y2 The second y coordinate.
   */
  public BoundingBox(final GeometryFactory geometryFactory, final double x1,
    final double y1, final double x2, final double y2) {
    super(geometryFactory.makePrecise(x1), geometryFactory.makePrecise(x2),
      geometryFactory.makePrecise(y1), geometryFactory.makePrecise(y2));
    this.geometryFactory = geometryFactory;
    this.minZ = Double.NaN;
    this.maxZ = Double.NaN;
  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param geometryFactory The geometry factory.
   * @param envelope The envelope.
   */
  public BoundingBox(final GeometryFactory geometryFactory,
    final Envelope envelope) {
    super(envelope);
    this.geometryFactory = geometryFactory;
    this.minZ = Double.NaN;
    this.maxZ = Double.NaN;
  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param geometryFactory The geometry factory.
   */
  public BoundingBox(final int srid) {
    this.geometryFactory = GeometryFactory.getFactory(srid);
  }

  public BoundingBox(final Point p1, final Point p2) {
    this(GeometryFactory.getFactory(p1), CoordinatesUtil.get(p1),
      CoordinatesUtil.get(p2));
  }

  public BoundingBox clipToCoordinateSystem() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    if (coordinateSystem == null) {
      return this;
    } else {
      final BoundingBox areaBoundingBox = coordinateSystem.getAreaBoundingBox();
      return intersection(areaBoundingBox);
    }
  }

  @Override
  public BoundingBox clone() {
    try {
      return (BoundingBox)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean contains(final Coordinates coordinate) {
    final double x = coordinate.getX();
    final double y = coordinate.getY();
    return contains(x, y);
  }

  public boolean contains(final Point point) {
    final Point projectedPoint = (Point)geometryFactory.createGeometry(point);
    final Coordinates coordinates = CoordinatesUtil.get(projectedPoint);
    return contains(coordinates);
  }

  public BoundingBox convert(final GeometryFactory geometryFactory) {
    if (this.geometryFactory == null || geometryFactory == null
      || this.geometryFactory.equals(geometryFactory) || isNull()) {
      return this;
    } else {
      final Polygon polygon = toPolygon();
      final GeometryOperation operation = ProjectionFactory.getGeometryOperation(
        this.geometryFactory, geometryFactory);
      if (operation != null) {
        final Polygon projectedPolygon = operation.perform(polygon);
        final Envelope envelope = projectedPolygon.getEnvelopeInternal();
        return new BoundingBox(geometryFactory, envelope);
      } else {
        return this;
      }
    }
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

  public BoundingBox expand(final double delta) {
    return expand(delta, delta);
  }

  public BoundingBox expand(final double deltaX, final double deltaY) {
    if (isNull() || (deltaX == 0 && deltaY == 0)) {
      return this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final double x1 = getMinX() - deltaX;
      final double x2 = getMaxX() + deltaX;
      final double y1 = getMinY() - deltaY;
      final double y2 = getMaxY() + deltaY;

      if (x1 > x2 || y1 > y2) {
        return new BoundingBox(geometryFactory);
      } else {
        return new BoundingBox(geometryFactory, x1, y1, x2, y2);
      }
    }
  }

  public BoundingBox expandPercent(final double factor) {
    return expandPercent(factor, factor);
  }

  public BoundingBox expandPercent(final double factorX, final double factorY) {
    if (isNull()) {
      return this;
    } else {
      final double deltaX = getWidth() * factorX / 2;
      final double deltaY = getHeight() * factorY / 2;
      return expand(deltaX, deltaY);
    }
  }

  public BoundingBox expandToInclude(final BoundingBox other) {
    if (other.isNull()) {
      return this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final BoundingBox convertedOther = other.convert(geometryFactory);
      if (isNull()) {
        return convertedOther;
      } else if (contains(convertedOther)) {
        return this;
      } else {
        final double minX = Math.min(getMinX(), other.getMinX());
        final double maxX = Math.min(getMaxX(), other.getMaxX());
        final double minY = Math.min(getMinY(), other.getMinY());
        final double maxY = Math.min(getMaxY(), other.getMaxY());
        return new BoundingBox(geometryFactory, minX, minY, maxX, maxY);
      }
    }
  }

  public BoundingBox expandToInclude(final Coordinates coordinates) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isNull()) {
      return new BoundingBox(geometryFactory, coordinates);
    } else {
      final double x = coordinates.getX();
      final double y = coordinates.getY();

      double minX = getMinX();
      double maxX = getMaxX();
      double minY = getMinY();
      double maxY = getMaxY();

      if (x < minX) {
        minX = x;
      }
      if (x > maxX) {
        maxX = x;
      }
      if (y < minY) {
        minY = y;
      }
      if (y > maxY) {
        maxY = y;
      }
      return new BoundingBox(geometryFactory, minX, minY, maxX, maxY);
    }
  }

  public BoundingBox expandToInclude(final Geometry geometry) {
    if (geometry == null) {
      return this;
    } else {
      final BoundingBox box = getBoundingBox(geometry);
      return expandToInclude(box);
    }
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

  public Point getBottomLeftPoint() {
    return getGeometryFactory().createPoint(getMinX(), getMinY());
  }

  public Point getBottomRightPoint() {
    return getGeometryFactory().createPoint(getMaxX(), getMinY());
  }

  public Coordinates getCentre() {
    return new DoubleCoordinates(getCentreX(), getCentreY());
  }

  public Point getCentrePoint() {
    return geometryFactory.createPoint(getCentre());
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

  public Coordinates getCornerPoint(int index) {
    final double minX = getMinX();
    final double maxX = getMaxX();
    final double minY = getMinY();
    final double maxY = getMaxY();
    index = index % 4;
    switch (index) {
      case 0:
        return new DoubleCoordinates(maxX, minY);
      case 1:
        return new DoubleCoordinates(minX, minY);
      case 2:
        return new DoubleCoordinates(minX, maxY);
      default:
        return new DoubleCoordinates(maxX, maxY);
    }
  }

  public CoordinatesList getCornerPoints() {
    final double minX = getMinX();
    final double maxX = getMaxX();
    final double minY = getMinY();
    final double maxY = getMaxY();
    return new DoubleCoordinatesList(2, maxX, minY, minX, minY, minX, maxX,
      maxX, maxY);
  }

  public LineSegment getEastLine() {
    return new LineSegment(getGeometryFactory(), getMaxX(), getMinY(),
      getMaxX(), getMaxY());
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public Measure<Length> getHeightLength() {
    final double height = getHeight();
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    if (coordinateSystem == null) {
      return Measure.valueOf(height, SI.METRE);
    } else {
      return Measure.valueOf(height, coordinateSystem.getLengthUnit());
    }
  }

  public String getId() {
    final String string = MathUtil.toString(getMinX()) + "_"
      + MathUtil.toString(getMinY()) + "_" + MathUtil.toString(getMaxX()) + "_"
      + MathUtil.toString(getMaxY());
    if (geometryFactory == null) {
      return string;
    } else {
      return geometryFactory.getSRID() + "-" + string;
    }
  }

  public <Q extends Quantity> Measurable<Q> getMaximumX() {
    final Unit<Q> unit = getCoordinateSystem().getUnit();
    return Measure.valueOf(super.getMaxX(), unit);
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public <Q extends Quantity> double getMaximumX(final Unit convertUnit) {
    return getMaximumX().doubleValue(convertUnit);
  }

  public <Q extends Quantity> Measurable<Q> getMaximumY() {
    final Unit<Q> unit = getCoordinateSystem().getUnit();
    return Measure.valueOf(super.getMaxY(), unit);
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public <Q extends Quantity> double getMaximumY(final Unit convertUnit) {
    return getMaximumY().doubleValue(convertUnit);
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
    return Measure.valueOf(super.getMinX(), unit);
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public <Q extends Quantity> double getMinimumX(final Unit convertUnit) {
    return getMinimumX().doubleValue(convertUnit);
  }

  public <Q extends Quantity> Measurable<Q> getMinimumY() {
    final Unit<Q> unit = getCoordinateSystem().getUnit();
    return Measure.valueOf(super.getMinY(), unit);
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public <Q extends Quantity> double getMinimumY(final Unit convertUnit) {
    return getMinimumY().doubleValue(convertUnit);
  }

  public double getMinZ() {
    if (Double.isNaN(minZ)) {
      return 0;
    } else {
      return minZ;
    }
  }

  public LineSegment getNorthLine() {
    return new LineSegment(getGeometryFactory(), getMinX(), getMaxY(),
      getMaxX(), getMaxY());
  }

  public LineSegment getSouthLine() {
    return new LineSegment(getGeometryFactory(), getMinX(), getMinY(),
      getMaxX(), getMinY());
  }

  public Point getTopLeftPoint() {
    return getGeometryFactory().createPoint(getMinX(), getMaxY());
  }

  public Point getTopRightPoint() {
    return getGeometryFactory().createPoint(getMaxX(), getMaxY());
  }

  public LineSegment getWestLine() {
    return new LineSegment(getGeometryFactory(), getMinX(), getMinY(),
      getMinX(), getMaxY());
  }

  public Measure<Length> getWidthLength() {
    final double width = getWidth();
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    if (coordinateSystem == null) {
      return Measure.valueOf(width, SI.METRE);
    } else {
      return Measure.valueOf(width, coordinateSystem.getLengthUnit());
    }
  }

  public BoundingBox intersection(final BoundingBox boundingBox) {
    final BoundingBox convertedBoundingBox = boundingBox.convert(geometryFactory);
    if (isNull() || convertedBoundingBox.isNull()
      || !intersects((Envelope)convertedBoundingBox)) {
      return new BoundingBox(geometryFactory);
    } else {
      final double intMinX = Math.max(getMinX(), convertedBoundingBox.getMinX());
      final double intMinY = Math.max(getMinY(), convertedBoundingBox.getMinY());
      final double intMaxX = Math.min(getMaxX(), convertedBoundingBox.getMaxX());
      final double intMaxY = Math.min(getMaxY(), convertedBoundingBox.getMaxY());
      return new BoundingBox(geometryFactory, intMinX, intMinY, intMaxX,
        intMaxY);
    }
  }

  public boolean intersects(final BoundingBox boundingBox) {
    final BoundingBox convertedBoundingBox = boundingBox.convert(geometryFactory);
    return intersects((Envelope)convertedBoundingBox);
  }

  /**
   * <p>Create a new BoundingBox by moving the min/max x coordinates by xDisplacement and
   * the min/max y coordinates by yDisplacement. If the bounding box is null or the xDisplacement
   * and yDisplacement are 0 then this bounding box will be returned.</p>
   * 
   * @param xDisplacement The distance to move the min/max x coordinates.
   * @param yDisplacement The distance to move the min/max y coordinates.
   * @return The moved bounding box.
   */
  public BoundingBox move(final double xDisplacement, final double yDisplacement) {
    if (isNull() || (xDisplacement == 0 && yDisplacement == 0)) {
      return this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final double x1 = getMinX() + xDisplacement;
      final double x2 = getMaxX() + xDisplacement;
      final double y1 = getMinY() + yDisplacement;
      final double y2 = getMaxY() + yDisplacement;
      return new BoundingBox(geometryFactory, x1, y1, x2, y2);
    }
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public void setMaxZ(final double maxZ) {
    if (isNull()) {
      this.minZ = maxZ;
      this.maxZ = maxZ;
    }
    if (maxZ < this.minZ) {
      this.minZ = maxZ;
    }
    if (maxZ > this.maxZ) {
      this.maxZ = maxZ;
    }
  }

  public void setMinZ(final double minZ) {
    if (isNull()) {
      this.minZ = minZ;
      this.maxZ = minZ;
    }
    if (minZ < this.minZ) {
      this.minZ = minZ;
    }
    if (minZ > this.maxZ) {
      this.maxZ = minZ;
    }
  }

  public Geometry toGeometry() {
    final double minX = getMinX();
    final double minY = getMinY();
    final double maxX = getMaxX();
    final double maxY = getMaxY();
    final double width = getWidth();
    final double height = getHeight();
    if (width == 0 && height == 0) {
      return geometryFactory.createPoint(new DoubleCoordinatesList(2, minX,
        minY));
    } else if (width == 0 || height == 0) {
      return geometryFactory.createLineString(new DoubleCoordinatesList(2,
        minX, minY, maxX, maxY));
    } else {
      return geometryFactory.createPolygon(new DoubleCoordinatesList(2, minX,
        minY, minX, maxY, maxX, maxY, maxX, minY, minX, minY));
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
    final double minX = getMinX();
    final double maxX = getMaxX();
    final double minY = getMinY();
    final double maxY = getMaxY();
    final int numCoordinates = 2 * (numX + numY) + 1;
    CoordinatesList coordinates = new DoubleCoordinatesList(numCoordinates, 2);
    int i = 0;

    for (int j = 0; j < numX; j++) {
      coordinates.setX(i, maxX - j * xStep);
      coordinates.setY(i, minY);
      i++;
    }
    for (int j = 0; j < numY; j++) {
      coordinates.setX(i, minX);
      coordinates.setY(i, minY + j * yStep);
      i++;
    }
    for (int j = 0; j < numX; j++) {
      coordinates.setX(i, minX + j * xStep);
      coordinates.setY(i, maxY);
      i++;
    }
    for (int j = 0; j < numY; j++) {
      coordinates.setX(i, maxX);
      coordinates.setY(i, minY + (numY - j) * yStep);
      i++;
    }
    coordinates.setX(coordinates.size() - 1, maxX);
    coordinates.setY(coordinates.size() - 1, minY);
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
    final StringBuffer s = new StringBuffer();
    if (geometryFactory != null) {
      s.append("SRID=");
      s.append(geometryFactory.getSRID());
      s.append(";");
    }
    s.append("BBOX(");
    s.append(StringConverterRegistry.toString(getMinX()));
    s.append(',');
    s.append(StringConverterRegistry.toString(getMinY()));
    s.append(' ');
    s.append(StringConverterRegistry.toString(getMaxX()));
    s.append(',');
    s.append(StringConverterRegistry.toString(getMaxY()));
    s.append(')');
    return s.toString();
  }
}
