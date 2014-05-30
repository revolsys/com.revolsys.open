package com.revolsys.jts.geom.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.revolsys.gis.data.io.IteratorReader;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.Reader;
import com.revolsys.jts.geom.Dimension;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.segment.Segment;
import com.revolsys.jts.geom.vertex.MultiPointVertex;
import com.revolsys.jts.geom.vertex.Vertex;

public abstract class AbstractMultiPoint extends AbstractGeometryCollection
  implements MultiPoint {

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V copy(final GeometryFactory geometryFactory) {
    final List<Point> newPoints = new ArrayList<>();
    final List<Point> points = getPoints();
    for (final Point point : points) {
      final Point newPoint = point.copy(geometryFactory);
      newPoints.add(newPoint);
    }
    return (V)geometryFactory.multiPoint(newPoints);
  }

  @Override
  public boolean equalsExact(final Geometry other, final double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    }
    return super.equalsExact(other, tolerance);
  }

  /**
   * Gets the boundary of this geometry.
   * Zero-dimensional geometries have no boundary by definition,
   * so an empty GeometryCollection is returned.
   *
   * @return an empty GeometryCollection
   * @see Geometry#getBoundary
   */
  @Override
  public Geometry getBoundary() {
    return getGeometryFactory().geometryCollection();
  }

  @Override
  public int getBoundaryDimension() {
    return Dimension.FALSE;
  }

  /**
   *  Returns the <code>Coordinate</code> at the given position.
   *
   *@param  n  the partIndex of the <code>Coordinate</code> to retrieve, beginning
   *      at 0
   *@return    the <code>n</code>th <code>Coordinate</code>
   */
  protected Point getCoordinate(final int n) {
    return getPoint(n);
  }

  @Override
  public double getCoordinate(final int partIndex, final int vertexIndex) {
    final Point point = getPoint(partIndex);
    return point.getCoordinate(vertexIndex);
  }

  @Override
  public DataType getDataType() {
    return DataTypes.MULTI_POINT;
  }

  @Override
  public int getDimension() {
    return 0;
  }

  @Override
  public Point getPoint(final int partIndex) {
    return (Point)getGeometry(partIndex);
  }

  /**
   * @author Paul Austin <paul.austin@revolsys.com>
   */
  @Override
  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public <V extends Point> List<V> getPoints() {
    return (List)getGeometries();
  }

  @Override
  public Segment getSegment(final int... segmentId) {
    return null;
  }

  @Override
  public Vertex getVertex(final int... vertexId) {
    if (vertexId.length != 1 || vertexId[0] < 0
      || vertexId[0] >= getGeometryCount()) {
      return null;
    } else {
      return new MultiPointVertex(this, vertexId);
    }
  }

  @Override
  protected boolean isEquivalentClass(final Geometry other) {
    return other instanceof MultiPoint;
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public MultiPoint normalize() {
    if (isEmpty()) {
      return this;
    } else {
      final List<Point> geometries = new ArrayList<>();
      for (final Geometry part : geometries()) {
        final Point normalizedPart = (Point)part.normalize();
        geometries.add(normalizedPart);
      }
      Collections.sort(geometries);
      final GeometryFactory geometryFactory = getGeometryFactory();
      final MultiPoint normalizedGeometry = geometryFactory.multiPoint(geometries);
      return normalizedGeometry;
    }
  }

  @Override
  public IteratorReader<Segment> segments() {
    return new IteratorReader<Segment>();
  }

  @Override
  public Reader<Vertex> vertices() {
    final MultiPointVertex vertex = new MultiPointVertex(this, -1);
    return vertex.reader();
  }
}
