package com.revolsys.gis.cs.parallel;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.vividsolutions.jts.geom.Geometry;

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
      final Geometry projectedGeometry = GeometryProjectionUtil.performCopy(
        geometry, geometryFactory);
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
