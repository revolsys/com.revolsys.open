/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.io.wkt;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
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

public class WktWriter {

  public static void append(final StringBuffer wkt, final int axisCount,
    final Coordinates point) {
    for (int i = 0; i < axisCount; i++) {
      if (i > 0) {
        wkt.append(" ");
      }
      MathUtil.append(wkt, point.getValue(i));
    }
  }

  public static void appendLineString(final StringBuffer wkt,
    final Coordinates... points) {
    wkt.append("LINESTRING");
    int axisCount = 2;
    for (final Coordinates point : points) {
      axisCount = Math.max(axisCount, point.getAxisCount());
    }
    if (axisCount > 3) {
      wkt.append(" ZM");
    } else if (axisCount > 2) {
      wkt.append(" Z");
    }
    boolean first = true;
    for (final Coordinates point : points) {
      if (first) {
        first = false;
      } else {
        wkt.append(",");
      }
      append(wkt, axisCount, point);
    }
    wkt.append(")");
  }

  public static void appendPoint(final StringBuffer wkt, final Coordinates point) {
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
  public static String lineString(final Coordinates... points) {
    final StringBuffer wkt = new StringBuffer();
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
  public static String point(final Coordinates point) {
    final StringBuffer wkt = new StringBuffer();
    appendPoint(wkt, point);
    return wkt.toString();
  }

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

  private static void write(final PrintWriter out, final Coordinates point,
    final int axisCount) {
    writeOrdinate(out, point, 0);
    for (int j = 1; j < axisCount; j++) {
      out.print(' ');
      writeOrdinate(out, point, j);
    }
  }

  public static void write(final PrintWriter out,
    final CoordinatesList coordinates, final int axisCount) {
    out.print('(');
    write(out, coordinates, 0, axisCount);
    for (int i = 1; i < coordinates.size(); i++) {
      out.print(',');
      write(out, coordinates, i, axisCount);
    }
    out.print(')');
  }

  private static void write(final PrintWriter out,
    final CoordinatesList coordinates, final int index, final int axisCount) {
    writeOrdinate(out, coordinates, index, 0);
    for (int j = 1; j < axisCount; j++) {
      out.print(' ');
      writeOrdinate(out, coordinates, index, j);
    }
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
        throw new IllegalArgumentException("Unknown geometry type"
          + geometry.getClass());
      }
    }
  }

  public static void write(final PrintWriter out, final Geometry geometry,
    final int axisCount) {
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
        throw new IllegalArgumentException("Unknown geometry type"
          + geometry.getClass());
      }
    }
  }

  public static void write(final PrintWriter out,
    final GeometryCollection multiGeometry) {
    final int axisCount = Math.min(multiGeometry.getAxisCount(), 4);
    write(out, multiGeometry, axisCount);
  }

  private static void write(final PrintWriter out,
    final GeometryCollection multiGeometry, final int axisCount) {
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

  private static void write(final PrintWriter out, final LinearRing line,
    final int axisCount) {
    writeGeometryType(out, "LINEARRING", axisCount);
    if (line.isEmpty()) {
      out.print(" EMPTY");
    } else {
      final CoordinatesList coordinates = line.getCoordinatesList();
      write(out, coordinates, axisCount);
    }
  }

  public static void write(final PrintWriter out, final LineString line) {
    final int axisCount = Math.min(line.getAxisCount(), 4);
    write(out, line, axisCount);
  }

  private static void write(final PrintWriter out, final LineString line,
    final int axisCount) {
    writeGeometryType(out, "LINESTRING", axisCount);
    if (line.isEmpty()) {
      out.print(" EMPTY");
    } else {
      final CoordinatesList coordinates = line.getCoordinatesList();
      write(out, coordinates, axisCount);
    }
  }

  public static void write(final PrintWriter out,
    final MultiLineString multiLineString) {
    final int axisCount = Math.min(multiLineString.getAxisCount(), 4);
    write(out, multiLineString, axisCount);
  }

  private static void write(final PrintWriter out,
    final MultiLineString multiLineString, final int axisCount) {
    writeGeometryType(out, "MULTILINESTRING", axisCount);
    if (multiLineString.isEmpty()) {
      out.print(" EMPTY");
    } else {
      out.print("(");
      LineString line = (LineString)multiLineString.getGeometry(0);
      CoordinatesList points = CoordinatesListUtil.get(line);
      write(out, points, axisCount);
      for (int i = 1; i < multiLineString.getGeometryCount(); i++) {
        out.print(",");
        line = (LineString)multiLineString.getGeometry(i);
        points = CoordinatesListUtil.get(line);
        write(out, points, axisCount);
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
      Coordinates point = multiPoint.getPoint(0);
      out.print("((");
      write(out, point, axisCount);
      for (int i = 1; i < multiPoint.getGeometryCount(); i++) {
        out.print("),(");
        point = multiPoint.getPoint(i);
        write(out, point, axisCount);
      }
      out.print("))");
    }
  }

  public static void write(final PrintWriter out,
    final MultiPolygon multiPolygon) {
    final int axisCount = Math.min(multiPolygon.getAxisCount(), 4);
    write(out, multiPolygon, axisCount);
  }

  private static void write(final PrintWriter out,
    final MultiPolygon multiPolygon, final int axisCount) {
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

  private static void write(final PrintWriter out, final Point point,
    final int axisCount) {
    writeGeometryType(out, "POINT", axisCount);
    if (point.isEmpty()) {
      out.print(" EMPTY");
    } else {
      out.print("(");
      write(out, (Coordinates)point, axisCount);
      out.print(')');
    }
  }

  public static void write(final PrintWriter out, final Polygon polygon) {
    final int axisCount = Math.min(polygon.getAxisCount(), 4);
    write(out, polygon, axisCount);
  }

  private static void write(final PrintWriter out, final Polygon polygon,
    final int axisCount) {
    writeGeometryType(out, "POLYGON", axisCount);
    if (polygon.isEmpty()) {
      out.print(" EMPTY");
    } else {
      writePolygon(out, polygon, axisCount);
    }
  }

  private static void writeAxis(final PrintWriter out, final int axisCount) {
    if (axisCount > 3) {
      out.print(" ZM");
    } else if (axisCount > 2) {
      out.print(" Z");
    }
  }

  private static void writeGeometryType(final PrintWriter out,
    final String geometryType, final int axisCount) {
    out.print(geometryType);
    writeAxis(out, axisCount);
  }

  private static void writeOrdinate(final PrintWriter out,
    final Coordinates coordinates, final int ordinateIndex) {
    if (ordinateIndex > coordinates.getAxisCount()) {
      out.print(0);
    } else {
      final double ordinate = coordinates.getValue(ordinateIndex);
      out.print(MathUtil.toString(ordinate));
    }
  }

  private static void writeOrdinate(final PrintWriter out,
    final CoordinatesList coordinates, final int index, final int ordinateIndex) {
    if (ordinateIndex > coordinates.getAxisCount()) {
      out.print(0);
    } else {
      final double ordinate = coordinates.getValue(index, ordinateIndex);
      if (Double.isNaN(ordinate)) {
        out.print(0);
      } else {
        out.print(MathUtil.toString(ordinate));
      }
    }
  }

  private static void writePolygon(final PrintWriter out,
    final Polygon polygon, final int axisCount) {
    out.print('(');
    final LineString shell = polygon.getExteriorRing();
    final CoordinatesList coordinates = shell.getCoordinatesList();
    write(out, coordinates, axisCount);
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      out.print(',');
      final LineString hole = polygon.getInteriorRing(i);
      final CoordinatesList holeCoordinates = hole.getCoordinatesList();
      write(out, holeCoordinates, axisCount);
    }
    out.print(')');
  }
}
