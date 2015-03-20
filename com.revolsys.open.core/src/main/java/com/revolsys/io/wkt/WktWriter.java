package com.revolsys.io.wkt;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.util.MathUtil;
import com.revolsys.util.WrappedException;

public class WktWriter {

  public static void append(final StringBuilder wkt, final int axisCount, final Point point) {
    for (int i = 0; i < axisCount; i++) {
      if (i > 0) {
        wkt.append(" ");
      }
      MathUtil.append(wkt, point.getCoordinate(i));
    }
  }

  public static void appendLineString(final StringBuilder wkt, final Point... points) {
    wkt.append("LINESTRING");
    int axisCount = 2;
    for (final Point point : points) {
      axisCount = Math.max(axisCount, point.getAxisCount());
    }
    boolean first = true;
    for (final Point point : points) {
      if (first) {
        first = false;
      } else {
        wkt.append(",");
      }
      append(wkt, axisCount, point);
    }
    wkt.append(")");
  }

  public static void appendPoint(final StringBuilder wkt, final Point point) {
    wkt.append("POINT");
    final int axisCount = point.getAxisCount();
    append(wkt, axisCount, point);
    wkt.append(")");
  }

  /**
   * Generates the WKT for a <tt>LINESTRING</tt>
   * specified by two {@link Coordinates}s.
   *
   * @param point1 the first coordinate
   * @param point2 the second coordinate
   *
   * @return the WKT
   */
  public static String lineString(final Point... points) {
    final StringBuilder wkt = new StringBuilder();
    appendLineString(wkt, points);
    return wkt.toString();
  }

  /**
   * Generates the WKT for a <tt>POINT</tt>
   * specified by a {@link Coordinates}.
   *
   * @param p0 the point coordinate
   *
   * @return the WKT
   */
  public static String point(final Point point) {
    final StringBuilder wkt = new StringBuilder();
    appendPoint(wkt, point);
    return wkt.toString();
  }

  public static String toString(final Geometry geometry) {
    final StringWriter out = new StringWriter();
    write(out, geometry);
    out.flush();
    return out.toString();
  }

  public static void write(final Writer out, final Geometry geometry) {
    if (geometry != null) {
      if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        write(out, point);
      } else if (geometry instanceof MultiPoint) {
        final MultiPoint multiPoint = (MultiPoint)geometry;
        write(out, multiPoint);
      } else if (geometry instanceof LinearRing) {
        final LinearRing line = (LinearRing)geometry;
        write(out, line);
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        write(out, line);
      } else if (geometry instanceof MultiLineString) {
        final MultiLineString multiLine = (MultiLineString)geometry;
        write(out, multiLine);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        write(out, polygon);
      } else if (geometry instanceof MultiPolygon) {
        final MultiPolygon multiPolygon = (MultiPolygon)geometry;
        write(out, multiPolygon);
      } else if (geometry instanceof GeometryCollection) {
        final GeometryCollection geometryCollection = (GeometryCollection)geometry;
        write(out, geometryCollection);
      } else {
        throw new IllegalArgumentException("Unknown geometry type" + geometry.getClass());
      }
    }
  }

  public static void write(final Writer out, final Geometry geometry, final int axisCount) {
    try {
      if (geometry != null) {
        if (geometry instanceof Point) {
          final Point point = (Point)geometry;
          write(out, point, axisCount);
        } else if (geometry instanceof MultiPoint) {
          final MultiPoint multiPoint = (MultiPoint)geometry;
          write(out, multiPoint, axisCount);
        } else if (geometry instanceof LinearRing) {
          final LinearRing line = (LinearRing)geometry;
          write(out, line, axisCount);
        } else if (geometry instanceof LineString) {
          final LineString line = (LineString)geometry;
          write(out, line, axisCount);
        } else if (geometry instanceof MultiLineString) {
          final MultiLineString multiLine = (MultiLineString)geometry;
          write(out, multiLine, axisCount);
        } else if (geometry instanceof Polygon) {
          final Polygon polygon = (Polygon)geometry;
          write(out, polygon, axisCount);
        } else if (geometry instanceof MultiPolygon) {
          final MultiPolygon multiPolygon = (MultiPolygon)geometry;
          write(out, multiPolygon, axisCount);
        } else if (geometry instanceof GeometryCollection) {
          final GeometryCollection geometryCollection = (GeometryCollection)geometry;
          write(out, geometryCollection, axisCount);
        } else {
          throw new IllegalArgumentException("Unknown geometry type" + geometry.getClass());
        }
      }
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }

  public static void write(final Writer out, final GeometryCollection multiGeometry) {
    final int axisCount = Math.min(multiGeometry.getAxisCount(), 4);
    try {
      write(out, multiGeometry, axisCount);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }

  }

  private static void write(final Writer out, final GeometryCollection multiGeometry,
    final int axisCount) throws IOException {
    writeGeometryType(out, "GEOMETRYCOLLECTION", axisCount);
    if (multiGeometry.isEmpty()) {
      out.write(" EMPTY");
    } else {
      out.write("(");
      Geometry geometry = multiGeometry.getGeometry(0);
      write(out, geometry, axisCount);
      for (int i = 1; i < multiGeometry.getGeometryCount(); i++) {
        out.write(',');
        geometry = multiGeometry.getGeometry(i);
        write(out, geometry, axisCount);
      }
      out.write(')');
    }
  }

  public static void write(final Writer out, final LinearRing line) {
    final int axisCount = Math.min(line.getAxisCount(), 4);
    try {
      write(out, line, axisCount);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }

  }

  private static void write(final Writer out, final LinearRing line, final int axisCount)
    throws IOException {
    writeGeometryType(out, "LINEARRING", axisCount);
    if (line.isEmpty()) {
      out.write(" EMPTY");
    } else {
      final LineString coordinates = line;
      writeCoordinates(out, coordinates, axisCount);
    }
  }

  public static void write(final Writer out, final LineString line) {
    final int axisCount = Math.min(line.getAxisCount(), 4);
    try {
      write(out, line, axisCount);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }

  }

  private static void write(final Writer out, final LineString line, final int axisCount)
    throws IOException {
    writeGeometryType(out, "LINESTRING", axisCount);
    if (line.isEmpty()) {
      out.write(" EMPTY");
    } else {
      final LineString coordinates = line;
      writeCoordinates(out, coordinates, axisCount);
    }
  }

  private static void write(final Writer out, final LineString coordinates, final int index,
    final int axisCount) throws IOException {
    writeOrdinate(out, coordinates, index, 0);
    for (int j = 1; j < axisCount; j++) {
      out.write(' ');
      writeOrdinate(out, coordinates, index, j);
    }
  }

  public static void write(final Writer out, final MultiLineString multiLineString) {
    final int axisCount = Math.min(multiLineString.getAxisCount(), 4);
    try {
      write(out, multiLineString, axisCount);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }

  }

  private static void write(final Writer out, final MultiLineString multiLineString,
    final int axisCount) throws IOException {
    writeGeometryType(out, "MULTILINESTRING", axisCount);
    if (multiLineString.isEmpty()) {
      out.write(" EMPTY");
    } else {
      out.write("(");
      LineString line = (LineString)multiLineString.getGeometry(0);
      LineString points = line;
      writeCoordinates(out, points, axisCount);
      for (int i = 1; i < multiLineString.getGeometryCount(); i++) {
        out.write(",");
        line = (LineString)multiLineString.getGeometry(i);
        points = line;
        writeCoordinates(out, points, axisCount);
      }
      out.write(")");
    }
  }

  public static void write(final Writer out, final MultiPoint multiPoint) {
    final int axisCount = Math.min(multiPoint.getAxisCount(), 4);
    try {
      write(out, multiPoint, axisCount);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }

  }

  private static void write(final Writer out, final MultiPoint multiPoint, final int axisCount)
    throws IOException {
    writeGeometryType(out, "MULTIPOINT", axisCount);
    if (multiPoint.isEmpty()) {
      out.write(" EMPTY");
    } else {
      Point point = multiPoint.getPoint(0);
      out.write("((");
      writeCoordinates(out, point, axisCount);
      for (int i = 1; i < multiPoint.getGeometryCount(); i++) {
        out.write("),(");
        point = multiPoint.getPoint(i);
        writeCoordinates(out, point, axisCount);
      }
      out.write("))");
    }
  }

  public static void write(final Writer out, final MultiPolygon multiPolygon) {
    final int axisCount = Math.min(multiPolygon.getAxisCount(), 4);
    try {
      write(out, multiPolygon, axisCount);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }

  }

  private static void write(final Writer out, final MultiPolygon multiPolygon, final int axisCount)
    throws IOException {
    writeGeometryType(out, "MULTIPOLYGON", axisCount);
    if (multiPolygon.isEmpty()) {
      out.write(" EMPTY");
    } else {
      out.write("(");

      Polygon polygon = (Polygon)multiPolygon.getGeometry(0);
      writePolygon(out, polygon, axisCount);
      for (int i = 1; i < multiPolygon.getGeometryCount(); i++) {
        out.write(",");
        polygon = (Polygon)multiPolygon.getGeometry(i);
        writePolygon(out, polygon, axisCount);
      }
      out.write(")");
    }
  }

  public static void write(final Writer out, final Point point) {
    final int axisCount = Math.min(point.getAxisCount(), 4);
    try {
      write(out, point, axisCount);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }

  }

  private static void write(final Writer out, final Point point, final int axisCount)
    throws IOException {
    writeGeometryType(out, "POINT", axisCount);
    if (point.isEmpty()) {
      out.write(" EMPTY");
    } else {
      out.write("(");
      writeCoordinates(out, point, axisCount);
      out.write(')');
    }
  }

  public static void write(final Writer out, final Polygon polygon) {
    final int axisCount = Math.min(polygon.getAxisCount(), 4);
    try {
      write(out, polygon, axisCount);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }

  }

  private static void write(final Writer out, final Polygon polygon, final int axisCount)
    throws IOException {
    writeGeometryType(out, "POLYGON", axisCount);
    if (polygon.isEmpty()) {
      out.write(" EMPTY");
    } else {
      writePolygon(out, polygon, axisCount);
    }
  }

  private static void writeCoordinates(final Writer out, final LineString coordinates,
    final int axisCount) throws IOException {
    out.write('(');
    write(out, coordinates, 0, axisCount);
    for (int i = 1; i < coordinates.getVertexCount(); i++) {
      out.write(',');
      write(out, coordinates, i, axisCount);
    }
    out.write(')');
  }

  private static void writeCoordinates(final Writer out, final Point point, final int axisCount)
    throws IOException {
    writeOrdinate(out, point, 0);
    for (int j = 1; j < axisCount; j++) {
      out.write(' ');
      writeOrdinate(out, point, j);
    }
  }

  private static void writeGeometryType(final Writer out, final String geometryType,
    final int axisCount) throws IOException {
    out.write(geometryType);
  }

  private static void writeOrdinate(final Writer out, final LineString coordinates,
    final int index, final int ordinateIndex) throws IOException {
    if (ordinateIndex > coordinates.getAxisCount()) {
      out.write('0');
    } else {
      final double ordinate = coordinates.getCoordinate(index, ordinateIndex);
      if (Double.isNaN(ordinate)) {
        out.write('0');
      } else {
        out.write(MathUtil.toString(ordinate));
      }
    }
  }

  private static void writeOrdinate(final Writer out, final Point coordinates,
    final int ordinateIndex) throws IOException {
    if (ordinateIndex > coordinates.getAxisCount()) {
      out.write('0');
    } else {
      final double ordinate = coordinates.getCoordinate(ordinateIndex);
      out.write(MathUtil.toString(ordinate));
    }
  }

  private static void writePolygon(final Writer out, final Polygon polygon, final int axisCount)
    throws IOException {
    out.write('(');
    final LineString shell = polygon.getExteriorRing();
    final LineString coordinates = shell;
    writeCoordinates(out, coordinates, axisCount);
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      out.write(',');
      final LineString hole = polygon.getInteriorRing(i);
      final LineString holeCoordinates = hole;
      writeCoordinates(out, holeCoordinates, axisCount);
    }
    out.write(')');
  }
}
