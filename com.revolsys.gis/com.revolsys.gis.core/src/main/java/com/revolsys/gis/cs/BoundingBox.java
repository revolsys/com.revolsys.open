package com.revolsys.gis.cs;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import com.revolsys.gis.cs.projection.CoordinatesListProjectionUtil;
import com.revolsys.gis.cs.projection.GeometryOperation;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * A BoundingBox is an {@link Envelope} with a {@link CoordinateSystem}.
 * 
 * @author Paul Austin
 */
public class BoundingBox extends Envelope {
  /** The serialization version. */
  private static final long serialVersionUID = -810356856421113732L;

  public static BoundingBox getBoundingBox(final Geometry geometry) {
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
    return new BoundingBox(geometryFactory, geometry.getEnvelopeInternal());
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

  private GeometryFactory geometryFactory;

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

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public BoundingBox(final Coordinates point) {
    this(null, point.getX(), point.getY());
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
    super(x1, x2, y1, y2);
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

  public BoundingBox convert(final GeometryFactory geometryFactory) {
    if (this.geometryFactory == null || geometryFactory == null
      || this.geometryFactory.equals(geometryFactory)) {
      return new BoundingBox(this);
    } else {
      final Polygon polygon = toPolygon();
      final GeometryOperation operation = ProjectionFactory.getGeometryOperation(
        this.geometryFactory, geometryFactory);
      if (operation != null) {
        final Polygon projectedPolygon = operation.perform(polygon);
        final Envelope envelope = projectedPolygon.getEnvelopeInternal();
        return new BoundingBox(geometryFactory, envelope);
      } else {
        return new BoundingBox(this);
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

  public void expandToInclude(final BoundingBox other) {
    super.expandToInclude(other.convert(geometryFactory));
  }

  public void expandToInclude(final Coordinates coordinates) {
    final double x = coordinates.getX();
    final double y = coordinates.getY();
    super.expandToInclude(x, y);
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

  public Measurable<Length> getHeightLength() {
    final double height = getHeight();
    return Measure.valueOf(height, getCoordinateSystem().getLengthUnit());
  }

  public <Q extends Quantity> Measurable<Q> getMaximumX() {
    final Unit<Q> unit = getCoordinateSystem().getUnit();
    return Measure.valueOf(super.getMaxX(), unit);
  }

  public <Q extends Quantity> Measurable<Q> getMaximumY() {
    final Unit<Q> unit = getCoordinateSystem().getUnit();
    return Measure.valueOf(super.getMaxY(), unit);
  }

  public double getMaxZ() {
    return maxZ;
  }

  public <Q extends Quantity> Measurable<Q> getMinimumX() {
    final Unit<Q> unit = getCoordinateSystem().getUnit();
    return Measure.valueOf(super.getMinX(), unit);
  }

  public <Q extends Quantity> Measurable<Q> getMinimumY() {
    final Unit<Q> unit = getCoordinateSystem().getUnit();
    return Measure.valueOf(super.getMinY(), unit);
  }

  public double getMinZ() {
    return minZ;
  }

  public Measurable<Length> getWidthLength() {
    final double width = getWidth();
    return Measure.valueOf(width, getCoordinateSystem().getLengthUnit());
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
    return "(" + getMinX() + "," + getMinY() + " " + getMaxX() + ","
      + getMaxY() + ")";
  }
}
