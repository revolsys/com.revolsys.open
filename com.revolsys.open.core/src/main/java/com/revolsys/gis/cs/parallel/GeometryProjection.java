package com.revolsys.gis.cs.parallel;

import com.revolsys.data.record.Record;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class GeometryProjection extends BaseInOutProcess<Record, Record> {
  private GeometryFactory geometryFactory;

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  protected void process(final Channel<Record> in, final Channel<Record> out, final Record object) {
    final Geometry geometry = object.getGeometryValue();

    if (geometry != null) {
      final Geometry projectedGeometry = geometry.copy(this.geometryFactory);
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
