package com.revolsys.io.kml;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.springframework.core.io.Resource;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.xml.StaxUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class KmlGeometryIterator extends AbstractIterator<Geometry> implements
  Kml22Constants {
  private final GeometryFactory geometryFactory = new GeometryFactory(
    COORDINATE_SYSTEM);

  private final XMLStreamReader in;

  public KmlGeometryIterator(final Reader reader) {
    try {
      this.in = StaxUtils.createXmlReader(reader);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to open resource " + reader);
    }
  }

  public KmlGeometryIterator(final Resource resource) {
    try {
      this.in = StaxUtils.createXmlReader(resource);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to open resource " + resource);
    }
  }

  @Override
  protected void doClose() {
    StaxUtils.closeSilent(in);
  }

  @Override
  protected void doInit() {
    try {
      // StaxUtils.skipToStartElement(this.in);
      // StaxUtils.requireLocalName(this.in, KML);
      StaxUtils.skipToStartElement(this.in);
    } catch (final XMLStreamException e) {
      throw new RuntimeException("Error initializing file ", e);
    }
  }

  @Override
  protected Geometry getNext() {
    try {
      final Geometry geometry = parseGeometry();
      if (geometry == null) {
        throw new NoSuchElementException();
      } else {
        return geometry;
      }
    } catch (final XMLStreamException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private CoordinatesList parseCoordinates() throws XMLStreamException {
    StaxUtils.requireLocalName(in, COORDINATES);
    final String coordinatesListString = StaxUtils.getElementText(in);
    final String[] coordinatesListArray = coordinatesListString.trim().split(
      "\\s+");
    final CoordinatesList coordinatesList = new DoubleCoordinatesList(
      coordinatesListArray.length, 3);
    for (int i = 0; i < coordinatesListArray.length; i++) {
      final String coordinatesString = coordinatesListArray[i];
      final String[] coordinatesArray = coordinatesString.split(",");
      for (int ordinateIndex = 0; ordinateIndex < coordinatesArray.length
        && ordinateIndex < 3; ordinateIndex++) {
        final String coordinate = coordinatesArray[ordinateIndex];
        coordinatesList.setValue(i, ordinateIndex, Double.valueOf(coordinate));
      }
    }
    StaxUtils.skipToEndElementByLocalName(in, COORDINATES);
    return coordinatesList;
  }

  private Geometry parseGeometry() throws XMLStreamException {
    if (in.getEventType() != XMLStreamConstants.START_ELEMENT) {
      StaxUtils.skipToStartElement(in);
    }
    while (in.getEventType() == XMLStreamConstants.START_ELEMENT) {
      if (StaxUtils.matchElementLocalName(in, MULTI_GEOMETRY)) {
        return parseMultiGeometry();
      } else if (StaxUtils.matchElementLocalName(in, POINT)) {
        return parsePoint();
      } else if (StaxUtils.matchElementLocalName(in, LINE_STRING)) {
        return parseLineString();
      } else if (StaxUtils.matchElementLocalName(in, POLYGON)) {
        return parsePolygon();
      } else {
        while (in.next() != XMLStreamConstants.START_ELEMENT
          && in.getEventType() != XMLStreamConstants.END_DOCUMENT) {

        }
      }
    }
    return null;
  }

  private LinearRing parseInnerBoundary() throws XMLStreamException {
    LinearRing ring = null;
    StaxUtils.requireLocalName(in, INNER_BOUNDARY_IS);
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (ring == null && StaxUtils.matchElementLocalName(in, LINEAR_RING)) {
        ring = parseLinearRing();
      } else {
        StaxUtils.skipSubTree(in);
      }
    }
    StaxUtils.skipToEndElementByLocalName(in, INNER_BOUNDARY_IS);
    return ring;
  }

  private LinearRing parseLinearRing() throws XMLStreamException {
    StaxUtils.requireLocalName(in, LINEAR_RING);
    CoordinatesList cooordinatesList = null;
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (StaxUtils.matchElementLocalName(in, COORDINATES)) {
        cooordinatesList = parseCoordinates();
      } else {
        StaxUtils.skipSubTree(in);
      }
    }
    final LinearRing ring = geometryFactory.createLinearRing(cooordinatesList);
    StaxUtils.skipToEndElementByLocalName(in, LINEAR_RING);
    return ring;
  }

  private LineString parseLineString() throws XMLStreamException {
    StaxUtils.requireLocalName(in, LINE_STRING);
    CoordinatesList cooordinatesList = null;
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (StaxUtils.matchElementLocalName(in, COORDINATES)) {
        cooordinatesList = parseCoordinates();
      } else {
        StaxUtils.skipSubTree(in);
      }
    }
    final LineString lineString = geometryFactory.createLineString(cooordinatesList);
    StaxUtils.skipToEndElementByLocalName(in, LINE_STRING);
    return lineString;
  }

  private Geometry parseMultiGeometry() throws XMLStreamException {
    StaxUtils.requireLocalName(in, MULTI_GEOMETRY);
    final List<Geometry> geometries = new ArrayList<Geometry>();
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Geometry geometry = parseGeometry();
      if (geometry != null) {
        geometries.add(geometry);
      }
    }
    final Geometry geometryCollection = geometryFactory.createGeometry(geometries);
    StaxUtils.skipToEndElementByLocalName(in, MULTI_GEOMETRY);
    return geometryCollection;
  }

  private LinearRing parseOuterBoundary() throws XMLStreamException {
    StaxUtils.requireLocalName(in, OUTER_BOUNDARY_IS);
    LinearRing ring = null;
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (ring == null && StaxUtils.matchElementLocalName(in, LINEAR_RING)) {
        ring = parseLinearRing();
      } else {
        StaxUtils.skipSubTree(in);
      }
    }
    StaxUtils.skipToEndElementByLocalName(in, OUTER_BOUNDARY_IS);
    return ring;
  }

  private Point parsePoint() throws XMLStreamException {
    StaxUtils.requireLocalName(in, POINT);
    CoordinatesList cooordinatesList = null;
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (cooordinatesList == null
        && StaxUtils.matchElementLocalName(in, COORDINATES)) {
        cooordinatesList = parseCoordinates();
      } else {
        StaxUtils.skipSubTree(in);
      }
    }
    final Point point = geometryFactory.createPoint(cooordinatesList);
    StaxUtils.skipToEndElementByLocalName(in, POINT);
    return point;
  }

  private Polygon parsePolygon() throws XMLStreamException {
    StaxUtils.requireLocalName(in, POLYGON);
    final List<LinearRing> rings = new ArrayList<LinearRing>();
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (rings.isEmpty()) {
        if (StaxUtils.matchElementLocalName(in, OUTER_BOUNDARY_IS)) {
          rings.add(parseOuterBoundary());
        } else {
          StaxUtils.skipSubTree(in);
        }
      } else if (StaxUtils.matchElementLocalName(in, INNER_BOUNDARY_IS)) {
        final LinearRing innerRing = parseInnerBoundary();
        if (innerRing != null) {
          rings.add(innerRing);
        }
      } else {
        StaxUtils.skipSubTree(in);
      }
    }
    final Polygon polygon = geometryFactory.createPolygon(rings);
    StaxUtils.skipToEndElementByLocalName(in, POLYGON);
    return polygon;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
