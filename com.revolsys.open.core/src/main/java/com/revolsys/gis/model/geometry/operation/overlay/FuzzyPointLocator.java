package com.revolsys.gis.model.geometry.operation.overlay;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.LineString;
import com.revolsys.gis.model.geometry.MultiLineString;
import com.revolsys.gis.model.geometry.Polygon;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.PointLocator;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Location;

/**
 * Finds the most likely {@link Location} of a point relative to the polygonal
 * components of a geometry, using a tolerance value. If a point is not clearly
 * in the Interior or Exterior, it is considered to be on the Boundary. In other
 * words, if the point is within the tolerance of the Boundary, it is considered
 * to be on the Boundary; otherwise, whether it is Interior or Exterior is
 * determined directly.
 * 
 * @author Martin Davis
 * @version 1.7
 */
public class FuzzyPointLocator {
  private final Geometry g;

  private final double boundaryDistanceTolerance;

  private final MultiLineString linework;

  private final PointLocator ptLocator = new PointLocator();

  public FuzzyPointLocator(final Geometry g,
    final double boundaryDistanceTolerance) {
    this.g = g;
    this.boundaryDistanceTolerance = boundaryDistanceTolerance;
    linework = extractLinework(g);
  }

  /**
   * Extracts linework for polygonal components.
   * 
   * @param g the geometry from which to extract
   * @return a lineal geometry containing the extracted linework
   */
  private MultiLineString extractLinework(final Geometry g) {
    final List<CoordinatesList> lines = new ArrayList<CoordinatesList>();
    for (final Geometry part : g.getGeometries()) {
      if (part instanceof Polygon) {
        final Polygon polygon = (Polygon)part;
        lines.addAll(polygon.getCoordinatesLists());
      }

    }
    return g.getGeometryFactory().createMultiLineString(lines);
  }

  public int getLocation(final Coordinates pt) {
    if (isWithinToleranceOfBoundary(pt)) {
      return Location.BOUNDARY;
      /*
       * double dist = linework.distance(point); // if point is close to
       * boundary, it is considered to be on the boundary if (dist < tolerance)
       * return Location.BOUNDARY;
       */
    }

    // now we know point must be clearly inside or outside geometry, so return
    // actual location value
    return ptLocator.locate(pt, g);
  }

  private boolean isWithinToleranceOfBoundary(final Coordinates pt) {
    for (int i = 0; i < linework.getGeometryCount(); i++) {
      final LineString line = (LineString)linework.getGeometry(i);
      for (int j = 0; j < line.size() - 1; j++) {
        final Coordinates p0 = line.get(j);
        final Coordinates p1 = line.get(j + 1);
        final double dist = LineSegmentUtil.distance(p0, p1, pt);
        if (dist <= boundaryDistanceTolerance) {
          return true;
        }
      }
    }
    return false;
  }
}
