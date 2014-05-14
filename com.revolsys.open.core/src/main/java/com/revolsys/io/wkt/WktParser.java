package com.revolsys.io.wkt;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;

public class WktParser {

  public static boolean hasText(final StringBuffer text, final String expected) {
    skipWhitespace(text);
    final int length = expected.length();
    final CharSequence subText = text.subSequence(0, length);
    if (subText.equals(expected)) {
      text.delete(0, length);
      return true;
    } else {
      return false;
    }
  }

  public static Double parseDouble(final StringBuffer text) {
    skipWhitespace(text);
    int i = 0;
    for (; i < text.length(); i++) {
      final char c = text.charAt(i);
      if (Character.isWhitespace(c) || c == ',' || c == ')') {
        break;
      }
    }
    final String numberText = text.substring(0, i);
    text.delete(0, i);
    if (numberText.length() == 0) {
      return null;
    } else {
      return new Double(numberText);
    }

  }

  public static Integer parseInteger(final StringBuffer text) {
    skipWhitespace(text);
    int i = 0;
    while (i < text.length() && Character.isDigit(text.charAt(i))) {
      i++;
    }
    if (!Character.isDigit(text.charAt(i))) {
      i--;
    }
    if (i < 0) {
      return null;
    } else {
      final String numberText = text.substring(0, i + 1);
      text.delete(0, i + 1);
      return Integer.valueOf(numberText);
    }
  }

  public static void skipWhitespace(final StringBuffer text) {
    for (int i = 0; i < text.length(); i++) {
      final char c = text.charAt(i);
      if (!Character.isWhitespace(c)) {
        if (i > 0) {
          text.delete(0, i);
        }
        return;
      }
    }
  }

  private final GeometryFactory geometryFactory;

  public WktParser() {
    this(GeometryFactory.getFactory());
  }

  public WktParser(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  private int getAxisCount(final StringBuffer text) {
    skipWhitespace(text);
    final char c = text.charAt(0);
    switch (c) {
      case '(':
      case 'E':
        return 2;
      case 'M':
        text.delete(0, 1);
        return 4;
      case 'Z':
        if (text.charAt(1) == 'M') {
          text.delete(0, 2);
          return 4;
        } else {
          text.delete(0, 1);
          return 3;
        }
      default:
        throw new IllegalArgumentException(
          "Expecting Z, M, ZM, (, or EMPTY not: " + text);
    }
  }

  private boolean isEmpty(final StringBuffer text) {
    if (hasText(text, "EMPTY")) {
      skipWhitespace(text);
      if (text.length() > 0) {
        throw new IllegalArgumentException(
          "Unexpected text at the end of an empty geometry: " + text);
      }
      return true;
    } else {
      return false;
    }
  }

  private PointList parseCoordinates(
    final GeometryFactory geometryFactory, final StringBuffer text,
    final int axisCount) {
    final int geometryFactoryAxisCount = geometryFactory.getAxisCount();
    char c = text.charAt(0);
    if (c == '(') {
      text.delete(0, 1);
      final List<Double> coordinates = new ArrayList<Double>();
      int axisNum = 0;
      boolean finished = false;
      while (!finished) {
        final Double number = parseDouble(text);
        c = text.charAt(0);
        if (number == null) {
          if (c == ')') {
            finished = true;
          } else {
            throw new IllegalArgumentException(
              "Expecting end of coordinates ')' not" + text);
          }
        } else if (c == ',' || c == ')') {
          if (axisNum < axisCount) {
            if (axisNum < geometryFactoryAxisCount) {
              coordinates.add(number);
            }
            axisNum++;
            while (axisNum < geometryFactoryAxisCount) {
              coordinates.add(Double.NaN);
              axisNum++;
            }
            axisNum = 0;
          } else {
            throw new IllegalArgumentException(
              "Too many coordinates, vertex must have " + axisCount
                + " coordinates not " + (axisNum + 1));
          }
          if (c == ')') {
            finished = true;
          } else {
            text.delete(0, 1);
          }
        } else {
          if (axisNum < axisCount) {
            if (axisNum < geometryFactoryAxisCount) {
              coordinates.add(number);
            }
            axisNum++;
          } else {
            throw new IllegalArgumentException(
              "Too many coordinates, vertex must have " + axisCount
                + " coordinates not " + (axisNum + 1));

          }
        }
      }
      text.delete(0, 1);
      return new DoubleCoordinatesList(geometryFactoryAxisCount, coordinates);
    } else {
      throw new IllegalArgumentException(
        "Expecting start of coordinates '(' not: " + text);
    }
  }

  public <T extends Geometry> T parseGeometry(final String value) {
    return parseGeometry(value, true);
  }

  @SuppressWarnings("unchecked")
  public <T extends Geometry> T parseGeometry(final String value,
    final boolean useAxisCountFromGeometryFactory) {
    if (StringUtils.hasLength(value)) {
      GeometryFactory geometryFactory = this.geometryFactory;
      final int axisCount = geometryFactory.getAxisCount();
      final double scaleXY = geometryFactory.getScaleXY();
      final double scaleZ = geometryFactory.getScaleZ();
      Geometry geometry;
      final StringBuffer text = new StringBuffer(value);
      if (hasText(text, "SRID=")) {
        final Integer srid = parseInteger(text);
        if (srid != null && srid != this.geometryFactory.getSrid()) {
          geometryFactory = GeometryFactory.getFactory(srid, axisCount);
        }
        hasText(text, ";");
      }
      if (hasText(text, "POINT")) {
        geometry = parsePoint(geometryFactory, useAxisCountFromGeometryFactory,
          text);
      } else if (hasText(text, "LINESTRING")) {
        geometry = parseLineString(geometryFactory,
          useAxisCountFromGeometryFactory, text);
      } else if (hasText(text, "POLYGON")) {
        geometry = parsePolygon(geometryFactory,
          useAxisCountFromGeometryFactory, text);
      } else if (hasText(text, "MULTIPOINT")) {
        geometry = parseMultiPoint(geometryFactory,
          useAxisCountFromGeometryFactory, text);
      } else if (hasText(text, "MULTILINESTRING")) {
        geometry = parseMultiLineString(geometryFactory,
          useAxisCountFromGeometryFactory, text);
      } else if (hasText(text, "MULTIPOLYGON")) {
        geometry = parseMultiPolygon(geometryFactory,
          useAxisCountFromGeometryFactory, text);
      } else {
        throw new IllegalArgumentException("Unknown geometry type " + text);
      }
      if (this.geometryFactory.getSrid() == 0) {
        final int srid = geometry.getSrid();
        if (useAxisCountFromGeometryFactory) {
          geometryFactory = GeometryFactory.getFactory(srid, axisCount,
            scaleXY, scaleZ);
          return (T)geometryFactory.geometry(geometry);
        } else {
          return (T)geometry;
        }
      } else if (geometryFactory == this.geometryFactory) {
        return (T)geometry;
      } else {
        return (T)this.geometryFactory.geometry(geometry);
      }
    } else {
      return null;
    }
  }

  private LineString parseLineString(GeometryFactory geometryFactory,
    final boolean useAxisCountFromGeometryFactory, final StringBuffer text) {
    final int axisCount = getAxisCount(text);
    if (!useAxisCountFromGeometryFactory) {
      if (axisCount != geometryFactory.getAxisCount()) {
        final int srid = geometryFactory.getSrid();
        final double scaleXY = geometryFactory.getScaleXY();
        final double scaleZ = geometryFactory.getScaleZ();
        geometryFactory = GeometryFactory.getFactory(srid, axisCount, scaleXY,
          scaleZ);
      }
    }
    if (isEmpty(text)) {
      return geometryFactory.lineString();
    } else {
      final PointList points = parseCoordinates(geometryFactory, text,
        axisCount);
      return geometryFactory.lineString(points);
    }
  }

  private MultiLineString parseMultiLineString(GeometryFactory geometryFactory,
    final boolean useAxisCountFromGeometryFactory, final StringBuffer text) {
    final int axisCount = getAxisCount(text);
    if (!useAxisCountFromGeometryFactory) {
      if (axisCount != geometryFactory.getAxisCount()) {
        final int srid = geometryFactory.getSrid();
        final double scaleXY = geometryFactory.getScaleXY();
        final double scaleZ = geometryFactory.getScaleZ();
        geometryFactory = GeometryFactory.getFactory(srid, axisCount, scaleXY,
          scaleZ);
      }
    }
    final List<PointList> lines;
    if (isEmpty(text)) {
      lines = new ArrayList<PointList>();
    } else {
      lines = parseParts(geometryFactory, text, axisCount);
    }
    return geometryFactory.multiLineString(lines);
  }

  private MultiPoint parseMultiPoint(GeometryFactory geometryFactory,
    final boolean useAxisCountFromGeometryFactory, final StringBuffer text) {
    final int axisCount = getAxisCount(text);
    if (!useAxisCountFromGeometryFactory) {
      if (axisCount != geometryFactory.getAxisCount()) {
        final int srid = geometryFactory.getSrid();
        final double scaleXY = geometryFactory.getScaleXY();
        final double scaleZ = geometryFactory.getScaleZ();
        geometryFactory = GeometryFactory.getFactory(srid, axisCount, scaleXY,
          scaleZ);
      }
    }

    if (isEmpty(text)) {
      return geometryFactory.multiPoint();
    } else {
      final List<PointList> pointsList = parseParts(geometryFactory,
        text, axisCount);
      return geometryFactory.multiPoint(pointsList);
    }
  }

  private MultiPolygon parseMultiPolygon(GeometryFactory geometryFactory,
    final boolean useAxisCountFromGeometryFactory, final StringBuffer text) {
    final int axisCount = getAxisCount(text);
    if (!useAxisCountFromGeometryFactory) {
      if (axisCount != geometryFactory.getAxisCount()) {
        final int srid = geometryFactory.getSrid();
        final double scaleXY = geometryFactory.getScaleXY();
        final double scaleZ = geometryFactory.getScaleZ();
        geometryFactory = GeometryFactory.getFactory(srid, axisCount, scaleXY,
          scaleZ);
      }
    }

    final List<List<PointList>> polygons;
    if (isEmpty(text)) {
      polygons = new ArrayList<List<PointList>>();
    } else {
      polygons = parsePartsList(geometryFactory, text, axisCount);
    }
    return geometryFactory.multiPolygon(polygons);
  }

  private List<PointList> parseParts(
    final GeometryFactory geometryFactory, final StringBuffer text,
    final int axisCount) {
    final List<PointList> parts = new ArrayList<PointList>();
    final char firstChar = text.charAt(0);
    switch (firstChar) {
      case '(':
        do {
          text.delete(0, 1);
          final PointList coordinates = parseCoordinates(geometryFactory,
            text, axisCount);
          parts.add(coordinates);
        } while (text.charAt(0) == ',');
        if (text.charAt(0) == ')') {
          text.delete(0, 1);
        } else {
          throw new IllegalArgumentException("Expecting ) not" + text);
        }
      break;
      case ')':
        text.delete(0, 2);
      break;

      default:
        throw new IllegalArgumentException("Expecting ( not" + text);
    }
    return parts;
  }

  private List<List<PointList>> parsePartsList(
    final GeometryFactory geometryFactory, final StringBuffer text,
    final int axisCount) {
    final List<List<PointList>> partsList = new ArrayList<List<PointList>>();
    final char firstChar = text.charAt(0);
    switch (firstChar) {
      case '(':
        do {
          text.delete(0, 1);
          final List<PointList> parts = parseParts(geometryFactory, text,
            axisCount);
          partsList.add(parts);
        } while (text.charAt(0) == ',');
        if (text.charAt(0) == ')') {
          text.delete(0, 1);
        } else {
          throw new IllegalArgumentException("Expecting ) not" + text);
        }
      break;
      case ')':
        text.delete(0, 2);
      break;

      default:
        throw new IllegalArgumentException("Expecting ( not" + text);
    }
    return partsList;
  }

  private Point parsePoint(GeometryFactory geometryFactory,
    final boolean useAxisCountFromGeometryFactory, final StringBuffer text) {
    final int axisCount = getAxisCount(text);
    if (!useAxisCountFromGeometryFactory) {
      if (axisCount != geometryFactory.getAxisCount()) {
        final int srid = geometryFactory.getSrid();
        final double scaleXY = geometryFactory.getScaleXY();
        final double scaleZ = geometryFactory.getScaleZ();
        geometryFactory = GeometryFactory.getFactory(srid, axisCount, scaleXY,
          scaleZ);
      }
    }
    if (isEmpty(text)) {
      return geometryFactory.point();
    } else {
      final PointList points = parseCoordinates(geometryFactory, text,
        axisCount);
      if (points.size() > 1) {
        throw new IllegalArgumentException("Points may only have 1 vertex");
      }
      return geometryFactory.point(points);
    }
  }

  private Polygon parsePolygon(GeometryFactory geometryFactory,
    final boolean useAxisCountFromGeometryFactory, final StringBuffer text) {
    final int axisCount = getAxisCount(text);
    if (!useAxisCountFromGeometryFactory) {
      if (axisCount != geometryFactory.getAxisCount()) {
        final int srid = geometryFactory.getSrid();
        final double scaleXY = geometryFactory.getScaleXY();
        final double scaleZ = geometryFactory.getScaleZ();
        geometryFactory = GeometryFactory.getFactory(srid, axisCount, scaleXY,
          scaleZ);
      }
    }

    final List<PointList> parts;
    if (isEmpty(text)) {
      parts = new ArrayList<PointList>();
    } else {
      parts = parseParts(geometryFactory, text, axisCount);
    }
    return geometryFactory.polygon(parts);
  }

}
