package com.revolsys.gis.cs.parallel;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class GeometryProjection extends
  BaseInOutProcess<DataObject, DataObject> {
  private GeometryFactory geometryFactory;

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  @Override
  protected void process(final Channel<DataObject> in,
    final Channel<DataObject> out, final DataObject object) {
    final Geometry geometry = object.getGeometryValue();

    if (geometry != null) {
      final Geometry projectedGeometry = geometry.copy(geometryFactory);
      if (geometry != projectedGeometry) {
        object.setGeometryValue(projectedGeometry);
      }
    }
    out.write(object);
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }
}
