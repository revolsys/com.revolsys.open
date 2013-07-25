package com.revolsys.io.saif.util;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.io.saif.SaifConstants;

public class OsnConverterRegistry {
  private final Map<String, OsnConverter> converters = new HashMap<String, OsnConverter>();

  private GeometryFactory geometryFactory;

  public OsnConverterRegistry() {
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(1.0);
    init(geometryFactory);
  }

  public OsnConverterRegistry(final int srid) {
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(srid,
      1.0, 1.0);

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

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public void init(final GeometryFactory geometryFactory) {
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
