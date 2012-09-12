package com.revolsys.io.wkt;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class WktParser {

  private final GeometryFactory geometryFactory;

  public WktParser() {
    this(GeometryFactory.getFactory());
  }

  public WktParser(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  private int getNumAxis(final StringBuffer text) {
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

  private boolean hasText(final StringBuffer text, final String expected) {
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

  private CoordinatesList parseCoordinates(
    final GeometryFactory geometryFactory, final StringBuffer text,
    final int numAxis) {
    final int geometryFactoryNumAxis = geometryFactory.getNumAxis();
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
          if (axisNum < numAxis) {
            if (axisNum < geometryFactoryNumAxis) {
              coordinates.add(number);
            }
            axisNum++;
            while (axisNum < geometryFactoryNumAxis) {
              coordinates.add(Double.NaN);
              axisNum++;
            }
            axisNum = 0;
          } else {
            throw new IllegalArgumentException(
              "Too many coordinates, vertex must have " + numAxis
                + " coordinates not " + (axisNum + 1));
          }
          if (c == ')') {
            finished = true;
          } else {
            text.delete(0, 1);
          }
        } else {
          if (axisNum < numAxis) {
            if (axisNum < geometryFactoryNumAxis) {
              coordinates.add(number);
            }
            axisNum++;
          } else {
            throw new IllegalArgumentException(
              "Too many coordinates, vertex must have " + numAxis
                + " coordinates not " + (axisNum + 1));

          }
        }
      }
      text.delete(0, 1);
      return new DoubleCoordinatesList(geometryFactoryNumAxis, coordinates);
    } else {
      throw new IllegalArgumentException(
        "Expecting start of coordinates '(' not: " + text);
    }
  }

  private Double parseDouble(final StringBuffer text) {
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

  public <T extends Geometry> T parseGeometry(final String value) {
    return parseGeometry(value, true);
  }

  @SuppressWarnings("unchecked")
  public <T extends Geometry> T parseGeometry(final String value,
    final boolean useNumAxisFromGeometryFactory) {
    if (StringUtils.hasLength(value)) {
      GeometryFactory geometryFactory = this.geometryFactory;
      final int numAxis = geometryFactory.getNumAxis();
      final double scaleXY = geometryFactory.getScaleXY();
      final double scaleZ = geometryFactory.getScaleZ();
      Geometry geometry;
      final StringBuffer text = new StringBuffer(value);
      if (hasText(text, "SRID=")) {
        final Integer srid = parseInteger(text);
        if (srid != null && srid != this.geometryFactory.getSRID()) {
          geometryFactory = GeometryFactory.getFactory(srid, numAxis);
        }
        hasText(text, ";");
      }
      if (hasText(text, "POINT")) {
        geometry = parsePoint(geometryFactory, useNumAxisFromGeometryFactory,
          text);
      } else if (hasText(text, "LINESTRING")) {
        geometry = parseLineString(geometryFactory,
          useNumAxisFromGeometryFactory, text);
      } else if (hasText(text, "POLYGON")) {
        geometry = parsePolygon(geometryFactory, useNumAxisFromGeometryFactory,
          text);
      } else if (hasText(text, "MULTIPOINT")) {
        geometry = parseMultiPoint(geometryFactory,
          useNumAxisFromGeometryFactory, text);
      } else if (hasText(text, "MULTILINESTRING")) {
        geometry = parseMultiLineString(geometryFactory,
          useNumAxisFromGeometryFactory, text);
      } else if (hasText(text, "MULTIPOLYGON")) {
        geometry = parseMultiPolygon(geometryFactory,
          useNumAxisFromGeometryFactory, text);
      } else {
        throw new IllegalArgumentException("Unknown geometry type " + text);
      }
      if (this.geometryFactory.getSRID() == 0) {
        final int srid = geometry.getSRID();
        if (useNumAxisFromGeometryFactory) {
          geometryFactory = GeometryFactory.getFactory(srid, numAxis, scaleXY,
            scaleZ);
          return (T)geometryFactory.createGeometry(geometry);
        } else {
          return (T)geometry;
        }
      } else if (geometryFactory == this.geometryFactory) {
        return (T)geometry;
      } else {
        return (T)this.geometryFactory.createGeometry(geometry);
      }
    } else {
      return null;
    }
  }

  private Integer parseInteger(final StringBuffer text) {
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

  private LineString parseLineString(GeometryFactory geometryFactory,
    final boolean useNumAxisFromGeometryFactory, final StringBuffer text) {
    int numAxis = getNumAxis(text);
    if (!useNumAxisFromGeometryFactory) {
      if (numAxis != geometryFactory.getNumAxis()) {
        final int srid = geometryFactory.getSRID();
        final double scaleXY = geometryFactory.getScaleXY();
        final double scaleZ = geometryFactory.getScaleZ();
        geometryFactory = GeometryFactory.getFactory(srid, numAxis, scaleXY,
          scaleZ);
      }
    } else {
      numAxis = geometryFactory.getNumAxis();
    }
    if (isEmpty(text)) {
      return geometryFactory.createLineString();
    } else {
      final CoordinatesList points = parseCoordinates(geometryFactory, text,
        numAxis);
      return geometryFactory.createLineString(points);
    }
  }

  private MultiLineString parseMultiLineString(GeometryFactory geometryFactory,
    final boolean useNumAxisFromGeometryFactory, final StringBuffer text) {
    final int numAxis = getNumAxis(text);
    if (!useNumAxisFromGeometryFactory) {
      if (numAxis != geometryFactory.getNumAxis()) {
        final int srid = geometryFactory.getSRID();
        final double scaleXY = geometryFactory.getScaleXY();
        final double scaleZ = geometryFactory.getScaleZ();
        geometryFactory = GeometryFactory.getFactory(srid, numAxis, scaleXY,
          scaleZ);
      }
    }
    final List<CoordinatesList> lines;
    if (isEmpty(text)) {
      lines = new ArrayList<CoordinatesList>();
    } else {
      lines = parseParts(geometryFactory, text, numAxis);
    }
    return geometryFactory.createMultiLineString(lines);
  }

  private MultiPoint parseMultiPoint(GeometryFactory geometryFactory,
    final boolean useNumAxisFromGeometryFactory, final StringBuffer text) {
    final int numAxis = getNumAxis(text);
    if (!useNumAxisFromGeometryFactory) {
      if (numAxis != geometryFactory.getNumAxis()) {
        final int srid = geometryFactory.getSRID();
        final double scaleXY = geometryFactory.getScaleXY();
        final double scaleZ = geometryFactory.getScaleZ();
        geometryFactory = GeometryFactory.getFactory(srid, numAxis, scaleXY,
          scaleZ);
      }
    }

    final List<CoordinatesList> pointsList;
    if (isEmpty(text)) {
      pointsList = new ArrayList<CoordinatesList>();
    } else {
      pointsList = parseParts(geometryFactory, text, numAxis);
    }
    return geometryFactory.createMultiPoint(pointsList);
  }

  private MultiPolygon parseMultiPolygon(GeometryFactory geometryFactory,
    final boolean useNumAxisFromGeometryFactory, final StringBuffer text) {
    final int numAxis = getNumAxis(text);
    if (!useNumAxisFromGeometryFactory) {
      if (numAxis != geometryFactory.getNumAxis()) {
        final int srid = geometryFactory.getSRID();
        final double scaleXY = geometryFactory.getScaleXY();
        final double scaleZ = geometryFactory.getScaleZ();
        geometryFactory = GeometryFactory.getFactory(srid, numAxis, scaleXY,
          scaleZ);
      }
    }

    final List<List<CoordinatesList>> polygons;
    if (isEmpty(text)) {
      polygons = new ArrayList<List<CoordinatesList>>();
    } else {
      polygons = parsePartsList(geometryFactory, text, numAxis);
    }
    return geometryFactory.createMultiPolygon(polygons);
  }

  private List<CoordinatesList> parseParts(
    final GeometryFactory geometryFactory, final StringBuffer text,
    final int numAxis) {
    final List<CoordinatesList> parts = new ArrayList<CoordinatesList>();
    final char firstChar = text.charAt(0);
    switch (firstChar) {
      case '(':
        do {
          text.delete(0, 1);
          final CoordinatesList coordinates = parseCoordinates(geometryFactory,
            text, numAxis);
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

  private List<List<CoordinatesList>> parsePartsList(
    final GeometryFactory geometryFactory, final StringBuffer text,
    final int numAxis) {
    final List<List<CoordinatesList>> partsList = new ArrayList<List<CoordinatesList>>();
    final char firstChar = text.charAt(0);
    switch (firstChar) {
      case '(':
        do {
          text.delete(0, 1);
          final List<CoordinatesList> parts = parseParts(geometryFactory, text,
            numAxis);
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
    final boolean useNumAxisFromGeometryFactory, final StringBuffer text) {
    final int numAxis = getNumAxis(text);
    if (!useNumAxisFromGeometryFactory) {
      if (numAxis != geometryFactory.getNumAxis()) {
        final int srid = geometryFactory.getSRID();
        final double scaleXY = geometryFactory.getScaleXY();
        final double scaleZ = geometryFactory.getScaleZ();
        geometryFactory = GeometryFactory.getFactory(srid, numAxis, scaleXY,
          scaleZ);
      }
    }
    if (isEmpty(text)) {
      return geometryFactory.createPoint();
    } else {
      final CoordinatesList points = parseCoordinates(geometryFactory, text,
        numAxis);
      if (points.size() > 1) {
        throw new IllegalArgumentException("Points may only have 1 vertex");
      }
      return geometryFactory.createPoint(points);
    }
  }

  private Polygon parsePolygon(GeometryFactory geometryFactory,
    final boolean useNumAxisFromGeometryFactory, final StringBuffer text) {
    int numAxis = getNumAxis(text);
    if (!useNumAxisFromGeometryFactory) {
      if (numAxis != geometryFactory.getNumAxis()) {
        final int srid = geometryFactory.getSRID();
        final double scaleXY = geometryFactory.getScaleXY();
        final double scaleZ = geometryFactory.getScaleZ();
        geometryFactory = GeometryFactory.getFactory(srid, numAxis, scaleXY,
          scaleZ);
      }
    } else {
      numAxis = geometryFactory.getNumAxis();
    }

    final List<CoordinatesList> parts;
    if (isEmpty(text)) {
      parts = new ArrayList<CoordinatesList>();
    } else {
      parts = parseParts(geometryFactory, text, numAxis);
    }
    return geometryFactory.createPolygon(parts);
  }

  private void skipWhitespace(final StringBuffer text) {
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

}
