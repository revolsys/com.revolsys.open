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
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * A BoundingBox is an {@link Envelope} with a {@link CoordinateSystem}.
 * 
 * @author Paul Austin
 */
public class BoundingBox extends Envelope {
  /** The serialization version. */
  private static final long serialVersionUID = -810356856421113732L;

  /** The coordinate system. */
  private CoordinateSystem coordinateSystem;

  private double maxZ;

  private double minZ;

  /**
   * Construct a new Bounding Box.
   * 
   * @param boundingBox The bounding box to clone.
   */
  public BoundingBox(
    final BoundingBox boundingBox) {
    super(boundingBox);
    this.coordinateSystem = boundingBox.getCoordinateSystem();
    this.minZ = boundingBox.getMinZ();
    this.maxZ = boundingBox.getMaxZ();
  }

  public BoundingBox(
    final Coordinates point) {
    this(null, point.getX(), point.getY());
  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param coordinateSystem The coordinate system.
   */
  public BoundingBox(
    final CoordinateSystem coordinateSystem) {
    this.coordinateSystem = coordinateSystem;
  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param coordinateSystem The coordinate system to convert the bounding box
   *          to.
   * @param boundingBox The bounding box.
   */
  public BoundingBox(
    final CoordinateSystem coordinateSystem,
    final BoundingBox boundingBox) {
    this.coordinateSystem = coordinateSystem;
    this.minZ = boundingBox.getMinZ();
    this.maxZ = boundingBox.getMaxZ();
    if (coordinateSystem == null) {
      throw new IllegalArgumentException(
        "A bounding box must have a coordinate system");
    } else if (boundingBox.coordinateSystem == null) {
      expandToInclude((Envelope)boundingBox);
    } else if (boundingBox.coordinateSystem.equals(coordinateSystem)) {
      this.coordinateSystem = boundingBox.coordinateSystem;
      expandToInclude((Envelope)boundingBox);
    } else {
      final Polygon polygon = boundingBox.toPolygon();
      final GeometryOperation operation = ProjectionFactory.getGeometryOperation(
        boundingBox.coordinateSystem, coordinateSystem);
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
   * @param coordinateSystem The coordinate system.
   * @param coordinate The coordinate.
   */
  public BoundingBox(
    final CoordinateSystem coordinateSystem,
    final Coordinate coordinate) {
    super(coordinate);
    this.coordinateSystem = coordinateSystem;
    this.minZ = coordinate.z;
    this.maxZ = coordinate.z;
  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param coordinateSystem The coordinate system.
   * @param coordinate1 The first coordinate.
   * @param coordinate2 The second coordinate.
   */
  public BoundingBox(
    final CoordinateSystem coordinateSystem,
    final Coordinate coordinate1,
    final Coordinate coordinate2) {
    super(coordinate1, coordinate2);
    this.coordinateSystem = coordinateSystem;
    this.minZ = Math.min(coordinate1.z, coordinate2.z);
    this.maxZ = Math.max(coordinate1.z, coordinate2.z);
  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param coordinateSystem The coordinate system.
   * @param x The x value.
   * @param y The y value.
   */
  public BoundingBox(
    final CoordinateSystem coordinateSystem,
    final double x,
    final double y) {
    this(coordinateSystem, x, y, x, y);
  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param coordinateSystem The coordinate system.
   * @param x1 The first x coordinate.
   * @param y1 The first y coordinate.
   * @param x2 The second x coordinate.
   * @param y2 The second y coordinate.
   */
  public BoundingBox(
    final CoordinateSystem coordinateSystem,
    final double x1,
    final double y1,
    final double x2,
    final double y2) {
    super(x1, x2, y1, y2);
    this.coordinateSystem = coordinateSystem;
    this.minZ = Double.NaN;
    this.maxZ = Double.NaN;
  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param coordinateSystem The coordinate system.
   * @param envelope The envelope.
   */
  public BoundingBox(
    final CoordinateSystem coordinateSystem,
    final Envelope envelope) {
    super(envelope);
    this.coordinateSystem = coordinateSystem;
    this.minZ = Double.NaN;
    this.maxZ = Double.NaN;
  }

  public BoundingBox convert(
    final CoordinateSystem coordinateSystem) {
    if (this.coordinateSystem == null || coordinateSystem == null
      || this.coordinateSystem.equals(coordinateSystem)) {
      return new BoundingBox(this);
    } else {
      final Polygon polygon = toPolygon();
      final GeometryOperation operation = ProjectionFactory.getGeometryOperation(
        this.coordinateSystem, coordinateSystem);
      if (operation != null) {
        final Polygon projectedPolygon = operation.perform(polygon);
        final Envelope envelope = projectedPolygon.getEnvelopeInternal();
        return new BoundingBox(coordinateSystem, envelope);
      } else {
        return new BoundingBox(this);
      }
    }
  }

  @Override
  public boolean equals(
    final Object other) {
    if (other instanceof BoundingBox) {
      final BoundingBox boundingBox = (BoundingBox)other;
      return coordinateSystem.equals(boundingBox.getCoordinateSystem())
        && getMaxX() == boundingBox.getMaxX()
        && getMaxY() == boundingBox.getMaxY()
        && getMinX() == boundingBox.getMinX()
        && getMinY() == boundingBox.getMinY();
    } else {
      return false;
    }
  }

  public void expandToInclude(
    final BoundingBox other) {
    super.expandToInclude(other.convert(coordinateSystem));
  }

  public void expandToInclude(
    final Coordinates coordinates) {
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
   * Get the coordinate system.
   * 
   * @return The coordinate system.
   */
  public CoordinateSystem getCoordinateSystem() {
    return coordinateSystem;
  }

  public Measurable<Length> getHeightLength() {
    final double height = getHeight();
    return Measure.valueOf(height, coordinateSystem.getLengthUnit());
  }

  public <Q extends Quantity> Measurable<Q> getMaximumX() {
    final Unit<Q> unit = coordinateSystem.getUnit();
    return Measure.valueOf(super.getMaxX(), unit);
  }

  public <Q extends Quantity> Measurable<Q> getMaximumY() {
    final Unit<Q> unit = coordinateSystem.getUnit();
    return Measure.valueOf(super.getMaxY(), unit);
  }

  public double getMaxZ() {
    return maxZ;
  }

  public <Q extends Quantity> Measurable<Q> getMinimumX() {
    final Unit<Q> unit = coordinateSystem.getUnit();
    return Measure.valueOf(super.getMinX(), unit);
  }

  public <Q extends Quantity> Measurable<Q> getMinimumY() {
    final Unit<Q> unit = coordinateSystem.getUnit();
    return Measure.valueOf(super.getMinY(), unit);
  }

  public double getMinZ() {
    return minZ;
  }

  public Measurable<Length> getWidthLength() {
    final double width = getWidth();
    return Measure.valueOf(width, coordinateSystem.getLengthUnit());
  }

  public BoundingBox intersection(
    final BoundingBox boundingBox) {
    final BoundingBox convertedBoundingBox = boundingBox.convert(coordinateSystem);
    if (isNull() || convertedBoundingBox.isNull()
      || !intersects(convertedBoundingBox)) {
      return new BoundingBox(coordinateSystem);
    } else {
      final double intMinX = Math.max(getMinX(), convertedBoundingBox.getMinX());
      final double intMinY = Math.max(getMinY(), convertedBoundingBox.getMinY());
      final double intMaxX = Math.min(getMaxX(), convertedBoundingBox.getMaxX());
      final double intMaxY = Math.min(getMaxY(), convertedBoundingBox.getMaxY());
      return new BoundingBox(coordinateSystem, intMinX, intMinY, intMaxX,
        intMaxY);
    }
  }

  public Geometry toGeometry() {
    final GeometryFactory factory = new GeometryFactory(coordinateSystem,
      new SimpleCoordinatesPrecisionModel());
    final double minX = getMinX();
    final double minY = getMinY();
    final double maxX = getMaxX();
    final double maxY = getMaxY();
    final double width = getWidth();
    final double height = getHeight();
    if (width == 0 && height == 0) {
      return factory.createPoint(new Coordinate(minX, minY));
    }

    if (width == 0 || height == 0) {
      return factory.createLineString(new Coordinate[] {
        new Coordinate(minX, minY), new Coordinate(maxX, maxY)
      });
    }

    return factory.createPolygon(
      factory.createLinearRing(new Coordinate[] {
        new Coordinate(minX, minY), new Coordinate(minX, maxY),
        new Coordinate(maxX, maxY), new Coordinate(maxX, minY),
        new Coordinate(minX, minY)
      }), null);
  }

  public Polygon toPolygon() {
    return toPolygon(100, 100);

  }

  public Polygon toPolygon(
    final GeometryFactory factory) {
    return toPolygon(factory, 100, 100);
  }

  public Polygon toPolygon(
    final GeometryFactory factory,
    final int numX,
    final int numY) {
    final CoordinateSystem coordinateSystem = factory.getCoordinateSystem();
    final double xStep = getWidth() / numX;
    final double yStep = getHeight() / numY;
    final double minX = getMinX();
    final double maxX = getMaxX();
    final double minY = getMinY();
    final double maxY = getMaxY();
    final int numCoordinates = 2 * (numX + numY) + 1;
    CoordinatesList coordinates = new DoubleCoordinatesList(numCoordinates, 2);
    for (int i = 0; i < numY; i++) {
      coordinates.setX(i, minX);
      coordinates.setY(i, minY + i * yStep);
      coordinates.setX(numY + numX + i, maxX);
      coordinates.setY(numY + numX + i, minY + (numY - i) * yStep);
    }
    for (int i = 0; i < numX; i++) {
      coordinates.setX(numY + i, minX + i * xStep);
      coordinates.setY(numY + i, maxY);
      coordinates.setX(2 * numY + numX + i, minX + (numX - i) * xStep);
      coordinates.setY(2 * numY + numX + i, minY);
    }
    coordinates.setX(coordinates.size() - 1, minX);
    coordinates.setY(coordinates.size() - 1, minY);
    if (coordinateSystem != this.coordinateSystem) {
      coordinates = CoordinatesListProjectionUtil.perform(coordinates,
        this.coordinateSystem, coordinateSystem);
    }

    coordinates.makePrecise(factory.getCoordinatesPrecisionModel());
    final LinearRing ring = factory.createLinearRing(coordinates);

    final Polygon polygon = factory.createPolygon(ring, null);
    return polygon;
  }

  public Polygon toPolygon(
    final int numSegments) {
    return toPolygon(numSegments, numSegments);
  }

  public Polygon toPolygon(
    final int numX,
    final int numY) {
    final GeometryFactory factory = new GeometryFactory(coordinateSystem,
      new SimpleCoordinatesPrecisionModel());
    return toPolygon(factory, numX, numY);
  }

  @Override
  public String toString() {
    return "(" + getMinX() + "," + getMinY() + " " + getMaxX() + ","
      + getMaxY() + ")";
  }

}
