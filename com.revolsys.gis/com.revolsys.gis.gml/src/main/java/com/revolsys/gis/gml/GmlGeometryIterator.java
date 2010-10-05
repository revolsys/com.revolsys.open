package com.revolsys.gis.gml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.IoConstants;
import com.revolsys.xml.io.StaxUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GmlGeometryIterator extends AbstractObjectWithProperties implements
  Iterator<Geometry>, GmlConstants {

  private boolean hasNext = true;

  private boolean initialized;

  private XMLStreamReader in;

  private Geometry currentGeometry;

  private GeometryFactory geometryFactory;

  public GmlGeometryIterator(
    Resource resource) {
    try {
      this.in = StaxUtils.createXmlReader(resource);
    } catch (final Exception e) {
      throw new IllegalArgumentException("Unable to open resource " + resource);
    }
    init();
  }

  private void init() {
    initialized = true;
    geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
    if (geometryFactory == null) {
      geometryFactory = new GeometryFactory();
    }
    readNextGeometry();

    if (!hasNext) {
      close();
    }
  }

  public void close() {
    hasNext = false;
    StaxUtils.closeSilent(in);
  }

  @Override
  protected void finalize()
    throws Throwable {
    close();
  }

  public boolean hasNext() {
    if (!initialized) {
      init();
    }
    return hasNext;
  }

  public Geometry next() {
    if (hasNext()) {
      Geometry geometry = currentGeometry;
      readNextGeometry();
      return geometry;
    } else {
      throw new NoSuchElementException();
    }
  }

  private void readNextGeometry() {
    try {
      if (StaxUtils.skipToStartElements(in, GEOMETRY_TYPE_NAMES)) {
        currentGeometry = readGeometry();
      } else {
        close();
      }
    } catch (XMLStreamException e) {
      throw new RuntimeException("Error reading next geometry", e);
    }

  }

  private Geometry readGeometry()
    throws XMLStreamException {
    QName typeName = in.getName();
    if (typeName.equals(POINT)) {
      return readPoint();
    } else if (typeName.equals(LINE_STRING)) {
      return readLineString();
    } else if (typeName.equals(POLYGON)) {
      return readPolygon();
    } else if (typeName.equals(MULTI_POINT)) {
      return readMultiPoint();
    } else if (typeName.equals(MULTI_CURVE)) {
      return readMultiCurve();
    } else if (typeName.equals(MULTI_SURFACE)) {
      return readMultiSurface();
    } else if (typeName.equals(MULTI_GEOMETRY)) {
      return readMultiGeometry();
    } else {
      throw new IllegalStateException("Unexpected geometry type " + typeName);
    }
  }

  private Geometry readMultiGeometry()
    throws XMLStreamException {
    GeometryFactory factory = geometryFactory;
    List<Geometry> geometries = new ArrayList<Geometry>();
    StaxUtils.skipSubTree(in);
    return factory.createGeometry(geometries);
  }

  private MultiPolygon readMultiSurface()
    throws XMLStreamException {
    GeometryFactory factory = geometryFactory;
    List<Polygon> polygons = new ArrayList<Polygon>();
    while (StaxUtils.skipToChildStartElements(in, POLYGON)) {
      final Polygon polygon = readPolygon();
      if (polygon != null) {
        polygons.add(polygon);
      }
    }
    StaxUtils.skipToEndElement(in, MULTI_SURFACE);
    return factory.createMultiPolygon(polygons);
  }

  private MultiLineString readMultiCurve()
    throws XMLStreamException {
    GeometryFactory factory = geometryFactory;
    List<LineString> lines = new ArrayList<LineString>();
    while (StaxUtils.skipToChildStartElements(in, LINE_STRING)) {
      final LineString line = readLineString();
      if (line != null) {
        lines.add(line);
      }
    }
    StaxUtils.skipToEndElement(in, MULTI_CURVE);
    return factory.createMultiLineString(lines);
  }

  private MultiPoint readMultiPoint()
    throws XMLStreamException {
    List<Point> points = new ArrayList<Point>();
    GeometryFactory factory = geometryFactory;
    while (StaxUtils.skipToChildStartElements(in, POINT)) {
      final Point point = readPoint();
      if (point != null) {
        points.add(point);
      }
    }
    StaxUtils.skipToEndElement(in, MULTI_POINT);
    return factory.createMultiPoint(points);
  }

  private Polygon readPolygon()
    throws XMLStreamException {
    GeometryFactory factory = geometryFactory;
    List<LinearRing> rings = new ArrayList<LinearRing>();
    if (StaxUtils.skipToChildStartElements(in, OUTER_BOUNDARY_IS)) {
      LinearRing exteriorRing = readLinearRing();
      rings.add(exteriorRing);
      StaxUtils.skipToEndElement(in, OUTER_BOUNDARY_IS);
      while (StaxUtils.skipToChildStartElements(in, INNER_BOUNDARY_IS)) {
        LinearRing interiorRing = readLinearRing();
        rings.add(interiorRing);
        StaxUtils.skipToEndElement(in, INNER_BOUNDARY_IS);
      }
      StaxUtils.skipToEndElement(in, POLYGON);
    } else {
      StaxUtils.skipSubTree(in);
    }
    final Polygon polygon = factory.createPolygon(rings);
    return polygon;
  }

  private LineString readLineString()
    throws XMLStreamException {
    GeometryFactory factory = geometryFactory;
    CoordinatesList points = null;
    if (StaxUtils.skipToChildStartElements(in, POS_LIST)) {
      points = readPosList();
      StaxUtils.skipToEndElement(in, LINE_STRING);
    }
    return factory.createLineString(points);
  }

  private LinearRing readLinearRing()
    throws XMLStreamException {
    GeometryFactory factory = geometryFactory;
    CoordinatesList points = null;
    if (StaxUtils.skipToChildStartElements(in, POS_LIST)) {
      points = readPosList();
      StaxUtils.skipToEndElement(in, LINEAR_RING);
    }
    return factory.createLinearRing(points);
  }

  private Point readPoint()
    throws XMLStreamException {
    GeometryFactory factory = geometryFactory;
    CoordinatesList points = null;
    if (StaxUtils.skipToChildStartElements(in, POS)) {
      points = readPosList();
      StaxUtils.skipToEndElement(in, POINT);
    }
    return factory.createPoint(points);
  }

  private CoordinatesList readPosList()
    throws XMLStreamException {
    String dimension = in.getAttributeValue(null, "dimension");
    if (dimension == null) {
      StaxUtils.skipSubTree(in);
      return null;
    } else {
      int numAxis = Integer.parseInt(dimension);
      String value = in.getElementText();
      CoordinatesList points = CoordinatesListUtil.parse(value, "\\s+", numAxis);
      StaxUtils.skipToEndElement(in);
      return points;
    }
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

}
