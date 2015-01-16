package com.revolsys.jtstest.function;

import java.util.List;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;

public class FunctionsUtil {

  public static Geometry buildGeometry(final List geoms,
    final Geometry parentGeom) {
    if (geoms.size() <= 0) {
      return null;
    }
    if (geoms.size() == 1) {
      return (Geometry)geoms.get(0);
    }
    // if parent was a GC, ensure returning a GC
    if (parentGeom.getGeometryType().equals("GeometryCollection")) {
      return parentGeom.getGeometryFactory().geometryCollection(geoms);
    }
    // otherwise return MultiGeom
    return parentGeom.getGeometryFactory().buildGeometry(geoms);
  }

  public static BoundingBox getEnvelopeOrDefault(final Geometry g) {
    if (g == null) {
      return DEFAULT_ENVELOPE;
    } else {
      return g.getBoundingBox();
    }
  }

  public static GeometryFactory getFactoryOrDefault(final Geometry g) {
    if (g == null) {
      return GeometryFactory.floating3();
    } else {
      return g.getGeometryFactory();
    }
  }

  public static final BoundingBox DEFAULT_ENVELOPE = new BoundingBoxDoubleGf(2, 0, 0, 100,
    100);

}
