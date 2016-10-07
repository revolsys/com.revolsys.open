package com.revolsys.geometry.test.function;

import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;

public class FunctionsUtil {

  public static final BoundingBox DEFAULT_ENVELOPE = new BoundingBoxDoubleXY(0, 0, 100, 100);

  public static Geometry buildGeometry(final List geoms, final Geometry parentGeom) {
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
      return GeometryFactory.DEFAULT;
    } else {
      return g.getGeometryFactory();
    }
  }

}
