package com.revolsys.gis.cs.parallel;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.AbstractInOutProcess;
import com.vividsolutions.jts.geom.Geometry;

public class GeometryProjectionProcess extends AbstractInOutProcess<DataObject> {
  private GeometryFactory geometryFactory;

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  @Override
  protected void run(
    final Channel<DataObject> in,
    final Channel<DataObject> out) {
    while (true) {
      final DataObject object = in.read();
      if (object != null) {
        final Geometry geometry = object.getGeometryValue();
        if (geometry != null) {
          Geometry projectedGeometry = GeometryProjectionUtil.perform(geometry,
            geometryFactory);
          JtsGeometryUtil.makePrecise(geometryFactory.getPrecisionModel(), projectedGeometry);
          if (geometry != projectedGeometry) {
            object.setGeometryValue(projectedGeometry);
          }
        }
        out.write(object);
      }
    }
  }

  public void setGeometryFactory(
    final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }
}
