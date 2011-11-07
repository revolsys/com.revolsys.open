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
package com.revolsys.gis.wkt;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class WktWriter {
  private static final NumberFormat FORMAT = new DecimalFormat(
    "#.#########################");

  public static String toString(Geometry geometry) {
    StringWriter out = new StringWriter();
    PrintWriter writer = new PrintWriter(out);
    write(writer, geometry);
    writer.flush();
    return out.toString();
  }

  public static void write(PrintWriter out, Geometry geometry) {
    if (geometry != null) {
      if (geometry instanceof Point) {
        Point point = (Point)geometry;
        write(out, point);
      } else if (geometry instanceof MultiPoint) {
        MultiPoint multiPoint = (MultiPoint)geometry;
        write(out, multiPoint);
      } else if (geometry instanceof LineString) {
        LineString line = (LineString)geometry;
        write(out, line);
      } else if (geometry instanceof MultiLineString) {
        MultiLineString multiLine = (MultiLineString)geometry;
        write(out, multiLine);
      } else if (geometry instanceof Polygon) {
        Polygon polygon = (Polygon)geometry;
        write(out, polygon);
      } else if (geometry instanceof MultiPolygon) {
        MultiPolygon multiPolygon = (MultiPolygon)geometry;
        write(out, multiPolygon);
      } else if (geometry instanceof GeometryCollection) {
        GeometryCollection geometryCollection = (GeometryCollection)geometry;
        write(out, geometryCollection);
      } else {
        throw new IllegalArgumentException("Unknown geometry type"
          + geometry.getClass());
      }
    }
  }

  public static void write(PrintWriter out, Geometry geometry, int numAxis) {
    if (geometry != null) {
      if (geometry instanceof Point) {
        Point point = (Point)geometry;
        write(out, point, numAxis);
      } else if (geometry instanceof MultiPoint) {
        MultiPoint multiPoint = (MultiPoint)geometry;
        write(out, multiPoint, numAxis);
      } else if (geometry instanceof LineString) {
        LineString line = (LineString)geometry;
        write(out, line, numAxis);
      } else if (geometry instanceof MultiLineString) {
        MultiLineString multiLine = (MultiLineString)geometry;
        write(out, multiLine, numAxis);
      } else if (geometry instanceof Polygon) {
        Polygon polygon = (Polygon)geometry;
        write(out, polygon, numAxis);
      } else if (geometry instanceof MultiPolygon) {
        MultiPolygon multiPolygon = (MultiPolygon)geometry;
        write(out, multiPolygon, numAxis);
      } else if (geometry instanceof GeometryCollection) {
        GeometryCollection geometryCollection = (GeometryCollection)geometry;
        write(out, geometryCollection, numAxis);
      } else {
        throw new IllegalArgumentException("Unknown geometry type"
          + geometry.getClass());
      }
    }
  }

  public static void write(PrintWriter out, Point point) {
    int numAxis = Math.min(getDimension(point), 4);
    write(out, point, numAxis);
  }

  private static void write(PrintWriter out, Point point, int numAxis) {
    writeGeometryType(out, "POINT", numAxis);
    if (point.isEmpty()) {
      out.print(" EMPTY");
    } else {
      out.print("(");
      final CoordinateSequence coordinates = point.getCoordinateSequence();
      write(out, coordinates, 0, numAxis);
      out.print(')');
    }
  }

  private static void writeGeometryType(PrintWriter out,
    final String geometryType, int numAxis) {
    out.print(geometryType);
    writeAxis(out, numAxis);
  }

  private static void writeAxis(PrintWriter out, int numAxis) {
    if (numAxis > 3) {
      out.print(" ZM");
    } else if (numAxis > 2) {
      out.print(" Z");
    }
  }

  public static void write(PrintWriter out, LineString line) {
    int numAxis = Math.min(getDimension(line), 4);
    write(out, line, numAxis);
  }

  private static void write(PrintWriter out, LineString line, int numAxis) {
    writeGeometryType(out, "LINESTRING", numAxis);
    if (line.isEmpty()) {
      out.print(" EMPTY");
    } else {
      final CoordinateSequence coordinates = line.getCoordinateSequence();
      write(out, coordinates, numAxis);
    }
  }

  public static void write(PrintWriter out, Polygon polygon) {
    int numAxis = Math.min(getDimension(polygon), 4);
    write(out, polygon, numAxis);
  }

  private static void write(PrintWriter out, Polygon polygon, int numAxis) {
    writeGeometryType(out, "POLYGON", numAxis);
    if (polygon.isEmpty()) {
      out.print(" EMPTY");
    } else {
      writePolygon(out, polygon, numAxis);
    }
  }

  private static void writePolygon(PrintWriter out, Polygon polygon, int numAxis) {
    out.print('(');
    final LineString shell = polygon.getExteriorRing();
    final CoordinateSequence coordinates = shell.getCoordinateSequence();
    write(out, coordinates, numAxis);
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      out.print(',');
      final LineString hole = polygon.getInteriorRingN(i);
      final CoordinateSequence holeCoordinates = hole.getCoordinateSequence();
      write(out, holeCoordinates, numAxis);
    }
    out.print(')');
  }

  public static void write(PrintWriter out, MultiPoint multiPoint) {
    int numAxis = Math.min(getDimension(multiPoint), 4);
    write(out, multiPoint, numAxis);
  }

  private static void write(PrintWriter out, MultiPoint multiPoint, int numAxis) {
    writeGeometryType(out, "MULTIPOINT", numAxis);
    if (multiPoint.isEmpty()) {
      out.print(" EMPTY");
    } else {
      Point point = (Point)multiPoint.getGeometryN(0);
      CoordinateSequence coordinates = point.getCoordinateSequence();
      out.print("((");
      write(out, coordinates, 0, numAxis);
      for (int i = 1; i < multiPoint.getNumGeometries(); i++) {
        out.print("),(");
        point = (Point)multiPoint.getGeometryN(i);
        coordinates = point.getCoordinateSequence();
        write(out, coordinates, 0, numAxis);
      }
      out.print("))");
    }
  }

  private static int getDimension(Geometry geometry) {
    int numAxis = GeometryFactory.getFactory(geometry).getNumAxis();
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      final Geometry subGeometry = geometry.getGeometryN(i);
      final int geometryDimension = CoordinatesListUtil.get(subGeometry)
        .getNumAxis();
      numAxis = Math.max(numAxis, geometryDimension);
    }
    return numAxis;
  }

  public static void write(PrintWriter out, MultiLineString multiLineString) {
    int numAxis = Math.min(getDimension(multiLineString), 4);
    write(out, multiLineString, numAxis);
  }

  private static void write(PrintWriter out, MultiLineString multiLineString,
    int numAxis) {
    writeGeometryType(out, "MULTILINESTRING", numAxis);
    if (multiLineString.isEmpty()) {
      out.print(" EMPTY");
    } else {
      out.print("(");
      LineString line = (LineString)multiLineString.getGeometryN(0);
      CoordinatesList points = CoordinatesListUtil.get(line);
      write(out, points, numAxis);
      for (int i = 1; i < multiLineString.getNumGeometries(); i++) {
        out.print(",");
        line = (LineString)multiLineString.getGeometryN(i);
        points = CoordinatesListUtil.get(line);
        write(out, points, numAxis);
      }
      out.print(")");
    }
  }

  public static void write(PrintWriter out, MultiPolygon multiPolygon) {
    int numAxis = Math.min(getDimension(multiPolygon), 4);
    write(out, multiPolygon, numAxis);
  }

  private static void write(PrintWriter out, MultiPolygon multiPolygon,
    int numAxis) {
    writeGeometryType(out, "MULTIPOLYGON", numAxis);
    if (multiPolygon.isEmpty()) {
      out.print(" EMPTY");
    } else {
      out.print("(");

      Polygon polygon = (Polygon)multiPolygon.getGeometryN(0);
      writePolygon(out, polygon, numAxis);
      for (int i = 1; i < multiPolygon.getNumGeometries(); i++) {
        out.print(",");
        polygon = (Polygon)multiPolygon.getGeometryN(i);
        writePolygon(out, polygon, numAxis);
      }
      out.print(")");
    }
  }

  public static void write(PrintWriter out, GeometryCollection multiGeometry) {
    int numAxis = Math.min(getDimension(multiGeometry), 4);
    write(out, multiGeometry, numAxis);
  }

  private static void write(PrintWriter out, GeometryCollection multiGeometry,
    int numAxis) {
    writeGeometryType(out, "MULTIGEOMETRY", numAxis);
    if (multiGeometry.isEmpty()) {
      out.print(" EMPTY");
    } else {
      out.print("(");
      Geometry geometry = multiGeometry.getGeometryN(0);
      write(out, geometry, numAxis);
      for (int i = 1; i < multiGeometry.getNumGeometries(); i++) {
        out.print(',');
        geometry = multiGeometry.getGeometryN(i);
        write(out, geometry, numAxis);
      }
      out.print(')');
    }
  }

  public static void write(PrintWriter out, CoordinateSequence coordinates,
    int numAxis) {
    out.print('(');
    write(out, coordinates, 0, numAxis);
    for (int i = 1; i < coordinates.size(); i++) {
      out.print(',');
      write(out, coordinates, i, numAxis);
    }
    out.print(')');
  }

  private static void write(PrintWriter out, CoordinateSequence coordinates,
    int index, int numAxis) {
    writeOrdinate(out, coordinates, index, 0);
    for (int j = 1; j < numAxis; j++) {
      out.print(' ');
      writeOrdinate(out, coordinates, index, j);
    }
  }

  private static void writeOrdinate(PrintWriter out,
    CoordinateSequence coordinates, int index, int ordinateIndex) {
    if (ordinateIndex > coordinates.getDimension()) {
      out.print(0);
    } else {
      final double ordinate = coordinates.getOrdinate(index, ordinateIndex);
      if (Double.isNaN(ordinate)) {
        out.print(0);
      } else {
        out.print(FORMAT.format(ordinate));
      }
    }
  }
}
