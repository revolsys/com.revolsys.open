package com.revolsys.format.gml;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.springframework.core.io.Resource;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.format.xml.StaxUtils;
import com.revolsys.io.IoConstants;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.impl.LineStringDouble;
import com.revolsys.util.MathUtil;

public class GmlGeometryIterator extends AbstractIterator<Geometry> implements
GmlConstants {

  public static final LineString parse(final String value,
    final String separator, final int axisCount) {
    final String[] values = value.split(separator);
    final double[] coordinates = new double[values.length];
    for (int i = 0; i < values.length; i++) {
      final String string = values[i];
      coordinates[i] = Double.parseDouble(string);
    }
    return new LineStringDouble(axisCount, coordinates);
  }

  public static LineString parse(final String value, final String decimal,
    String coordSeperator, String toupleSeperator) {

    toupleSeperator = toupleSeperator.replaceAll("\\\\", "\\\\\\\\");
    toupleSeperator = toupleSeperator.replaceAll("\\.", "\\\\.");
    final Pattern touplePattern = Pattern.compile("\\s*" + toupleSeperator
      + "\\s*");
    final String[] touples = touplePattern.split(value);

    coordSeperator = coordSeperator.replaceAll("\\\\", "\\\\\\\\");
    coordSeperator = coordSeperator.replaceAll("\\.", "\\\\.");
    final Pattern coordinatePattern = Pattern.compile("\\s*" + coordSeperator
      + "\\s*");

    int axisCount = 0;
    final List<double[]> listOfCoordinateArrays = new ArrayList<double[]>();
    if (touples.length == 0) {
      return null;
    } else {
      for (final String touple : touples) {
        final String[] values = coordinatePattern.split(touple);
        if (values.length > 0) {
          final double[] coordinates = MathUtil.toDoubleArray(values);
          axisCount = Math.max(axisCount, coordinates.length);
          listOfCoordinateArrays.add(coordinates);
        }
      }
    }

    return toCoordinateList(axisCount, listOfCoordinateArrays);
  }

  public static LineString toCoordinateList(final int axisCount,
    final List<double[]> listOfCoordinateArrays) {
    final int vertexCount = listOfCoordinateArrays.size();
    final double[] coordinates = new double[vertexCount * axisCount];
    for (int i = 0; i < vertexCount; i++) {
      final double[] coordinates2 = listOfCoordinateArrays.get(i);
      for (int j = 0; j < axisCount; j++) {
        final double value;
        if (j < coordinates2.length) {
          value = coordinates2[j];
        } else {
          value = Double.NaN;
        }
        coordinates[i * axisCount + j] = value;
      }
    }
    return new LineStringDouble(axisCount, coordinates);
  }

  private GeometryFactory geometryFactory;

  private XMLStreamReader in;

  public GmlGeometryIterator(final Resource resource) {
    try {
      this.in = StaxUtils.createXmlReader(resource);
    } catch (final Exception e) {
      throw new IllegalArgumentException("Unable to open resource " + resource);
    }
  }

  @Override
  protected void doClose() {
    StaxUtils.closeSilent(this.in);
    this.geometryFactory = null;
    this.in = null;
  }

  @Override
  protected void doInit() {
    this.geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
    if (this.geometryFactory == null) {
      this.geometryFactory = GeometryFactory.floating3();
    }
  }

  private GeometryFactory getGeometryFactory(
    final GeometryFactory geometryFactory) {
    final String srsName = this.in.getAttributeValue(
      SRS_NAME.getNamespaceURI(), SRS_NAME.getLocalPart());
    if (srsName == null) {
      return geometryFactory;
    } else {
      if (srsName.startsWith("urn:ogc:def:crs:EPSG:6.6:")) {
        final int srid = Integer.parseInt(srsName.substring("urn:ogc:def:crs:EPSG:6.6:".length()));
        final GeometryFactory factory = GeometryFactory.floating3(srid);
        return factory;
      } else if (srsName.startsWith("EPSG:")) {
        final int srid = Integer.parseInt(srsName.substring("EPSG:".length()));
        final GeometryFactory factory = GeometryFactory.floating3(srid);
        return factory;
      } else {
        return geometryFactory;
      }
    }
  }

  @Override
  protected Geometry getNext() {
    try {
      while (StaxUtils.skipToStartElements(this.in,
        ENVELOPE_AND_GEOMETRY_TYPE_NAMES)) {
        final QName name = this.in.getName();
        if (name.equals(ENVELOPE)) {
          this.geometryFactory = getGeometryFactory(this.geometryFactory);
          StaxUtils.skipToEndElement(this.in, ENVELOPE);
        } else {
          return readGeometry(this.geometryFactory);
        }
      }
      throw new NoSuchElementException();
    } catch (final XMLStreamException e) {
      throw new RuntimeException("Error reading next geometry", e);
    }

  }

  private LineString readCoordinates() throws XMLStreamException {
    String decimal = this.in.getAttributeValue(null, "decimal");
    if (decimal == null) {
      decimal = ".";
    }
    String coordSeperator = this.in.getAttributeValue(null, "coordSeperator");
    if (coordSeperator == null) {
      coordSeperator = ",";
    }
    String toupleSeperator = this.in.getAttributeValue(null, "toupleSeperator");
    if (toupleSeperator == null) {
      toupleSeperator = " ";
    }
    final String value = this.in.getElementText();

    final LineString points = GmlGeometryIterator.parse(value, decimal,
      coordSeperator, toupleSeperator);
    StaxUtils.skipToEndElement(this.in);
    return points;
  }

  private Geometry readGeometry(final GeometryFactory geometryFactory)
      throws XMLStreamException {
    final QName typeName = this.in.getName();
    if (typeName.equals(POINT)) {
      return readPoint(geometryFactory);
    } else if (typeName.equals(LINE_STRING)) {
      return readLineString(geometryFactory);
    } else if (typeName.equals(POLYGON)) {
      return readPolygon(geometryFactory);
    } else if (typeName.equals(MULTI_POINT)) {
      return readMultiPoint(geometryFactory);
    } else if (typeName.equals(MULTI_LINE_STRING)) {
      return readMultiLineString(geometryFactory);
    } else if (typeName.equals(MULTI_POLYGON)) {
      return readMultiPolygon(geometryFactory);
    } else if (typeName.equals(MULTI_GEOMETRY)) {
      return readMultiGeometry(geometryFactory);
    } else {
      throw new IllegalStateException("Unexpected geometry type " + typeName);
    }
  }

  private LinearRing readLinearRing(final GeometryFactory geometryFactory)
      throws XMLStreamException {
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    LineString points = null;
    if (StaxUtils.skipToChildStartElements(this.in, POS_LIST, COORDINATES)) {
      final QName elementName = this.in.getName();
      if (elementName.equals(POS_LIST)) {
        points = readPosList();
      } else if (elementName.equals(COORDINATES)) {
        points = readCoordinates();
      }
      StaxUtils.skipToEndElement(this.in, LINEAR_RING);
    } else {
      StaxUtils.skipToEndElement(this.in, LINEAR_RING);
    }
    if (points == null) {
      return factory.linearRing();
    } else {
      final int axisCount = points.getAxisCount();
      return factory.convertAxisCount(axisCount).linearRing(points);
    }
  }

  private LineString readLineString(final GeometryFactory geometryFactory)
      throws XMLStreamException {
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    LineString points = null;
    if (StaxUtils.skipToChildStartElements(this.in, POS_LIST, COORDINATES)) {
      if (this.in.getName().equals(POS)) {
        points = readPosList();
      } else if (this.in.getName().equals(COORDINATES)) {
        points = readCoordinates();
      }
    } else {
      StaxUtils.skipToEndElement(this.in, LINE_STRING);
    }
    if (points == null) {
      return factory.lineString();
    } else {
      final int axisCount = points.getAxisCount();
      return factory.convertAxisCount(axisCount).lineString(points);
    }
  }

  private Geometry readMultiGeometry(final GeometryFactory geometryFactory)
      throws XMLStreamException {
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    final List<Geometry> geometries = new ArrayList<Geometry>();
    StaxUtils.skipSubTree(this.in);
    return factory.geometry(geometries);
  }

  private MultiLineString readMultiLineString(
    final GeometryFactory geometryFactory) throws XMLStreamException {
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    int axisCount = 2;
    final List<LineString> lines = new ArrayList<LineString>();
    while (StaxUtils.skipToChildStartElements(this.in, LINE_STRING)) {
      final LineString line = readLineString(factory);
      if (line != null) {
        axisCount = Math.max(axisCount, line.getAxisCount());
        lines.add(line);
      }
    }
    StaxUtils.skipToEndElement(this.in, MULTI_LINE_STRING);
    return factory.convertAxisCount(axisCount).multiLineString(lines);
  }

  private MultiPoint readMultiPoint(final GeometryFactory geometryFactory)
      throws XMLStreamException {
    int axisCount = 2;
    final List<Point> points = new ArrayList<Point>();
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    while (StaxUtils.skipToChildStartElements(this.in, POINT)) {
      final Point point = readPoint(factory);
      if (point != null) {
        axisCount = Math.max(axisCount, point.getAxisCount());
        points.add(point);
      }
    }
    StaxUtils.skipToEndElement(this.in, MULTI_POINT);
    return factory.convertAxisCount(axisCount).multiPoint(points);
  }

  private MultiPolygon readMultiPolygon(final GeometryFactory geometryFactory)
      throws XMLStreamException {
    int axisCount = 2;
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    final List<Polygon> polygons = new ArrayList<Polygon>();
    while (StaxUtils.skipToChildStartElements(this.in, POLYGON)) {
      final Polygon polygon = readPolygon(factory);
      if (polygon != null) {
        axisCount = Math.max(axisCount, polygon.getAxisCount());
        polygons.add(polygon);
      }
    }
    StaxUtils.skipToEndElement(this.in, MULTI_POLYGON);
    return factory.convertAxisCount(axisCount).multiPolygon(polygons);
  }

  private Point readPoint(final GeometryFactory geometryFactory)
      throws XMLStreamException {
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    LineString points = null;
    if (StaxUtils.skipToChildStartElements(this.in, POS, COORDINATES)) {
      if (this.in.getName().equals(POS)) {
        points = readPosList();
      } else if (this.in.getName().equals(COORDINATES)) {
        points = readCoordinates();
      }
    } else {
      StaxUtils.skipToEndElement(this.in, POINT);
    }
    if (points == null) {
      return factory.point();
    } else {
      final int axisCount = points.getAxisCount();
      return factory.convertAxisCount(axisCount).point(points);
    }
  }

  private Polygon readPolygon(final GeometryFactory geometryFactory)
      throws XMLStreamException {
    int axisCount = 0;
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    final List<LinearRing> rings = new ArrayList<LinearRing>();
    if (StaxUtils.skipToChildStartElements(this.in, OUTER_BOUNDARY_IS)) {
      final LinearRing exteriorRing = readLinearRing(factory);
      axisCount = Math.max(axisCount, exteriorRing.getAxisCount());
      rings.add(exteriorRing);
      StaxUtils.skipToEndElement(this.in, OUTER_BOUNDARY_IS);
      while (StaxUtils.skipToChildStartElements(this.in, INNER_BOUNDARY_IS)) {
        final LinearRing interiorRing = readLinearRing(factory);
        axisCount = Math.max(axisCount, interiorRing.getAxisCount());
        rings.add(interiorRing);
        StaxUtils.skipToEndElement(this.in, INNER_BOUNDARY_IS);
      }
    }
    final Polygon polygon = factory.convertAxisCount(axisCount).polygon(rings);
    return polygon;
  }

  private LineString readPosList() throws XMLStreamException {
    final String dimension = this.in.getAttributeValue(null, "dimension");
    if (dimension == null) {
      StaxUtils.skipSubTree(this.in);
      return null;
    } else {
      final int axisCount = Integer.parseInt(dimension);
      final String value = this.in.getElementText();
      final LineString points = GmlGeometryIterator.parse(value, "\\s+",
        axisCount);
      StaxUtils.skipToEndElement(this.in);
      return points;
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
