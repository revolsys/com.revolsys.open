package com.revolsys.gis.model.geometry.operation.distance;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.LineString;
import com.revolsys.gis.model.geometry.Point;
import com.revolsys.gis.model.geometry.Polygon;

/**
 * A ConnectedElementPointFilter extracts a single point from each connected
 * element in a Geometry (e.g. a polygon, linestring or point) and returns them
 * in a list. The elements of the list are
 * {@link com.vividsolutions.jts.operation.distance.GeometryLocation}s.
 * 
 * @version 1.7
 */
public class ConnectedElementLocationFilter {

  /**
   * Returns a list containing a point from each Polygon, LineString, and Point
   * found inside the specified geometry. Thus, if the specified geometry is not
   * a GeometryCollection, an empty list will be returned. The elements of the
   * list are {@link com.vividsolutions.jts.operation.distance.GeometryLocation}
   * s.
   */
  public static List<GeometryLocation> getLocations(final Geometry geom) {
    final List<GeometryLocation> locations = new ArrayList<GeometryLocation>();
    final ConnectedElementLocationFilter filter = new ConnectedElementLocationFilter(
      locations);
    for (final Geometry geometry : geom.getGeometries()) {
      filter.filter(geometry);
    }
    return locations;
  }

  private final List<GeometryLocation> locations;

  ConnectedElementLocationFilter(final List<GeometryLocation> locations) {
    this.locations = locations;
  }

  public void filter(final Geometry geom) {
    if (geom instanceof Point || geom instanceof LineString
      || geom instanceof Polygon) {
      locations.add(new GeometryLocation(geom, 0, geom.getFirstPoint()));
    }
  }

}
