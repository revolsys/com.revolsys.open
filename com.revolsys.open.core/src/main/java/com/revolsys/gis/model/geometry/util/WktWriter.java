package com.revolsys.gis.model.geometry.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.GeometryCollection;
import com.revolsys.gis.model.geometry.LineString;
import com.revolsys.gis.model.geometry.LinearRing;
import com.revolsys.gis.model.geometry.MultiLineString;
import com.revolsys.gis.model.geometry.MultiLinearRing;
import com.revolsys.gis.model.geometry.MultiPoint;
import com.revolsys.gis.model.geometry.MultiPolygon;
import com.revolsys.gis.model.geometry.Point;
import com.revolsys.gis.model.geometry.Polygon;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.util.MathUtil;

public class WktWriter {
  public static String toString(final Geometry geometry) {
    final StringWriter out = new StringWriter();
    final PrintWriter writer = new PrintWriter(out);
    write(writer, geometry);
    writer.flush();
    return out.toString();
  }

  public static String toString(final Geometry geometry, final boolean writeSrid) {
    final StringWriter out = new StringWriter();
    final PrintWriter writer = new PrintWriter(out);
    if (writeSrid) {
      final int srid = geometry.getSrid();
      if (srid > 0) {
        writer.print("SRID=");
        writer.print(srid);
        writer.print(';');
      }
    }
    write(writer, geometry);
    writer.flush();
    return out.toString();
  }

  public static void write(final PrintWriter out,
    final Coordinates coordinates, final int numAxis) {
    out.print('(');
    writeCoordinate(out, coordinates, numAxis);
    out.print(')');
  }

  public static void write(final PrintWriter out,
    final CoordinatesList coordinates, final int numAxis) {
    out.print('(');
    for (int i = 0; i < coordinates.size(); i++) {
      if (i > 0) {
        out.print(',');
      }
      write(out, coordinates, i, numAxis);
    }
    out.print(')');
  }

  private static void write(final PrintWriter out,
    final CoordinatesList coordinates, final int index, final int numAxis) {
    writeCoordinate(out, coordinates, index, 0);
    for (int j = 1; j < numAxis; j++) {
      out.print(' ');
      writeCoordinate(out, coordinates, index, j);
    }
  }

  public static void write(final PrintWriter out, final Geometry geometry) {
    if (geometry != null) {
      final int numAxis = Math.min(geometry.getNumAxis(), 4);
      write(out, geometry, numAxis);
    }
  }

  public static void write(final PrintWriter out, final Geometry geometry,
    final int numAxis) {
    if (geometry != null) {
      if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        write(out, point, numAxis);
      } else if (geometry instanceof MultiPoint) {
        final MultiPoint multiPoint = (MultiPoint)geometry;
        write(out, multiPoint, numAxis);
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        write(out, line, numAxis);
      } else if (geometry instanceof MultiLineString) {
        final MultiLineString multiLine = (MultiLineString)geometry;
        write(out, multiLine, numAxis);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        write(out, polygon, numAxis);
      } else if (geometry instanceof MultiPolygon) {
        final MultiPolygon multiPolygon = (MultiPolygon)geometry;
        write(out, multiPolygon, numAxis);
      } else if (geometry instanceof GeometryCollection) {
        final GeometryCollection geometryCollection = (GeometryCollection)geometry;
        write(out, geometryCollection, numAxis);
      } else {
        throw new IllegalArgumentException("Unknown geometry type"
          + geometry.getClass());
      }
    }
  }

  public static void write(final PrintWriter out,
    final GeometryCollection multiGeometry) {
    final int numAxis = Math.min(multiGeometry.getGeometryCount(), 4);
    write(out, multiGeometry, numAxis);
  }

  private static void write(final PrintWriter out,
    final GeometryCollection multiGeometry, final int numAxis) {
    writeGeometryType(out, "MULTIGEOMETRY", numAxis);
    if (multiGeometry.isEmpty()) {
      out.print(" EMPTY");
    } else {
      out.print("(");
      for (int i = 0; i < multiGeometry.getGeometryCount(); i++) {
        if (i > 0) {
          out.print(',');
        }
        final Geometry geometry = multiGeometry.getGeometry(i);
        write(out, geometry, numAxis);
      }
      out.print(')');
    }
  }

  public static void write(final PrintWriter out, final LineString line) {
    final int numAxis = Math.min(line.getNumAxis(), 4);
    write(out, line, numAxis);
  }

  private static void write(final PrintWriter out, final LineString line,
    final int numAxis) {
    writeGeometryType(out, "LINESTRING", numAxis);
    if (line.isEmpty()) {
      out.print(" EMPTY");
    } else {
      final CoordinatesList coordinates = line;
      write(out, coordinates, numAxis);
    }
  }

  public static void write(final PrintWriter out,
    final MultiLineString multiLineString) {
    final int numAxis = Math.min(multiLineString.getNumAxis(), 4);
    write(out, multiLineString, numAxis);
  }

  private static void write(final PrintWriter out,
    final MultiLineString multiLineString, final int numAxis) {
    writeGeometryType(out, "MULTILINESTRING", numAxis);
    if (multiLineString.isEmpty()) {
      out.print(" EMPTY");
    } else {
      out.print("(");
      for (int i = 0; i < multiLineString.getGeometryCount(); i++) {
        if (i > 0) {
          out.print(",");
        }
        final CoordinatesList points = (CoordinatesList)multiLineString.getGeometry(i);
        write(out, points, numAxis);
      }
      out.print(")");
    }
  }

  public static void write(final PrintWriter out, final MultiPoint multiPoint) {
    final int numAxis = Math.min(multiPoint.getNumAxis(), 4);
    write(out, multiPoint, numAxis);
  }

  private static void write(final PrintWriter out, final MultiPoint multiPoint,
    final int numAxis) {
    writeGeometryType(out, "MULTIPOINT", numAxis);
    if (multiPoint.isEmpty()) {
      out.print(" EMPTY");
    } else {
      out.print("((");
      for (int i = 0; i < multiPoint.getGeometryCount(); i++) {
        if (i > 0) {
          out.print("),(");

          out.print(",");
        }
        final Coordinates point = (Coordinates)multiPoint.getGeometry(i);
        write(out, point, numAxis);
      }
      out.print("))");
    }
  }

  public static void write(final PrintWriter out,
    final MultiPolygon multiPolygon) {
    final int numAxis = Math.min(multiPolygon.getNumAxis(), 4);
    write(out, multiPolygon, numAxis);
  }

  private static void write(final PrintWriter out,
    final MultiPolygon multiPolygon, final int numAxis) {
    writeGeometryType(out, "MULTIPOLYGON", numAxis);
    if (multiPolygon.isEmpty()) {
      out.print(" EMPTY");
    } else {
      out.print("(");

      for (int i = 0; i < multiPolygon.getGeometryCount(); i++) {
        if (i > 0) {
          out.print(",");
        }
        final Polygon polygon = multiPolygon.getGeometry(i);
        writePolygon(out, polygon, numAxis);
      }
      out.print(")");
    }
  }

  public static void write(final PrintWriter out, final Point point) {
    final int numAxis = Math.min(point.getNumAxis(), 4);
    write(out, point, numAxis);
  }

  private static void write(final PrintWriter out, final Point point,
    final int numAxis) {
    writeGeometryType(out, "POINT", numAxis);
    if (point.isEmpty()) {
      out.print(" EMPTY");
    } else {
      out.print("(");
      final Coordinates coordinates = point;
      write(out, coordinates, numAxis);
      out.print(')');
    }
  }

  public static void write(final PrintWriter out, final Polygon polygon) {
    final int numAxis = Math.min(polygon.getNumAxis(), 4);
    write(out, polygon, numAxis);
  }

  private static void write(final PrintWriter out, final Polygon polygon,
    final int numAxis) {
    writeGeometryType(out, "POLYGON", numAxis);
    if (polygon.isEmpty()) {
      out.print(" EMPTY");
    } else {
      writePolygon(out, polygon, numAxis);
    }
  }

  private static void writeAxis(final PrintWriter out, final int numAxis) {
    if (numAxis > 3) {
      out.print(" ZM");
    } else if (numAxis > 2) {
      out.print(" Z");
    }
  }

  private static void writeCoordinate(final PrintWriter out,
    final Coordinates coordinates, final int axisIndex) {
    if (axisIndex > coordinates.getNumAxis()) {
      out.print(0);
    } else {
      final double coordinate = coordinates.getValue(axisIndex);
      if (Double.isNaN(coordinate)) {
        out.print(0);
      } else {
        out.print(MathUtil.toString(coordinate));
      }
    }
  }

  private static void writeCoordinate(final PrintWriter out,
    final CoordinatesList coordinates, final int index, final int axisIndex) {
    if (axisIndex > coordinates.getNumAxis()) {
      out.print(0);
    } else {
      final double coordinate = coordinates.getValue(index, axisIndex);
      if (Double.isNaN(coordinate)) {
        out.print(0);
      } else {
        out.print(MathUtil.toString(coordinate));
      }
    }
  }

  private static void writeGeometryType(final PrintWriter out,
    final String geometryType, final int numAxis) {
    out.print(geometryType);
    writeAxis(out, numAxis);
  }

  private static void writePolygon(final PrintWriter out,
    final Polygon polygon, final int numAxis) {
    final MultiLinearRing rings = polygon.getRings();
    out.print('(');
    for (int i = 0; i < rings.getGeometryCount(); i++) {
      if (i > 0) {
        out.print(',');
      }
      final LinearRing coordinates = rings.getGeometry(i);
      write(out, coordinates, numAxis);
    }
    out.print(')');
  }
}
