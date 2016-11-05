package com.revolsys.geometry.cs.parallel;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.record.Record;

public class GeometryProjection extends BaseInOutProcess<Record, Record> {
  private GeometryFactory geometryFactory;

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  protected void process(final Channel<Record> in, final Channel<Record> out, final Record object) {
    final Geometry geometry = object.getGeometry();

    if (geometry != null) {
      final Geometry projectedGeometry = geometry.newGeometry(this.geometryFactory);
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
