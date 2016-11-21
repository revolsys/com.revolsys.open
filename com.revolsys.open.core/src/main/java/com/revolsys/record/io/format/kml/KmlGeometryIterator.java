package com.revolsys.record.io.format.kml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLStreamException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.record.io.format.xml.StaxReader;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;

public class KmlGeometryIterator extends AbstractIterator<Geometry>
  implements GeometryReader, Kml22Constants {
  private GeometryFactory geometryFactory = GeometryFactory.floating3(COORDINATE_SYSTEM_ID);

  private StaxReader in;

  public KmlGeometryIterator(final InputStream in) {
    this.in = StaxReader.newXmlReader(in);
  }

  public KmlGeometryIterator(final Resource resource) {
    this.in = StaxReader.newXmlReader(resource);
  }

  @Override
  protected void closeDo() {
    if (this.in != null) {
      this.in.close();
    }
    this.geometryFactory = null;
    this.in = null;
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  protected Geometry getNext() {
    try {
      final int depth = 0;
      if (this.in.skipToStartElements(depth, MULTI_GEOMETRY, POINT, LINE_STRING, POLYGON)) {
        final Geometry geometry = parseGeometry();
        if (geometry == null) {
          throw new NoSuchElementException();
        } else {
          return geometry;
        }
      } else {
        throw new NoSuchElementException();
      }
    } catch (final XMLStreamException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  protected void initDo() {
    this.in.skipToStartElement();
  }

  private LineString parseCoordinates() throws XMLStreamException {
    this.in.requireLocalName(COORDINATES);
    final String coordinatesListString = this.in.getElementText();
    if (Property.hasValue(coordinatesListString)) {
      int axisCount = 2;
      final String[] coordinatesListArray = coordinatesListString.trim().split("\\s+");
      final List<Point> points = new ArrayList<>();
      for (final String coordinatesString : coordinatesListArray) {
        final String[] coordinatesArray = coordinatesString.split(",");
        final double[] coordinates = new double[coordinatesArray.length];
        for (int axisIndex = 0; axisIndex < coordinatesArray.length; axisIndex++) {
          final String coordinate = coordinatesArray[axisIndex];
          coordinates[axisIndex] = Double.valueOf(coordinate);
        }
        axisCount = Math.max(axisCount, coordinates.length);
        points.add(new PointDouble(coordinates));
      }
      this.in.skipToEndElement();
      return new LineStringDouble(axisCount, points);
    } else {
      return null;
    }
  }

  private Geometry parseGeometry() throws XMLStreamException {
    if (this.in.isStartElementLocalName(MULTI_GEOMETRY)) {
      return parseMultiGeometry();
    } else if (this.in.isStartElementLocalName(POINT)) {
      return parsePoint();
    } else if (this.in.isStartElementLocalName(LINE_STRING)) {
      return parseLineString();
    } else if (this.in.isStartElementLocalName(POLYGON)) {
      return parsePolygon();
    } else {
      return null;
    }
  }

  private LinearRing parseLinearRing() throws XMLStreamException {
    this.in.requireLocalName(LINEAR_RING);
    LineString points = null;
    final int depth = this.in.getDepth();
    while (this.in.skipToStartElements(depth, COORDINATES)) {
      if (points == null && this.in.isStartElementLocalName(COORDINATES)) {
        points = parseCoordinates();
        this.in.skipToEndElement();
      }
    }
    if (points == null) {
      return this.geometryFactory.linearRing();
    } else {
      final int axisCount = points.getAxisCount();
      final GeometryFactory geometryFactory = this.geometryFactory.convertAxisCount(axisCount);
      return geometryFactory.linearRing(points);
    }
  }

  private LineString parseLineString() throws XMLStreamException {
    this.in.requireLocalName(LINE_STRING);
    LineString points = null;
    final int depth = this.in.getDepth();
    while (this.in.skipToStartElements(depth, COORDINATES)) {
      if (points == null && this.in.isStartElementLocalName(COORDINATES)) {
        points = parseCoordinates();
      }
    }
    if (points == null) {
      return this.geometryFactory.lineString();
    } else {
      final int axisCount = points.getAxisCount();
      final GeometryFactory geometryFactory = this.geometryFactory.convertAxisCount(axisCount);
      return geometryFactory.lineString(points);
    }
  }

  private Geometry parseMultiGeometry() throws XMLStreamException {
    int axisCount = 2;
    final List<Geometry> geometries = new ArrayList<>();
    while (this.in.skipToChildStartElements(POINT, LINE_STRING, POLYGON)) {
      final Geometry geometry = parseGeometry();
      if (geometry != null) {
        axisCount = Math.max(axisCount, geometry.getAxisCount());
        geometries.add(geometry);
      }
    }
    final GeometryFactory geometryFactory = this.geometryFactory.convertAxisCount(axisCount);
    final Geometry geometryCollection = geometryFactory.geometry(geometries);

    return geometryCollection;
  }

  private Point parsePoint() throws XMLStreamException {
    this.in.requireLocalName(POINT);
    LineString points = null;
    final int depth = this.in.getDepth();
    while (this.in.skipToStartElements(depth, COORDINATES)) {
      if (points == null && this.in.isStartElementLocalName(COORDINATES)) {
        points = parseCoordinates();
      }
    }
    if (points == null) {
      return this.geometryFactory.point();
    } else {
      final int axisCount = points.getAxisCount();
      final GeometryFactory geometryFactory = this.geometryFactory.convertAxisCount(axisCount);
      return geometryFactory.point(points);
    }
  }

  private Polygon parsePolygon() throws XMLStreamException {
    this.in.requireLocalName(POLYGON);
    final List<LinearRing> rings = new ArrayList<>();
    int axisCount = 2;
    final int depth = this.in.getDepth();
    while (this.in.skipToStartElements(depth, OUTER_BOUNDARY_IS, INNER_BOUNDARY_IS)) {
      final LinearRing ring = parseRing();
      if (ring != null) {
        axisCount = Math.max(axisCount, ring.getAxisCount());
        rings.add(ring);
      }
    }
    final GeometryFactory geometryFactory = this.geometryFactory.convertAxisCount(axisCount);
    final Polygon polygon = geometryFactory.polygon(rings);
    return polygon;
  }

  private LinearRing parseRing() throws XMLStreamException {
    final int depth = this.in.getDepth();
    while (this.in.skipToStartElements(depth, LINEAR_RING)) {
      final LinearRing ring = parseLinearRing();
      return ring;
    }
    return null;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return this.in.toString();
  }

}
