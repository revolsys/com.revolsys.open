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
import java.text.DecimalFormat;
import java.text.NumberFormat;

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
  private static final NumberFormat FORMAT = new DecimalFormat("#.#########################");
  public static void write(
    PrintWriter out,
    Geometry geometry) {
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

  public static void write(
    PrintWriter out,
    Geometry geometry,
    int dimension) {
    if (geometry != null) {
      if (geometry instanceof Point) {
        Point point = (Point)geometry;
        write(out, point, dimension);
      } else if (geometry instanceof MultiPoint) {
        MultiPoint multiPoint = (MultiPoint)geometry;
        write(out, multiPoint, dimension);
      } else if (geometry instanceof LineString) {
        LineString line = (LineString)geometry;
        write(out, line, dimension);
      } else if (geometry instanceof MultiLineString) {
        MultiLineString multiLine = (MultiLineString)geometry;
        write(out, multiLine, dimension);
      } else if (geometry instanceof Polygon) {
        Polygon polygon = (Polygon)geometry;
        write(out, polygon, dimension);
      } else if (geometry instanceof MultiPolygon) {
        MultiPolygon multiPolygon = (MultiPolygon)geometry;
        write(out, multiPolygon, dimension);
      } else if (geometry instanceof GeometryCollection) {
        GeometryCollection geometryCollection = (GeometryCollection)geometry;
        write(out, geometryCollection, dimension);
      } else {
        throw new IllegalArgumentException("Unknown geometry type"
          + geometry.getClass());
      }
    }
  }

  public static void write(
    PrintWriter out,
    Point point) {
    int dimension = Math.min(getDimension(point), 3);
    write(out, point, dimension);
  }

  private static void write(
    PrintWriter out,
    Point point,
    int dimension) {
    if (point.isEmpty()) {
      out.print("POINT EMPTY");
    } else {
      if (dimension > 2) {
        out.print("POINT Z(");
      } else {
        out.print("POINT(");
      }
      final CoordinateSequence coordinates = point.getCoordinateSequence();
      write(out, coordinates, 0, dimension);
      out.print(')');
    }
  }

  public static void write(
    PrintWriter out,
    LineString line) {
    int dimension = Math.min(getDimension(line), 3);
    write(out, line, dimension);
  }

  private static void write(
    PrintWriter out,
    LineString line,
    int dimension) {
    if (line.isEmpty()) {
      out.print("LINE_STRING EMPTY");
    } else {
      if (dimension > 2) {
        out.print("LINE_STRING Z");
      } else {
        out.print("LINE_STRING");
      }
      final CoordinateSequence coordinates = line.getCoordinateSequence();
      write(out, coordinates, dimension);
    }
  }

  public static void write(
    PrintWriter out,
    Polygon polygon) {
    int dimension = Math.min(getDimension(polygon), 3);
    write(out, polygon, dimension);
  }

  private static void write(
    PrintWriter out,
    Polygon polygon,
    int dimension) {
    if (polygon.isEmpty()) {
      out.print("POLYGON EMPTY");
    } else {
      if (dimension > 2) {
        out.print("POLYGON Z");
      } else {
        out.print("POLYGON");
      }
      writePolygon(out, polygon, dimension);
    }
  }

  private static void writePolygon(
    PrintWriter out,
    Polygon polygon,
    int dimension) {
    out.print('(');
    final LineString shell = polygon.getExteriorRing();
    final CoordinateSequence coordinates = shell.getCoordinateSequence();
    write(out, coordinates, dimension);
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      final LineString hole = polygon.getInteriorRingN(i);
      final CoordinateSequence holeCoordinates = hole.getCoordinateSequence();
      write(out, holeCoordinates, dimension);
    }
    out.print(')');
  }

  public static void write(
    PrintWriter out,
    MultiPoint multiPoint) {
    int dimension = Math.min(getDimension(multiPoint), 3);
    write(out, multiPoint, dimension);
  }

  private static void write(
    PrintWriter out,
    MultiPoint multiPoint,
    int dimension) {
    if (multiPoint.isEmpty()) {
      out.print("MULTIPOINT EMPTY");
    } else {
      Point point = (Point)multiPoint.getGeometryN(0);
      CoordinateSequence coordinates = point.getCoordinateSequence();
      if (dimension > 2) {
        out.print("MUTLIPOINT Z(");
      } else {
        out.print("MUTLIPOINT(");
      }
      write(out, coordinates, 0, dimension);
      for (int i = 1; i < multiPoint.getNumGeometries(); i++) {
        out.print(',');
        point = (Point)multiPoint.getGeometryN(i);
        coordinates = point.getCoordinateSequence();
        write(out, coordinates, 0, dimension);
      }
      out.print(')');
    }
  }

  private static int getDimension(
    Geometry geometry) {
    int dimension = 0;
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      final Geometry subGeometry = geometry.getGeometryN(i);
      final int geometryDimension = CoordinatesListUtil.get(subGeometry).getNumAxis();
      dimension = Math.max(dimension, geometryDimension);
    }
    return dimension;
  }

  public static void write(
    PrintWriter out,
    MultiLineString multiLineString) {
    int dimension = Math.min(getDimension(multiLineString), 3);
    write(out, multiLineString, dimension);
  }

  private static void write(
    PrintWriter out,
    MultiLineString multiLineString,
    int dimension) {
    if (multiLineString.isEmpty()) {
      out.print("MULTILINESTRING EMPTY");
    } else {
      if (dimension > 2) {
        out.print("MULTILINESTRING Z(");
      } else {
        out.print("MULTILINESTRING(");
      }

      LineString line = (LineString)multiLineString.getGeometryN(0);
      CoordinateSequence coordinates = line.getCoordinateSequence();
      write(out, coordinates, 0, dimension);
      for (int i = 1; i < multiLineString.getNumGeometries(); i++) {
        out.print(',');
        line = (LineString)multiLineString.getGeometryN(i);
        coordinates = line.getCoordinateSequence();
        write(out, coordinates, 0, dimension);
      }
      out.print(')');
    }
  }

  public static void write(
    PrintWriter out,
    MultiPolygon multiPolygon) {
    int dimension = Math.min(getDimension(multiPolygon), 3);
    write(out, multiPolygon, dimension);
  }

  private static void write(
    PrintWriter out,
    MultiPolygon multiPolygon,
    int dimension) {
    if (multiPolygon.isEmpty()) {
      out.print("MULTIPOLYGON EMPTY");
    } else {
      if (dimension > 2) {
        out.print("MULTIPOLYGON Z(");
      } else {
        out.print("MULTIPOLYGON(");
      }

      Polygon polygon = (Polygon)multiPolygon.getGeometryN(0);
      writePolygon(out, polygon, dimension);
      for (int i = 1; i < multiPolygon.getNumGeometries(); i++) {
        out.print(',');
        polygon = (Polygon)multiPolygon.getGeometryN(i);
        writePolygon(out, polygon, dimension);
      }
      out.print(')');
    }
  }

  public static void write(
    PrintWriter out,
    GeometryCollection multiGeometry) {
    int dimension = Math.min(getDimension(multiGeometry), 3);
    write(out, multiGeometry, dimension);
  }

  private static void write(
    PrintWriter out,
    GeometryCollection multiGeometry,
    int dimension) {
    if (multiGeometry.isEmpty()) {
      out.print("MULTIGEOMETRY EMPTY");
    } else {
      if (dimension > 2) {
        out.print("MULTIGEOMETRY Z(");
      } else {
        out.print("MULTIGEOMETRY(");
      }

      Geometry geometry = multiGeometry.getGeometryN(0);
      write(out, geometry, dimension);
      for (int i = 1; i < multiGeometry.getNumGeometries(); i++) {
        out.print(',');
        geometry = multiGeometry.getGeometryN(i);
        write(out, geometry, dimension);
      }
      out.print(')');
    }
  }

  public static void write(
    PrintWriter out,
    CoordinateSequence coordinates,
    int dimension) {
    out.print('(');
    write(out, coordinates, 0, dimension);
    for (int i = 1; i < coordinates.size(); i++) {
      out.print(',');
      write(out, coordinates, i, dimension);
    }
    out.print(')');
  }

  private static void write(
    PrintWriter out,
    CoordinateSequence coordinates,
    int index,
    int dimension) {
    writeOrdinate(out, coordinates, index, 0);
    for (int j = 1; j < dimension; j++) {
      out.print(' ');
      writeOrdinate(out, coordinates, index, j);
    }
  }

  private static void writeOrdinate(
    PrintWriter out,
    CoordinateSequence coordinates,
    int index,
    int ordinateIndex) {
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
