package com.revolsys.io.saif.util;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.io.saif.SaifConstants;
import com.revolsys.jts.geom.GeometryFactory;

public class OsnConverterRegistry {
  private final Map<String, OsnConverter> converters = new HashMap<String, OsnConverter>();

  private com.revolsys.jts.geom.GeometryFactory geometryFactory;

  public OsnConverterRegistry() {
    final com.revolsys.jts.geom.GeometryFactory geometryFactory = GeometryFactory.fixedNoSrid(
      1.0, 1.0);
    init(geometryFactory);
  }

  public OsnConverterRegistry(final int srid) {
    final com.revolsys.jts.geom.GeometryFactory geometryFactory = GeometryFactory.fixed(
      srid, 1.0, 1.0);

    init(geometryFactory);
  }

  private void addConverter(final String name, final OsnConverter converter) {
    converters.put(name, converter);
  }

  public OsnConverter getConverter(String name) {
    if (name == null) {
      return null;
    } else {
      if (name.startsWith("/")) {
        name = name.substring(1);
      }
      return converters.get(name);
    }
  }

  public com.revolsys.jts.geom.GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public void init(final com.revolsys.jts.geom.GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    addConverter("Date", new DateConverter());
    addConverter("SpatialObject", new SpatialObjectConverter(this));
    addConverter(SaifConstants.ARC, new ArcConverter(geometryFactory));
    addConverter(SaifConstants.ORIENTED_ARC, new OrientedArcConverter(
      geometryFactory, this));
    addConverter(SaifConstants.ARC_DIRECTED, new ArcDirectedConverter(
      geometryFactory));
    addConverter(SaifConstants.CONTOUR, new ContourConverter(geometryFactory,
      this));
    addConverter(SaifConstants.POINT, new PointConverter(geometryFactory));
    addConverter(SaifConstants.ALIGNED_POINT, new AlignedPointConverter(
      geometryFactory));
    addConverter(SaifConstants.TEXT_LINE, new TextLineConverter(
      geometryFactory, this));
    addConverter(SaifConstants.TEXT_ON_CURVE, new TextOnCurveConverter(
      geometryFactory, this));
  }
}
