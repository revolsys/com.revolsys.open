package com.revolsys.gis.format.saif.io.util;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;

public class OsnConverterRegistry {
  private final Map<QName, OsnConverter> converters = new HashMap<QName, OsnConverter>();

  public OsnConverterRegistry(
    final int srid) {
    final GeometryFactory geometryFactory = new GeometryFactory(EpsgCoordinateSystems.getCoordinateSystem(srid),
      new SimpleCoordinatesPrecisionModel(1));
    addConverter("Date", new DateConverter());
    addConverter("SpatialObject", new SpatialObjectConverter(this));
    addConverter("Arc", new ArcConverter(geometryFactory));
    addConverter("OrientedArc", new OrientedArcConverter(geometryFactory, this));
    addConverter("ArcDirected", new ArcDirectedConverter(geometryFactory));
    addConverter("Contour", new ContourConverter(geometryFactory, this));
    addConverter("Point", new PointConverter(geometryFactory));
    addConverter("AlignedPoint", new AlignedPointConverter(geometryFactory));
    addConverter("TextLine", new TextLineConverter(geometryFactory, this));
    addConverter("TextOnCurve", new TextOnCurveConverter(geometryFactory, this));
  }

  private void addConverter(
    final String name,
    final OsnConverter converter) {
    converters.put(QName.valueOf(name), converter);
  }

  public OsnConverter getConverter(
    final QName name) {
    return converters.get(name);
  }

  public OsnConverter getConverter(
    final String name) {
    if (name == null) {
      return null;
    } else {
      return converters.get(QName.valueOf(name));
    }
  }
}
