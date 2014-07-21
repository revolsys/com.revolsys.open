package com.revolsys.io.kml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.springframework.core.io.Resource;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.io.xml.StaxUtils;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.impl.LineStringDouble;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.util.Property;

public class KmlGeometryIterator extends AbstractIterator<Geometry> implements
Kml22Constants {
  private GeometryFactory geometryFactory = GeometryFactory.floating3(COORDINATE_SYSTEM_ID);

  private XMLStreamReader in;

  public KmlGeometryIterator(final InputStream in) {
    this.in = StaxUtils.createXmlReader(in);
  }

  public KmlGeometryIterator(final Resource resource) {
    this.in = StaxUtils.createXmlReader(resource);
  }

  @Override
  protected void doClose() {
    StaxUtils.closeSilent(this.in);
    this.geometryFactory = null;
    this.in = null;
  }

  @Override
  protected void doInit() {
    StaxUtils.skipToStartElement(this.in);
  }

  @Override
  protected Geometry getNext() {
    try {
      if (StaxUtils.skipToChildStartElements(this.in, MULTI_GEOMETRY, POINT,
        LINE_STRING, POLYGON)) {
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

  private LineString parseCoordinates() throws XMLStreamException {
    StaxUtils.requireLocalName(this.in, COORDINATES);
    final String coordinatesListString = StaxUtils.getElementText(this.in);
    if (Property.hasValue(coordinatesListString)) {
      int axisCount = 2;
      final String[] coordinatesListArray = coordinatesListString.trim().split(
        "\\s+");
      final List<Point> points = new ArrayList<>();
      for (int i = 0; i < coordinatesListArray.length; i++) {
        final String coordinatesString = coordinatesListArray[i];
        final String[] coordinatesArray = coordinatesString.split(",");
        final double[] coordinates = new double[coordinatesArray.length];
        for (int axisIndex = 0; axisIndex < coordinatesArray.length; axisIndex++) {
          final String coordinate = coordinatesArray[axisIndex];
          coordinates[axisIndex] = Double.valueOf(coordinate);
        }
        axisCount = Math.max(axisCount, coordinates.length);
        points.add(new PointDouble(coordinates));
      }
      StaxUtils.skipToEndElement(this.in);
      return new LineStringDouble(axisCount, points);
    } else {
      return null;
    }
  }

  private Geometry parseGeometry() throws XMLStreamException {
    if (StaxUtils.matchElementLocalName(this.in, MULTI_GEOMETRY)) {
      return parseMultiGeometry();
    } else if (StaxUtils.matchElementLocalName(this.in, POINT)) {
      return parsePoint();
    } else if (StaxUtils.matchElementLocalName(this.in, LINE_STRING)) {
      return parseLineString();
    } else if (StaxUtils.matchElementLocalName(this.in, POLYGON)) {
      return parsePolygon();
    } else {
      return null;
    }
  }

  private LinearRing parseLinearRing() throws XMLStreamException {
    StaxUtils.requireLocalName(this.in, LINEAR_RING);
    LineString points = null;
    if (StaxUtils.skipToChildStartElements(this.in, COORDINATES)) {
      points = parseCoordinates();
      StaxUtils.skipToEndElement(this.in);
    } else {
      StaxUtils.skipToEndElement(this.in, LINEAR_RING);
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
    StaxUtils.requireLocalName(this.in, LINE_STRING);
    LineString points = null;
    while (!StaxUtils.isEndElementLocalName(this.in, LINE_STRING)
        && this.in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (StaxUtils.matchElementLocalName(this.in, COORDINATES)) {
        points = parseCoordinates();
      } else {
        StaxUtils.skipSubTree(this.in);
      }
    }
    StaxUtils.skipToEndElementByLocalName(this.in, LINE_STRING);
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
    final List<Geometry> geometries = new ArrayList<Geometry>();
    while (StaxUtils.skipToChildStartElements(this.in, POINT, LINE_STRING,
      POLYGON)) {
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
    StaxUtils.requireLocalName(this.in, POINT);
    LineString points = null;
    while (!StaxUtils.isEndElementLocalName(this.in, POINT)
        && this.in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (points == null
        && StaxUtils.matchElementLocalName(this.in, COORDINATES)) {
        points = parseCoordinates();
      } else {
        StaxUtils.skipSubTree(this.in);
      }
    }
    StaxUtils.skipToEndElementByLocalName(this.in, POINT);
    if (points == null) {
      return this.geometryFactory.point();
    } else {
      final int axisCount = points.getAxisCount();
      final GeometryFactory geometryFactory = this.geometryFactory.convertAxisCount(axisCount);
      return geometryFactory.point(points);
    }
  }

  private Polygon parsePolygon() throws XMLStreamException {
    StaxUtils.requireLocalName(this.in, POLYGON);
    final List<LinearRing> rings = new ArrayList<LinearRing>();
    int axisCount = 2;

    if (StaxUtils.skipToChildStartElements(this.in, OUTER_BOUNDARY_IS)) {
      final LinearRing exteriorRing = parseRing();
      axisCount = Math.max(axisCount, exteriorRing.getAxisCount());
      rings.add(exteriorRing);
      StaxUtils.skipToEndElement(this.in, OUTER_BOUNDARY_IS);
      while (StaxUtils.skipToChildStartElements(this.in, INNER_BOUNDARY_IS)) {
        final LinearRing interiorRing = parseRing();
        axisCount = Math.max(axisCount, interiorRing.getAxisCount());
        rings.add(interiorRing);
        StaxUtils.skipToEndElement(this.in, INNER_BOUNDARY_IS);
      }
    }
    final GeometryFactory geometryFactory = this.geometryFactory.convertAxisCount(axisCount);
    final Polygon polygon = geometryFactory.polygon(rings);
    return polygon;
  }

  private LinearRing parseRing() throws XMLStreamException {
    if (StaxUtils.skipToChildStartElements(this.in, LINEAR_RING)) {
      final LinearRing ring = parseLinearRing();
      return ring;
    } else {
      StaxUtils.skipToEndElement(this.in);
      return null;
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return StaxUtils.toString(this.in);
  }

}
