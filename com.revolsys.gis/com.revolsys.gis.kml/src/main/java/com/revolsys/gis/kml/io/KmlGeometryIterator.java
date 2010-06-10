package com.revolsys.gis.kml.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.xml.io.StaxUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

@SuppressWarnings("restriction")
public class KmlGeometryIterator implements Iterator<Geometry> {
  private static final XMLInputFactory FACTORY = XMLInputFactory.newInstance();

  private static final Logger log = LoggerFactory.getLogger(KmlGeometryIterator.class);

  private static XMLStreamReader createXmlReader(
    final Resource resource)
    throws IOException {
    try {
      return FACTORY.createXMLStreamReader(resource.getInputStream());
    } catch (final XMLStreamException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }

  private final GeometryFactory geometryFactory = new GeometryFactory(
    Kml22Constants.COORDINATE_SYSTEM, new SimpleCoordinatesPrecisionModel());

  private Geometry currentGeometry;

  private boolean hasNext = true;

  private final XMLStreamReader in;

  private boolean loadNextObject = true;

  public KmlGeometryIterator(
    final Resource resource) {
    try {
      this.in = createXmlReader(resource);
      StaxUtils.skipToStartElement(this.in);
      StaxUtils.requireLocalName(this.in, Kml22Constants.KML);
      StaxUtils.skipToStartElement(this.in);
    } catch (Exception e) {
      throw new IllegalArgumentException("Unable to open resource " + resource);
    }
  }

  public void close() {
    try {
      in.close();
    } catch (final XMLStreamException e) {
      log.error(e.getMessage(), e);
    }

  }

  public boolean hasNext() {
    if (!hasNext) {
      return false;
    } else if (loadNextObject) {
      return loadNextRecord();
    } else {
      return true;
    }
  }

  protected boolean loadNextRecord() {
    try {
      currentGeometry = parseGeometry();
      loadNextObject = false;
      if (currentGeometry == null) {
        close();
        hasNext = false;
      }
      return hasNext;
    } catch (final XMLStreamException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public Geometry next() {
    if (hasNext()) {
      loadNextObject = true;
      return currentGeometry;
    } else {
      throw new NoSuchElementException();
    }
  }

  private Geometry parseGeometry()
    throws XMLStreamException {
    if (in.getEventType() != XMLStreamConstants.START_ELEMENT) {
      StaxUtils.skipToStartElement(in);
    }
    while (in.getEventType() == XMLStreamConstants.START_ELEMENT) {
      if (StaxUtils.matchElementLocalName(in, Kml22Constants.POINT)) {
        return parsePoint();
      } else if (StaxUtils.matchElementLocalName(in, Kml22Constants.LINE_STRING)) {
        return parseLineString();
      } else if (StaxUtils.matchElementLocalName(in, Kml22Constants.POLYGON)) {
        return parsePolygon();
      } else {
        while (in.next() != XMLStreamConstants.START_ELEMENT
          && in.getEventType() != XMLStreamConstants.END_DOCUMENT) {

        }
      }
    }
    return null;
  }

  private Polygon parsePolygon()
    throws XMLStreamException {
    StaxUtils.requireLocalName(in, Kml22Constants.POLYGON);
    LinearRing exteriorRing = null;
    List<LinearRing> interiorRings = new ArrayList<LinearRing>();
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (StaxUtils.matchElementLocalName(in, Kml22Constants.OUTER_BOUNDARY_IS)) {
        exteriorRing = parseOuterBoundary();
      } else if (StaxUtils.matchElementLocalName(in,
        Kml22Constants.INNER_BOUNDARY_IS)) {
        LinearRing innerRing = parseInnerBoundary();
        if (innerRing != null) {
          interiorRings.add(innerRing);
        }
      } else {
        StaxUtils.skipSubTree(in);
      }
    }
    return geometryFactory.createPolygon(exteriorRing,
      GeometryFactory.toLinearRingArray(interiorRings));
  }

  private LinearRing parseOuterBoundary()
    throws XMLStreamException {
    StaxUtils.requireLocalName(in, Kml22Constants.OUTER_BOUNDARY_IS);
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (StaxUtils.matchElementLocalName(in, Kml22Constants.LINEAR_RING)) {
        return parseLinearRing();
      } else {
        StaxUtils.skipSubTree(in);
      }
    }
    return null;
  }

  private LinearRing parseInnerBoundary()
    throws XMLStreamException {
    StaxUtils.requireLocalName(in, Kml22Constants.INNER_BOUNDARY_IS);
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (StaxUtils.matchElementLocalName(in, Kml22Constants.LINEAR_RING)) {
        return parseLinearRing();
      } else {
        StaxUtils.skipSubTree(in);
      }
    }
    return null;
  }

  private LineString parseLineString()
    throws XMLStreamException {
    StaxUtils.requireLocalName(in, Kml22Constants.LINE_STRING);
    CoordinatesList cooordinatesList = null;
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (StaxUtils.matchElementLocalName(in, Kml22Constants.COORDINATES)) {
        cooordinatesList = parseCoordinates();
      } else {
        StaxUtils.skipSubTree(in);
      }
    }
    return geometryFactory.createLineString(cooordinatesList);
  }

  private LinearRing parseLinearRing()
    throws XMLStreamException {
    StaxUtils.requireLocalName(in, Kml22Constants.LINEAR_RING);
    CoordinatesList cooordinatesList = null;
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (StaxUtils.matchElementLocalName(in, Kml22Constants.COORDINATES)) {
        cooordinatesList = parseCoordinates();
      } else {
        StaxUtils.skipSubTree(in);
      }
    }
    return geometryFactory.createLinearRing(cooordinatesList);
  }

  private Point parsePoint()
    throws XMLStreamException {
    StaxUtils.requireLocalName(in, Kml22Constants.POINT);
    CoordinatesList cooordinatesList = null;
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (StaxUtils.matchElementLocalName(in, Kml22Constants.COORDINATES)) {
        cooordinatesList = parseCoordinates();
      } else {
        StaxUtils.skipSubTree(in);
      }
    }
    return geometryFactory.createPoint(cooordinatesList);
  }

  private CoordinatesList parseCoordinates()
    throws XMLStreamException {
    StaxUtils.requireLocalName(in, Kml22Constants.COORDINATES);
    final String coordinatesListString = StaxUtils.getElementText(in);
    String[] coordinatesListArray = coordinatesListString.trim().split("\\s+");
    CoordinatesList coordinatesList = new DoubleCoordinatesList(
      coordinatesListArray.length, 3);
    for (int i = 0; i < coordinatesListArray.length; i++) {
      String coordinatesString = coordinatesListArray[i];
      String[] coordinatesArray = coordinatesString.split(",");
      for (int ordinateIndex = 0; ordinateIndex < coordinatesArray.length
        && ordinateIndex < 3; ordinateIndex++) {
        String coordinate = coordinatesArray[ordinateIndex];
        coordinatesList.setValue(i, ordinateIndex, Double.valueOf(coordinate));
      }
    }
    return coordinatesList;
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

}
