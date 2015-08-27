package com.revolsys.geometry.algorithm;

import com.revolsys.geometry.algorithm.locate.IndexedPointInAreaLocator;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.util.GeometryProperties;

public class GeometryFactoryIndexedPointInAreaLocator extends IndexedPointInAreaLocator {

  private static final String KEY = GeometryFactoryIndexedPointInAreaLocator.class.getName();

  public static GeometryFactoryIndexedPointInAreaLocator get(final Geometry geometry) {
    GeometryFactoryIndexedPointInAreaLocator locator = GeometryProperties
      .getGeometryProperty(geometry, KEY);
    if (locator == null) {
      locator = new GeometryFactoryIndexedPointInAreaLocator(geometry);
      GeometryProperties.setGeometryProperty(geometry, KEY, locator);
    }
    return locator;
  }

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
