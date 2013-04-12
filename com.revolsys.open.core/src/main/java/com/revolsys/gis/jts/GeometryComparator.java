package com.revolsys.gis.jts;

import java.util.Comparator;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.comparator.LowestLeftComparator;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.vividsolutions.jts.geom.Geometry;

public class GeometryComparator implements Comparator<Geometry> {

  @Override
  public int compare(final Geometry geometry1, final Geometry geometry2) {
    if (geometry1 == geometry2) {
      return 0;
    } else if (geometry1 == null) {
      if (geometry2 == null) {
        return 0;
      } else {
        return -1;
      }
    } else if (geometry2 == null) {
      return 1;
    } else {
      final int numGeometries1 = geometry1.getNumGeometries();
      final int numGeometries2 = geometry2.getNumGeometries();
      for (int i = 0; i < Math.max(numGeometries1, numGeometries2); i++) {
        if (i > numGeometries1) {
          return -1;
        } else if (i > numGeometries2) {
          return -1;
        } else {
          final Geometry part1 = geometry1.getGeometryN(i);
          final Geometry part2 = geometry2.getGeometryN(i);
          if (!part1.equalsExact(part2)) {
            final CoordinatesList points1 = CoordinatesListUtil.get(part1);
            final CoordinatesList points2 = CoordinatesListUtil.get(part2);
            final int numPoints1 = points1.size();
            final int numPoints2 = points2.size();
            for (int j = 0; j < Math.max(numPoints1, numPoints2); j++) {
              if (j > numPoints1) {
                return -1;
              } else if (j > numPoints2) {
                return -1;
              } else {
                final Coordinates point1 = points1.get(j);
                final Coordinates point2 = points2.get(j);
                final int compare = LowestLeftComparator.compareCoordinates(
                  point1, point2);
                if (compare != 0) {
                  return compare;
                }
              }
            }

          }
        }
      }
    }
    return 0;
  }
}
