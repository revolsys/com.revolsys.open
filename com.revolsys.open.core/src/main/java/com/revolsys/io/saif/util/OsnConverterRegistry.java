package com.revolsys.io.saif.util;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.gis.cs.GeometryFactory;

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

  public OsnConverter getConverter(final String name) {
    if (name == null) {
      return null;
    } else {
      return converters.get(name);
    }
  }

  public void init(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    addConverter("/Date", new DateConverter());
    addConverter("/SpatialObject", new SpatialObjectConverter(this));
    addConverter("/Arc", new ArcConverter(geometryFactory));
    addConverter("/OrientedArc",
      new OrientedArcConverter(geometryFactory, this));
    addConverter("/ArcDirected", new ArcDirectedConverter(geometryFactory));
    addConverter("/Contour", new ContourConverter(geometryFactory, this));
    addConverter("/Point", new PointConverter(geometryFactory));
    addConverter("/AlignedPoint", new AlignedPointConverter(geometryFactory));
    addConverter("/TextLine", new TextLineConverter(geometryFactory, this));
    addConverter("/TextOnCurve",
      new TextOnCurveConverter(geometryFactory, this));
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }
}
