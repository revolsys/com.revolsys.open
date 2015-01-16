package com.revolsys.gis.jts;

import com.revolsys.jts.algorithm.PointInArea;
import com.revolsys.jts.algorithm.locate.IndexedPointInAreaLocator;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Location;

public class GeometryFactoryIndexedPointInAreaLocator extends
IndexedPointInAreaLocator {

  public static GeometryFactoryIndexedPointInAreaLocator get(
    final Geometry geometry) {
    GeometryFactoryIndexedPointInAreaLocator locator = GeometryProperties.getGeometryProperty(
      geometry, KEY);
    if (locator == null) {
      locator = new GeometryFactoryIndexedPointInAreaLocator(geometry);
      GeometryProperties.setGeometryProperty(geometry, KEY, locator);
    }
    return locator;
  }

  private static final String KEY = GeometryFactoryIndexedPointInAreaLocator.class.getName();

  public GeometryFactoryIndexedPointInAreaLocator(final Geometry geometry) {
    super(geometry);
  }

  @Override
  public Location locate(final double x, final double y) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final double resolutionXy = geometryFactory.getResolutionXy();
    final double minY = y - resolutionXy;
    final double maxY = y + resolutionXy;
    final PointInArea visitor = new PointInArea(geometryFactory, x, y);
    getIndex().query(minY, maxY, visitor);

    return visitor.getLocation();
  }

}
