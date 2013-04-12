package com.revolsys.gis.model.geometry.util;

import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.InPlaceIterator;
import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.Polygon;
import com.revolsys.gis.model.geometry.algorithm.locate.IndexedPointInAreaLocator;
import com.revolsys.gis.model.geometry.algorithm.locate.PointOnGeometryLocator;
import com.revolsys.gis.model.geometry.algorithm.locate.SimplePointInAreaLocator;
import com.vividsolutions.jts.geom.Location;

public class PolygonUtil {

  public static boolean isAllTestComponentsInTarget(final Polygon polygon,
    final Geometry geometry) {
    final PointOnGeometryLocator targetPointLocator = IndexedPointInAreaLocator.get(polygon);
    final List<CoordinatesList> pointsList = geometry.getCoordinatesLists();
    for (final CoordinatesList points : pointsList) {
      for (final Coordinates point : new InPlaceIterator(points)) {
        final int loc = targetPointLocator.locate(point);
        if (loc == Location.EXTERIOR) {
          return false;
        }
      }
    }

    return true;
  }

  public static boolean isAllTestComponentsInTargetInterior(
    final Polygon polygon, final Geometry geometry) {
    final PointOnGeometryLocator targetPointLocator = IndexedPointInAreaLocator.get(polygon);
    final List<CoordinatesList> pointsList = geometry.getCoordinatesLists();
    for (final CoordinatesList points : pointsList) {
      for (final Coordinates point : new InPlaceIterator(points)) {
        final int loc = targetPointLocator.locate(point);
        if (loc != Location.INTERIOR) {
          return false;
        }
      }
    }
    return true;
  }

  public static boolean isAnyTargetComponentInAreaTest(final Geometry geometry,
    final List<CoordinatesList> pointsList) {
    final PointOnGeometryLocator locator = new SimplePointInAreaLocator(
      geometry);
    for (final CoordinatesList points : pointsList) {
      for (final Coordinates point : new InPlaceIterator(points)) {
        final int loc = locator.locate(point);
        if (loc != Location.EXTERIOR) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isAnyTestComponentInTarget(final Polygon polygon,
    final Geometry geometry) {
    final PointOnGeometryLocator targetPointLocator = IndexedPointInAreaLocator.get(polygon);
    final List<CoordinatesList> pointsList = geometry.getCoordinatesLists();
    for (final CoordinatesList points : pointsList) {
      for (final Coordinates point : new InPlaceIterator(points)) {
        final int loc = targetPointLocator.locate(point);
        if (loc != Location.EXTERIOR) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isAnyTestComponentInTargetInterior(
    final Polygon polygon, final Geometry geometry) {
    final PointOnGeometryLocator targetPointLocator = IndexedPointInAreaLocator.get(polygon);
    final List<CoordinatesList> pointsList = geometry.getCoordinatesLists();
    for (final CoordinatesList points : pointsList) {
      for (final Coordinates point : new InPlaceIterator(points)) {
        final int loc = targetPointLocator.locate(point);
        if (loc == Location.INTERIOR) {
          return true;
        }
      }
    }
    return false;
  }

}
