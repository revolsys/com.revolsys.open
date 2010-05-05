package com.revolsys.gis.cs.parallel;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.vividsolutions.jts.geom.Geometry;

public class GeometryProjectionProcess extends BaseInOutProcess<DataObject> {
  private GeometryFactory geometryFactory;

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  @Override
  protected void process(
    Channel<DataObject> in,
    Channel<DataObject> out,
    DataObject object) {
    final Geometry geometry = object.getGeometryValue();
    if (geometry != null) {
      Geometry projectedGeometry = GeometryProjectionUtil.perform(geometry,
        geometryFactory);
      JtsGeometryUtil.makePrecise(geometryFactory.getPrecisionModel(),
        projectedGeometry);
      if (geometry != projectedGeometry) {
        object.setGeometryValue(projectedGeometry);
      }
    }
    out.write(object);
  }

  public void setGeometryFactory(
    final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }
}
