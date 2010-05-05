package com.revolsys.gis.parallel;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.vividsolutions.jts.geom.Geometry;

public class GeometryBufferProcess extends BaseInOutProcess<DataObject> {

  private int buffer;

  public int getBuffer() {
    return buffer;
  }

  @Override
  protected void process(
    Channel<DataObject> in,
    Channel<DataObject> out,
    DataObject object) {
    final Geometry geometry = object.getGeometryValue();
    if (geometry == null) {
      out.write(object);
    } else {
      final Geometry bufferedGeometry = geometry.buffer(buffer);
      final DataObject newObject = DataObjectUtil.copy(object, bufferedGeometry);
      out.write(newObject);
    }
  }

  public void setBuffer(
    final int buffer) {
    this.buffer = buffer;
  }

}
