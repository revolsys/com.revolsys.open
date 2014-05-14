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
package com.revolsys.jts.geom.util;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;

/**
 * @version 1.7
 */
public class CleanDuplicatePoints {

  public static PointList clean(final PointList points) {
    if (points.size() == 0) {
      return points;
    } else {
      final int axisCount = points.getAxisCount();
      final int vertexCount = points.size();
      final double[] coordinates = new double[vertexCount * axisCount];
      double previousX = points.getX(0);
      double previousY = points.getY(0);
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, 0, points, 0);
      int j = 1;
      for (int i = 0; i < points.size(); i++) {
        final double x = points.getX(i);
        final double y = points.getY(i);
        if (x != previousX || y != previousY) {
          CoordinatesListUtil.setCoordinates(coordinates, axisCount, j++,
            points, i);
        }
        previousX = x;
        previousY = y;
      }
      return new DoubleCoordinatesList(axisCount, j, coordinates);
    }
  }

  public static Geometry clean(final Geometry geometry) {
    if (geometry.isEmpty()) {
      return geometry;
    } else if (geometry instanceof Point) {
      return geometry;
    } else if (geometry instanceof MultiPoint) {
      return geometry;
    } else if (geometry instanceof LinearRing) {
      return clean((LinearRing)geometry);
    } else if (geometry instanceof LineString) {
      return clean((LineString)geometry);
    } else if (geometry instanceof Polygon) {
      return clean((Polygon)geometry);
    } else if (geometry instanceof MultiLineString) {
      return clean((MultiLineString)geometry);
    } else if (geometry instanceof MultiPolygon) {
      return clean((MultiPolygon)geometry);
    } else if (geometry instanceof GeometryCollection) {
      return clean((GeometryCollection)geometry);
    } else {
      throw new UnsupportedOperationException(geometry.getClass().getName());
    }
  }

  public static GeometryCollection clean(final GeometryCollection collection) {
    if (collection.isEmpty()) {
      return collection;
    } else {
      final List<Geometry> geometries = new ArrayList<Geometry>();
      for (final Geometry geometry : collection.geometries()) {
        if (geometry != null && !geometry.isEmpty()) {
          geometries.add(clean(geometry));
        }
      }
      final GeometryFactory geometryFactory = collection.getGeometryFactory();
      return geometryFactory.geometryCollection(geometries);
    }
  }

  public static LinearRing clean(final LinearRing ring) {
    if (ring.isEmpty()) {
      return ring;
    } else {
      final PointList points = clean(ring.getCoordinatesList());
      final GeometryFactory geometryFactory = ring.getGeometryFactory();
      if (points.size() < 4) {
        return geometryFactory.linearRing();
      } else {
        return geometryFactory.linearRing(points);
      }
    }
  }

  public static LineString clean(final LineString line) {
    if (line.isEmpty()) {
      return line;
    } else {
      final PointList points = clean(line.getCoordinatesList());
      final GeometryFactory geometryFactory = line.getGeometryFactory();
      if (points.size() < 2) {
        return geometryFactory.lineString();
      } else {
        return geometryFactory.lineString(points);
      }
    }
  }

  public static MultiLineString clean(final MultiLineString multiLineString) {
    if (multiLineString.isEmpty()) {
      return multiLineString;
    } else {
      final List<LineString> lines = new ArrayList<LineString>();
      for (final LineString line : multiLineString.getLineStrings()) {
        if (line != null && !line.isEmpty()) {
          lines.add(clean(line));
        }
      }
      final GeometryFactory geometryFactory = multiLineString.getGeometryFactory();
      return geometryFactory.multiLineString(lines);
    }
  }

  public static MultiPolygon clean(final MultiPolygon multiPolygon) {
    if (multiPolygon.isEmpty()) {
      return multiPolygon;
    } else {
      final List<Polygon> polygons = new ArrayList<Polygon>();
      for (final Polygon polygon : multiPolygon.getPolygons()) {
        if (polygon != null && !polygon.isEmpty()) {
          polygons.add(clean(polygon));
        }
      }
      final GeometryFactory geometryFactory = multiPolygon.getGeometryFactory();
      return geometryFactory.multiPolygon(polygons);
    }
  }

  public static Polygon clean(final Polygon polygon) {
    if (polygon.isEmpty()) {
      return polygon;
    } else {
      final List<LinearRing> rings = new ArrayList<>();
      for (final LinearRing ring : polygon.getRings()) {
        final LinearRing newRing = clean(ring);
        rings.add(newRing);
      }
      final GeometryFactory geometryFactory = polygon.getGeometryFactory();
      return geometryFactory.polygon(rings);
    }
  }

}
