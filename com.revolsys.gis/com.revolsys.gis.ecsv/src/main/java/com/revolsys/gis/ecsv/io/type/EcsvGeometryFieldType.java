package com.revolsys.gis.ecsv.io.type;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class EcsvGeometryFieldType implements EcsvFieldType {
  private final NumberFormat FORMAT = new DecimalFormat(
    "#.#########################");

  private GeometryFactory geometryFactory;

  public EcsvGeometryFieldType() {
  }

  public EcsvGeometryFieldType(
    final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  private int getDimension(
    final Geometry geometry) {
    int dimension = 0;
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      final Geometry subGeometry = geometry.getGeometryN(i);
      final int geometryDimension = CoordinatesListUtil.get(subGeometry)
        .getNumAxis();
      dimension = Math.max(dimension, geometryDimension);
    }
    return dimension;
  }

  private int getNumAxis(
    final StringBuffer text) {
    if (text.charAt(0) == '[') {
      final int endAxisIndex = text.indexOf("]");
      if (endAxisIndex == -1) {
        throw new IllegalArgumentException("Expecting ]");
      } else {
        final Pattern pattern = Pattern.compile("^\\[([^,\\],)*Z(,[^,\\])*)\\]");
        final boolean zFound = pattern.matcher(text).find();
        text.delete(0, endAxisIndex);
        if (zFound) {
          return 3;
        }
      }
    }
    return 2;
  }

  private Map<String, String> getParameters(
    final StringBuffer text) {
    if (text.charAt(0) == '{') {
      final int endIndex = text.indexOf("}");
      if (endIndex == -1) {
        throw new IllegalArgumentException("Expecting }");
      } else {
        text.delete(0, endIndex);
      }
    }
    return Collections.emptyMap();
  }

  private CoordinatesList parseCoordinates(
    final StringBuffer text,
    final int numAxis) {
    if (text.charAt(0) == '(') {
      final int endIndex = text.indexOf(")");
      if (endIndex == -1) {
        throw new IllegalArgumentException("Expecting )");
      } else {
        final String coordinatesText = text.substring(1, endIndex);
        text.delete(0, endIndex);
        return CoordinatesListUtil.parse(coordinatesText, ",", numAxis);
      }
    } else {
      throw new IllegalArgumentException("Expecting start of coordinates '('");
    }
  }

  private Object parseLineString(
    final StringBuffer text) {
    final int numAxis = getNumAxis(text);
    final Map<String, String> parameters = getParameters(text);
    final CoordinatesList points = parseCoordinates(text, numAxis);
    return geometryFactory.createLineString(points);
  }

  private Object parsePoint(
    final StringBuffer text) {
    final int numAxis = getNumAxis(text);
    final Map<String, String> parameters = getParameters(text);
    final CoordinatesList points = parseCoordinates(text, numAxis);

    if (points.size() > 1) {
      throw new IllegalArgumentException("Points may only have 1 vertex");
    } else {
      return geometryFactory.createPoint(points);

    }
  }

  public Object parseValue(
    final String value) {
    final StringBuffer text = new StringBuffer(value);
    switch (text.charAt(0)) {
      case 'P':
        text.delete(0, 1);
        return parsePoint(text);
      case 'L':
        text.delete(0, 1);
        return parseLineString(text);
      case 'A':
        text.delete(0, 1);
        // TODO return parsePolygon(text);
      case 'M':
        text.delete(0, 1);
        // TODO return parseMultiGeometry(text);
      default:
        throw new IllegalArgumentException("Unknown geometry type");
    }
  }

  public void write(
    final PrintWriter out,
    final Geometry geometry) {
    if (geometry != null) {
      final int dimension = Math.min(getDimension(geometry), 3);
      writeGeometry(out, geometry, dimension);
    }
  }

  private void writeCoordinate(
    final PrintWriter out,
    final CoordinatesList coordinates,
    final int index,
    final int ordinateIndex) {
    if (ordinateIndex > coordinates.getDimension()) {
      out.print(0);
    } else {
      final double coordinate = coordinates.getValue(index, ordinateIndex);
      if (Double.isNaN(coordinate)) {
        out.print(0);
      } else {
        out.print(FORMAT.format(coordinate));
      }
    }
  }

  public void writeCoordinates(
    final PrintWriter out,
    final CoordinatesList points,
    final int dimension) {
    out.print('(');
    writeCoordinates(out, points, 0, dimension);
    for (int i = 1; i < points.size(); i++) {
      out.print(',');
      writeCoordinates(out, points, i, dimension);
    }
    out.print(')');
  }

  private void writeCoordinates(
    final PrintWriter out,
    final CoordinatesList points,
    final int index,
    final int dimension) {
    writeCoordinate(out, points, index, 0);
    for (int j = 1; j < dimension; j++) {
      out.print(',');
      writeCoordinate(out, points, index, j);
    }
  }

  public void writeCoordinates(
    final PrintWriter out,
    final Geometry geometry,
    final int dimension) {
    final CoordinatesList points = CoordinatesListUtil.get(geometry);
    writeCoordinates(out, points, dimension);
  }

  private void writeCoordinates(
    final PrintWriter out,
    final Polygon polygon,
    final int dimension) {
    out.print('(');
    final LineString shell = polygon.getExteriorRing();
    writeCoordinates(out, shell, dimension);
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      final LineString hole = polygon.getInteriorRingN(i);
      writeCoordinates(out, hole, dimension);
    }
    out.print(')');
  }

  private void writeGeometry(
    final PrintWriter out,
    final Geometry geometry,
    final int dimension) {
    if (geometry != null) {
      if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        writePoint(out, point, dimension);
      } else if (geometry instanceof MultiPoint) {
        final MultiPoint multiPoint = (MultiPoint)geometry;
        writeMultiPoint(out, multiPoint, dimension);
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        writeLineString(out, line, dimension);
      } else if (geometry instanceof MultiLineString) {
        final MultiLineString multiLine = (MultiLineString)geometry;
        writeMultiLineString(out, multiLine, dimension);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        writePolygon(out, polygon, dimension);
      } else if (geometry instanceof MultiPolygon) {
        final MultiPolygon multiPolygon = (MultiPolygon)geometry;
        writeMultiPolygon(out, multiPolygon, dimension);
      } else if (geometry instanceof GeometryCollection) {
        final GeometryCollection geometryCollection = (GeometryCollection)geometry;
        writeGeometryCollection(out, geometryCollection, dimension);
      } else {
        throw new IllegalArgumentException("Unknown geometry type"
          + geometry.getClass());
      }
    }
  }

  public void writeGeometryCollection(
    final PrintWriter out,
    final GeometryCollection multiGeometry,
    final int dimension) {
    out.print("MG");
    if (dimension > 2) {
      out.print("[Z]");
    }
    out.print("(");

    Geometry geometry = multiGeometry.getGeometryN(0);
    writeGeometry(out, geometry, dimension);
    for (int i = 1; i < multiGeometry.getNumGeometries(); i++) {
      out.print(',');
      geometry = multiGeometry.getGeometryN(i);
      writeGeometry(out, geometry, dimension);
    }
    out.print(')');
  }

  private void writeLineString(
    final PrintWriter out,
    final LineString line,
    final int dimension) {
    if (dimension > 2) {
      out.print("L[Z]");
    } else {
      out.print("L");
    }
    writeCoordinates(out, line, dimension);
  }

  public void writeMultiLineString(
    final PrintWriter out,
    final MultiLineString multiLineString,
    final int dimension) {
    out.print("ML");
    if (dimension > 2) {
      out.print("[Z]");
    }
    out.print("(");

    LineString geometry = (LineString)multiLineString.getGeometryN(0);
    writeCoordinates(out, geometry, dimension);
    for (int i = 1; i < multiLineString.getNumGeometries(); i++) {
      out.print(',');
      geometry = (LineString)multiLineString.getGeometryN(i);
      writeCoordinates(out, geometry, dimension);
    }
    out.print(')');
  }

  public void writeMultiPoint(
    final PrintWriter out,
    final MultiPoint multiPoint,
    final int dimension) {
    out.print("MP");
    if (dimension > 2) {
      out.print("[Z]");
    }
    out.print("(");

    Point point = (Point)multiPoint.getGeometryN(0);
    writeCoordinates(out, point, dimension);
    for (int i = 1; i < multiPoint.getNumGeometries(); i++) {
      out.print(',');
      point = (Point)multiPoint.getGeometryN(i);
      writeCoordinates(out, point, dimension);
    }
    out.print(')');
  }

  public void writeMultiPolygon(
    final PrintWriter out,
    final MultiPolygon multiPolygon,
    final int dimension) {
    out.print("MA");
    if (dimension > 2) {
      out.print("[Z]");
    }
    out.print("(");

    Polygon polygon = (Polygon)multiPolygon.getGeometryN(0);
    writeCoordinates(out, polygon, dimension);
    for (int i = 1; i < multiPolygon.getNumGeometries(); i++) {
      out.print(',');
      polygon = (Polygon)multiPolygon.getGeometryN(i);
      writeCoordinates(out, polygon, dimension);
    }
    out.print(')');
  }

  private void writePoint(
    final PrintWriter out,
    final Point point,
    final int dimension) {
    out.print('P');
    if (dimension > 2) {
      out.print("[Z]");
    }
    writeCoordinates(out, point, dimension);
  }

  private void writePolygon(
    final PrintWriter out,
    final Polygon polygon,
    final int dimension) {
    out.print('A');
    if (dimension > 2) {
      out.print("[Z]");
    }
    writeCoordinates(out, polygon, dimension);
  }

  public void writeValue(
    final PrintWriter out,
    final Object value) {
    // TODO Auto-generated method stub

  }
}
