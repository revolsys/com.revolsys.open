package com.revolsys.gis.postgresql.type;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.MultiLineString;
import com.revolsys.geometry.model.MultiPoint;
import com.revolsys.geometry.model.MultiPolygon;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.util.MathUtil;

public class PostgreSQLWktWriter {

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
    if (axisCount > 3) {
      wkt.append(" ZM");
    } else if (axisCount > 2) {
      wkt.append(" Z");
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
    if (axisCount > 3) {
      wkt.append(" ZM");
    } else if (axisCount > 2) {
      wkt.append(" Z");
    }
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
    final PrintWriter writer = new PrintWriter(out);
    final int srid = geometry.getCoordinateSystemId();
    if (srid > 0) {
      writer.print("SRID=");
      writer.print(srid);
      writer.print(';');
    }
    write(writer, geometry);
    writer.flush();
    return out.toString();
  }

  public static void write(final PrintWriter out, final Geometry geometry) {
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

  public static void write(final PrintWriter out, final Geometry geometry, final int axisCount) {
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
  }

  public static void write(final PrintWriter out, final GeometryCollection multiGeometry) {
    final int axisCount = Math.min(multiGeometry.getAxisCount(), 4);
    write(out, multiGeometry, axisCount);
  }

  private static void write(final PrintWriter out, final GeometryCollection multiGeometry,
    final int axisCount) {
    writeGeometryType(out, "GEOMETRYCOLLECTION", axisCount);
    if (multiGeometry.isEmpty()) {
      out.print(" EMPTY");
    } else {
      out.print("(");
      Geometry geometry = multiGeometry.getGeometry(0);
      write(out, geometry, axisCount);
      for (int i = 1; i < multiGeometry.getGeometryCount(); i++) {
        out.print(',');
        geometry = multiGeometry.getGeometry(i);
        write(out, geometry, axisCount);
      }
      out.print(')');
    }
  }

  public static void write(final PrintWriter out, final LinearRing line) {
    final int axisCount = Math.min(line.getAxisCount(), 4);
    write(out, line, axisCount);
  }

  private static void write(final PrintWriter out, final LinearRing line, final int axisCount) {
    writeGeometryType(out, "LINEARRING", axisCount);
    if (line.isEmpty()) {
      out.print(" EMPTY");
    } else {
      final LineString coordinates = line;
      writeCoordinates(out, coordinates, axisCount);
    }
  }

  public static void write(final PrintWriter out, final LineString line) {
    final int axisCount = Math.min(line.getAxisCount(), 4);
    write(out, line, axisCount);
  }

  private static void write(final PrintWriter out, final LineString line, final int axisCount) {
    writeGeometryType(out, "LINESTRING", axisCount);
    if (line.isEmpty()) {
      out.print(" EMPTY");
    } else {
      final LineString coordinates = line;
      writeCoordinates(out, coordinates, axisCount);
    }
  }

  private static void write(final PrintWriter out, final LineString coordinates, final int index,
    final int axisCount) {
    writeOrdinate(out, coordinates, index, 0);
    for (int j = 1; j < axisCount; j++) {
      out.print(' ');
      writeOrdinate(out, coordinates, index, j);
    }
  }

  public static void write(final PrintWriter out, final MultiLineString multiLineString) {
    final int axisCount = Math.min(multiLineString.getAxisCount(), 4);
    write(out, multiLineString, axisCount);
  }

  private static void write(final PrintWriter out, final MultiLineString multiLineString,
    final int axisCount) {
    writeGeometryType(out, "MULTILINESTRING", axisCount);
    if (multiLineString.isEmpty()) {
      out.print(" EMPTY");
    } else {
      out.print("(");
      LineString line = (LineString)multiLineString.getGeometry(0);
      LineString points = line;
      writeCoordinates(out, points, axisCount);
      for (int i = 1; i < multiLineString.getGeometryCount(); i++) {
        out.print(",");
        line = (LineString)multiLineString.getGeometry(i);
        points = line;
        writeCoordinates(out, points, axisCount);
      }
      out.print(")");
    }
  }

  public static void write(final PrintWriter out, final MultiPoint multiPoint) {
    final int axisCount = Math.min(multiPoint.getAxisCount(), 4);
    write(out, multiPoint, axisCount);
  }

  private static void write(final PrintWriter out, final MultiPoint multiPoint,
    final int axisCount) {
    writeGeometryType(out, "MULTIPOINT", axisCount);
    if (multiPoint.isEmpty()) {
      out.print(" EMPTY");
    } else {
      Point point = multiPoint.getPoint(0);
      out.print("((");
      writeCoordinates(out, point, axisCount);
      for (int i = 1; i < multiPoint.getGeometryCount(); i++) {
        out.print("),(");
        point = multiPoint.getPoint(i);
        writeCoordinates(out, point, axisCount);
      }
      out.print("))");
    }
  }

  public static void write(final PrintWriter out, final MultiPolygon multiPolygon) {
    final int axisCount = Math.min(multiPolygon.getAxisCount(), 4);
    write(out, multiPolygon, axisCount);
  }

  private static void write(final PrintWriter out, final MultiPolygon multiPolygon,
    final int axisCount) {
    writeGeometryType(out, "MULTIPOLYGON", axisCount);
    if (multiPolygon.isEmpty()) {
      out.print(" EMPTY");
    } else {
      out.print("(");

      Polygon polygon = (Polygon)multiPolygon.getGeometry(0);
      writePolygon(out, polygon, axisCount);
      for (int i = 1; i < multiPolygon.getGeometryCount(); i++) {
        out.print(",");
        polygon = (Polygon)multiPolygon.getGeometry(i);
        writePolygon(out, polygon, axisCount);
      }
      out.print(")");
    }
  }

  public static void write(final PrintWriter out, final Point point) {
    final int axisCount = Math.min(point.getAxisCount(), 4);
    write(out, point, axisCount);
  }

  private static void write(final PrintWriter out, final Point point, final int axisCount) {
    writeGeometryType(out, "POINT", axisCount);
    if (point.isEmpty()) {
      out.print(" EMPTY");
    } else {
      out.print("(");
      writeCoordinates(out, point, axisCount);
      out.print(')');
    }
  }

  public static void write(final PrintWriter out, final Polygon polygon) {
    final int axisCount = Math.min(polygon.getAxisCount(), 4);
    write(out, polygon, axisCount);
  }

  private static void write(final PrintWriter out, final Polygon polygon, final int axisCount) {
    writeGeometryType(out, "POLYGON", axisCount);
    if (polygon.isEmpty()) {
      out.print(" EMPTY");
    } else {
      writePolygon(out, polygon, axisCount);
    }
  }

  private static void writeAxis(final PrintWriter out, final int axisCount) {
    if (axisCount > 3) {
      out.print("M");
    }
  }

  public static void writeCoordinates(final PrintWriter out, final LineString coordinates,
    final int axisCount) {
    out.print('(');
    write(out, coordinates, 0, axisCount);
    for (int i = 1; i < coordinates.getVertexCount(); i++) {
      out.print(',');
      write(out, coordinates, i, axisCount);
    }
    out.print(')');
  }

  private static void writeCoordinates(final PrintWriter out, final Point point,
    final int axisCount) {
    writeOrdinate(out, point, 0);
    for (int j = 1; j < axisCount; j++) {
      out.print(' ');
      writeOrdinate(out, point, j);
    }
  }

  private static void writeGeometryType(final PrintWriter out, final String geometryType,
    final int axisCount) {
    out.print(geometryType);
    writeAxis(out, axisCount);
  }

  private static void writeOrdinate(final PrintWriter out, final LineString coordinates,
    final int index, final int ordinateIndex) {
    if (ordinateIndex > coordinates.getAxisCount()) {
      out.print(0);
    } else {
      final double ordinate = coordinates.getCoordinate(index, ordinateIndex);
      if (Double.isNaN(ordinate)) {
        out.print(0);
      } else {
        out.print(MathUtil.toString(ordinate));
      }
    }
  }

  private static void writeOrdinate(final PrintWriter out, final Point coordinates,
    final int ordinateIndex) {
    if (ordinateIndex > coordinates.getAxisCount()) {
      out.print(0);
    } else {
      final double ordinate = coordinates.getCoordinate(ordinateIndex);
      if (Double.isNaN(ordinate)) {
        out.print(0);
      } else {
        out.print(MathUtil.toString(ordinate));
      }
    }
  }

  private static void writePolygon(final PrintWriter out, final Polygon polygon,
    final int axisCount) {
    out.print('(');
    final LineString shell = polygon.getShell();
    final LineString coordinates = shell;
    writeCoordinates(out, coordinates, axisCount);
    for (int i = 0; i < polygon.getHoleCount(); i++) {
      out.print(',');
      final LineString hole = polygon.getHole(i);
      final LineString holeCoordinates = hole;
      writeCoordinates(out, holeCoordinates, axisCount);
    }
    out.print(')');
  }
}
