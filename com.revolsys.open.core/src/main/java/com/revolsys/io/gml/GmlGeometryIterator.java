package com.revolsys.io.gml;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.springframework.core.io.Resource;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.IoConstants;
import com.revolsys.io.xml.StaxUtils;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.util.MathUtil;

public class GmlGeometryIterator extends AbstractIterator<Geometry> implements
  GmlConstants {

  public static final CoordinatesList parse(final String value,
    final String separator, final int axisCount) {
    final String[] values = value.split(separator);
    final double[] coordinates = new double[values.length];
    for (int i = 0; i < values.length; i++) {
      final String string = values[i];
      coordinates[i] = Double.parseDouble(string);
    }
    return new DoubleCoordinatesList(axisCount, coordinates);
  }

  public static CoordinatesList parse(final String value, final String decimal,
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

  public static CoordinatesList toCoordinateList(final int axisCount,
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
    return new DoubleCoordinatesList(axisCount, coordinates);
  }

  private com.revolsys.jts.geom.GeometryFactory geometryFactory;

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
    StaxUtils.closeSilent(in);
    geometryFactory = null;
    in = null;
  }

  @Override
  protected void doInit() {
    geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
    if (geometryFactory == null) {
      geometryFactory = GeometryFactory.getFactory();
    }
  }

  private com.revolsys.jts.geom.GeometryFactory getGeometryFactory(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory) {
    final String srsName = in.getAttributeValue(SRS_NAME.getNamespaceURI(),
      SRS_NAME.getLocalPart());
    if (srsName == null) {
      return geometryFactory;
    } else {
      if (srsName.startsWith("urn:ogc:def:crs:EPSG:6.6:")) {
        final int srid = Integer.parseInt(srsName.substring("urn:ogc:def:crs:EPSG:6.6:".length()));
        final com.revolsys.jts.geom.GeometryFactory factory = GeometryFactory.getFactory(srid);
        return factory;
      } else if (srsName.startsWith("EPSG:")) {
        final int srid = Integer.parseInt(srsName.substring("EPSG:".length()));
        final com.revolsys.jts.geom.GeometryFactory factory = GeometryFactory.getFactory(srid);
        return factory;
      } else {
        return geometryFactory;
      }
    }
  }

  @Override
  protected Geometry getNext() {
    try {
      while (StaxUtils.skipToStartElements(in, ENVELOPE_AND_GEOMETRY_TYPE_NAMES)) {
        if (in.getName().equals(ENVELOPE)) {
          geometryFactory = getGeometryFactory(geometryFactory);
          StaxUtils.skipToEndElement(in, ENVELOPE);
        } else {
          return readGeometry(geometryFactory);
        }
      }
      throw new NoSuchElementException();
    } catch (final XMLStreamException e) {
      throw new RuntimeException("Error reading next geometry", e);
    }

  }

  private CoordinatesList readCoordinates() throws XMLStreamException {
    String decimal = in.getAttributeValue(null, "decimal");
    if (decimal == null) {
      decimal = ".";
    }
    String coordSeperator = in.getAttributeValue(null, "coordSeperator");
    if (coordSeperator == null) {
      coordSeperator = ",";
    }
    String toupleSeperator = in.getAttributeValue(null, "toupleSeperator");
    if (toupleSeperator == null) {
      toupleSeperator = " ";
    }
    final String value = in.getElementText();

    final CoordinatesList points = GmlGeometryIterator.parse(value, decimal,
      coordSeperator, toupleSeperator);
    StaxUtils.skipToEndElement(in);
    return points;
  }

  private Geometry readGeometry(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory)
    throws XMLStreamException {
    final QName typeName = in.getName();
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

  private LinearRing readLinearRing(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory)
    throws XMLStreamException {
    final com.revolsys.jts.geom.GeometryFactory factory = getGeometryFactory(geometryFactory);
    CoordinatesList points = null;
    if (StaxUtils.skipToChildStartElements(in, POS_LIST, COORDINATES)) {
      final QName elementName = in.getName();
      if (elementName.equals(POS_LIST)) {
        points = readPosList();
      } else if (elementName.equals(COORDINATES)) {
        points = readCoordinates();
      }
      StaxUtils.skipToEndElement(in, LINEAR_RING);
    } else {
      StaxUtils.skipToEndElement(in, LINEAR_RING);
    }
    return factory.linearRing(points);
  }

  private LineString readLineString(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory)
    throws XMLStreamException {
    final com.revolsys.jts.geom.GeometryFactory factory = getGeometryFactory(geometryFactory);
    CoordinatesList points = null;
    if (StaxUtils.skipToChildStartElements(in, POS_LIST)) {
      points = readPosList();
      StaxUtils.skipToEndElement(in, LINE_STRING);
    } else if (StaxUtils.skipToChildStartElements(in, COORDINATES)) {
      points = readCoordinates();
      StaxUtils.skipToEndElement(in, LINE_STRING);
    } else {
      StaxUtils.skipToEndElement(in, LINE_STRING);
    }
    return factory.lineString(points);
  }

  private Geometry readMultiGeometry(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory)
    throws XMLStreamException {
    final com.revolsys.jts.geom.GeometryFactory factory = getGeometryFactory(geometryFactory);
    final List<Geometry> geometries = new ArrayList<Geometry>();
    StaxUtils.skipSubTree(in);
    return factory.geometry(geometries);
  }

  private MultiLineString readMultiLineString(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory)
    throws XMLStreamException {
    final com.revolsys.jts.geom.GeometryFactory factory = getGeometryFactory(geometryFactory);
    final List<LineString> lines = new ArrayList<LineString>();
    while (StaxUtils.skipToChildStartElements(in, LINE_STRING)) {
      final LineString line = readLineString(factory);
      if (line != null) {
        lines.add(line);
      }
    }
    StaxUtils.skipToEndElement(in, MULTI_LINE_STRING);
    return factory.multiLineString(lines);
  }

  private MultiPoint readMultiPoint(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory)
    throws XMLStreamException {
    final List<Point> points = new ArrayList<Point>();
    final com.revolsys.jts.geom.GeometryFactory factory = getGeometryFactory(geometryFactory);
    while (StaxUtils.skipToChildStartElements(in, POINT)) {
      final Point point = readPoint(factory);
      if (point != null) {
        points.add(point);
      }
    }
    StaxUtils.skipToEndElement(in, MULTI_POINT);
    return factory.multiPoint(points);
  }

  private MultiPolygon readMultiPolygon(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory)
    throws XMLStreamException {
    final com.revolsys.jts.geom.GeometryFactory factory = getGeometryFactory(geometryFactory);
    final List<Polygon> polygons = new ArrayList<Polygon>();
    while (StaxUtils.skipToChildStartElements(in, POLYGON)) {
      final Polygon polygon = readPolygon(factory);
      if (polygon != null) {
        polygons.add(polygon);
      }
    }
    StaxUtils.skipToEndElement(in, MULTI_POLYGON);
    return factory.multiPolygon(polygons);
  }

  private Point readPoint(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory)
    throws XMLStreamException {
    final com.revolsys.jts.geom.GeometryFactory factory = getGeometryFactory(geometryFactory);
    CoordinatesList points = null;
    if (StaxUtils.skipToChildStartElements(in, POS)) {
      points = readPosList();
      StaxUtils.skipToEndElement(in, POINT);
    } else if (StaxUtils.skipToChildStartElements(in, COORDINATES)) {
      points = readCoordinates();
      StaxUtils.skipToEndElement(in, POINT);
    } else {
      StaxUtils.skipToEndElement(in, POINT);
    }
    return factory.point(points);
  }

  private Polygon readPolygon(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory)
    throws XMLStreamException {
    final com.revolsys.jts.geom.GeometryFactory factory = getGeometryFactory(geometryFactory);
    final List<LinearRing> rings = new ArrayList<LinearRing>();
    if (StaxUtils.skipToChildStartElements(in, OUTER_BOUNDARY_IS)) {
      final LinearRing exteriorRing = readLinearRing(factory);
      rings.add(exteriorRing);
      StaxUtils.skipToEndElement(in, OUTER_BOUNDARY_IS);
      while (StaxUtils.skipToChildStartElements(in, INNER_BOUNDARY_IS)) {
        final LinearRing interiorRing = readLinearRing(factory);
        rings.add(interiorRing);
        StaxUtils.skipToEndElement(in, INNER_BOUNDARY_IS);
      }
      StaxUtils.skipToEndElement(in, POLYGON);
    } else {
      StaxUtils.skipSubTree(in);
    }
    final Polygon polygon = factory.polygon(rings);
    return polygon;
  }

  private CoordinatesList readPosList() throws XMLStreamException {
    final String dimension = in.getAttributeValue(null, "dimension");
    if (dimension == null) {
      StaxUtils.skipSubTree(in);
      return null;
    } else {
      final int axisCount = Integer.parseInt(dimension);
      final String value = in.getElementText();
      final CoordinatesList points = GmlGeometryIterator.parse(value, "\\s+",
        axisCount);
      StaxUtils.skipToEndElement(in);
      return points;
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
